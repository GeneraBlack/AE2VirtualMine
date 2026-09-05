package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AE2VirtualMine.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_MODE_TABS.register("ae2virtualmine_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ae2virtualmine"))
                    .icon(() -> new ItemStack(ModItems.MINE_CELL_4K.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MINE_CELL_HOUSING.get());
                        output.accept(ModItems.MINE_COMPONENT_1K.get());
                        output.accept(ModItems.MINE_COMPONENT_4K.get());
                        output.accept(ModItems.MINE_COMPONENT_16K.get());
                        output.accept(ModItems.MINE_COMPONENT_64K.get());
                        output.accept(ModItems.MINE_COMPONENT_256K.get());
                        output.accept(ModItems.MINE_CELL_1K.get());
                        output.accept(ModItems.MINE_CELL_4K.get());
                        output.accept(ModItems.MINE_CELL_16K.get());
                        output.accept(ModItems.MINE_CELL_64K.get());
                        output.accept(ModItems.MINE_CELL_256K.get());
                    })
                    .build());
}
