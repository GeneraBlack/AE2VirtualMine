package de.project.ae2virtualmine;

import appeng.api.networking.GridServices;
import appeng.api.storage.StorageCells;
import de.project.ae2virtualmine.cell.VirtualMineCellHandler;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.network.IVirtualMineGridService;
import de.project.ae2virtualmine.network.VirtualMineGridService;
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

@Mod(AE2VirtualMine.MODID)
public class AE2VirtualMine {
    public static final String MODID = "ae2virtualmine";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public AE2VirtualMine(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing AE2 Virtual Mine");

        // Register Config
        modContainer.registerConfig(ModConfig.Type.COMMON, VirtualMineConfig.SPEC);

        // Register Registries
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);

        // Register Grid Service during mod init
        GridServices.register(IVirtualMineGridService.class, VirtualMineGridService.class);

        // Register Setup Listener
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Registering AE2 Virtual Mine Storage Cell Handler");
            StorageCells.addCellHandler(new VirtualMineCellHandler());
        });
    }
}
