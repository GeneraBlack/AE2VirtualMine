package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.menu.VirtualPartitionerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AE2VirtualMine.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<VirtualPartitionerMenu>> PARTITIONER_MENU =
            MENUS.register("virtual_partitioner", () -> IMenuTypeExtension.create(VirtualPartitionerMenu::new));
}
