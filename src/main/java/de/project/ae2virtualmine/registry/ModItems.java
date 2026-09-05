package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.cell.MineCellTier;
import de.project.ae2virtualmine.cell.VirtualMineCellItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AE2VirtualMine.MODID);

    // Housing
    public static final DeferredHolder<Item, Item> MINE_CELL_HOUSING =
            ITEMS.register("mine_cell_housing", () -> new Item(new Item.Properties()));

    // Storage Components
    public static final DeferredHolder<Item, Item> MINE_COMPONENT_1K =
            ITEMS.register("mine_cell_component_1k", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MINE_COMPONENT_4K =
            ITEMS.register("mine_cell_component_4k", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MINE_COMPONENT_16K =
            ITEMS.register("mine_cell_component_16k", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MINE_COMPONENT_64K =
            ITEMS.register("mine_cell_component_64k", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MINE_COMPONENT_256K =
            ITEMS.register("mine_cell_component_256k", () -> new Item(new Item.Properties()));

    // Complete Storage Cells
    public static final DeferredHolder<Item, VirtualMineCellItem> MINE_CELL_1K =
            ITEMS.register("mine_storage_cell_1k", () -> new VirtualMineCellItem(MineCellTier.TIER_1K, new Item.Properties()));
    public static final DeferredHolder<Item, VirtualMineCellItem> MINE_CELL_4K =
            ITEMS.register("mine_storage_cell_4k", () -> new VirtualMineCellItem(MineCellTier.TIER_4K, new Item.Properties()));
    public static final DeferredHolder<Item, VirtualMineCellItem> MINE_CELL_16K =
            ITEMS.register("mine_storage_cell_16k", () -> new VirtualMineCellItem(MineCellTier.TIER_16K, new Item.Properties()));
    public static final DeferredHolder<Item, VirtualMineCellItem> MINE_CELL_64K =
            ITEMS.register("mine_storage_cell_64k", () -> new VirtualMineCellItem(MineCellTier.TIER_64K, new Item.Properties()));
    public static final DeferredHolder<Item, VirtualMineCellItem> MINE_CELL_256K =
            ITEMS.register("mine_storage_cell_256k", () -> new VirtualMineCellItem(MineCellTier.TIER_256K, new Item.Properties()));
}
