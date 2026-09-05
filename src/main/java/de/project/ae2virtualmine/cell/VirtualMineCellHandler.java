package de.project.ae2virtualmine.cell;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VirtualMineCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof VirtualMineCellItem;
    }

    @Nullable
    @Override
    public StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        if (is.getItem() instanceof VirtualMineCellItem cellItem) {
            return new VirtualMineCellInventory(is, host, cellItem.getTier());
        }
        return null;
    }
}
