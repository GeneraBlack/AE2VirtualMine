package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.cell.MineCellTier;
import de.project.ae2virtualmine.cell.VirtualMineCellItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AE2VirtualMine.MODID);

    // Housing
    public static final DeferredItem<Item> MINE_CELL_HOUSING =
            ITEMS.registerSimpleItem("mine_cell_housing");

    // Storage Components
    public static final DeferredItem<Item> MINE_COMPONENT_1K =
            ITEMS.registerSimpleItem("mine_cell_component_1k");
    public static final DeferredItem<Item> MINE_COMPONENT_4K =
            ITEMS.registerSimpleItem("mine_cell_component_4k");
    public static final DeferredItem<Item> MINE_COMPONENT_16K =
            ITEMS.registerSimpleItem("mine_cell_component_16k");
    public static final DeferredItem<Item> MINE_COMPONENT_64K =
            ITEMS.registerSimpleItem("mine_cell_component_64k");
    public static final DeferredItem<Item> MINE_COMPONENT_256K =
            ITEMS.registerSimpleItem("mine_cell_component_256k");

    // Complete Storage Cells
    public static final DeferredItem<VirtualMineCellItem> MINE_CELL_1K =
            ITEMS.registerItem("mine_storage_cell_1k", props -> new VirtualMineCellItem(MineCellTier.TIER_1K, props));
    public static final DeferredItem<VirtualMineCellItem> MINE_CELL_4K =
            ITEMS.registerItem("mine_storage_cell_4k", props -> new VirtualMineCellItem(MineCellTier.TIER_4K, props));
    public static final DeferredItem<VirtualMineCellItem> MINE_CELL_16K =
            ITEMS.registerItem("mine_storage_cell_16k", props -> new VirtualMineCellItem(MineCellTier.TIER_16K, props));
    public static final DeferredItem<VirtualMineCellItem> MINE_CELL_64K =
            ITEMS.registerItem("mine_storage_cell_64k", props -> new VirtualMineCellItem(MineCellTier.TIER_64K, props));
    public static final DeferredItem<VirtualMineCellItem> MINE_CELL_256K =
            ITEMS.registerItem("mine_storage_cell_256k", props -> new VirtualMineCellItem(MineCellTier.TIER_256K, props));
}
