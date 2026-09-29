package de.project.ae2virtualmine;

import appeng.api.networking.GridServices;
import appeng.api.storage.StorageCells;
import de.project.ae2virtualmine.cell.VirtualMineCellHandler;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.network.IVirtualMineGridService;
import de.project.ae2virtualmine.network.VirtualMineGridService;
import de.project.ae2virtualmine.registry.ModBlocks;
import de.project.ae2virtualmine.registry.ModCreativeTabs;
import de.project.ae2virtualmine.registry.ModItems;
import de.project.ae2virtualmine.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Mod(AE2VirtualMine.MODID)
public class AE2VirtualMine {
    public static final String MODID = "ae2virtualmine";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public AE2VirtualMine(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing AE2 Virtual Mine");

        // Register Config
        modContainer.registerConfig(ModConfig.Type.COMMON, VirtualMineConfig.SPEC);

        // Register Registries
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        de.project.ae2virtualmine.registry.ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        de.project.ae2virtualmine.registry.ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        de.project.ae2virtualmine.registry.ModDataComponents.DATA_COMPONENTS.register(modEventBus);

        // Register Grid Service during mod init
        GridServices.register(IVirtualMineGridService.class, VirtualMineGridService.class);

        // Register Setup Listener
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(de.project.ae2virtualmine.network.VirtualPartitionerNetworking::onRegisterPayloadHandlers);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
            modEventBus.addListener(de.project.ae2virtualmine.client.VirtualPartitionerClient::onRegisterMenuScreens);
        }

        // Refresh recipe cache and clear dynamic cache when tags/datapacks update
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.TagsUpdatedEvent event) -> {
            de.project.ae2virtualmine.recipe.MineDropRegistry.clearCache();
            // BUG-07: Rebuild the recipe cache from the server's loaded recipes
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                de.project.ae2virtualmine.recipe.MineDropRegistry.refreshRecipeCache(server.getRecipeManager());
                LOGGER.info("AE2 Virtual Mine: Refreshed recipe cache");
            }
        });
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Registering AE2 Virtual Mine Storage Cell Handler");
            StorageCells.addCellHandler(new VirtualMineCellHandler());

            // Register Upgrades on all Mine Storage Cells
            for (var cell : java.util.List.of(
                    ModItems.MINE_CELL_1K,
                    ModItems.MINE_CELL_4K,
                    ModItems.MINE_CELL_16K,
                    ModItems.MINE_CELL_64K,
                    ModItems.MINE_CELL_256K
            )) {
                appeng.api.upgrades.Upgrades.add(appeng.core.definitions.AEItems.SPEED_CARD.asItem(), cell.get(), 4);
                appeng.api.upgrades.Upgrades.add(ModItems.VOID_SECONDARY_CARD.get(), cell.get(), 1);
                appeng.api.upgrades.Upgrades.add(appeng.core.definitions.AEItems.VOID_CARD.asItem(), cell.get(), 1);

                for (String ns : List.of("ae2virtualgarden", "ae2virtualbattle", "ae2virtualwell")) {
                    net.minecraft.world.item.Item sisterCard = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(ns, "void_secondary_card")
                    ).map(net.minecraft.core.Holder::value).orElse(net.minecraft.world.item.Items.AIR);
                    if (sisterCard != net.minecraft.world.item.Items.AIR) {
                        appeng.api.upgrades.Upgrades.add(sisterCard, cell.get(), 1);
                    }
                }
            }
        });
    }
}




