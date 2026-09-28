package de.project.ae2virtualmine.cell;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import de.project.ae2virtualmine.cell.partition.MineCellPartition;
import de.project.ae2virtualmine.cell.partition.MineCellPartitionList;
import de.project.ae2virtualmine.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IVirtualMineCell extends StorageCell {
    ItemStack getItemStack();
    @Nullable
    ISaveProvider getSaveProvider();
    MineCellTier getTier();
    @Nullable
    Item getConfiguredTarget();
    boolean isFull();
    long injectGeneratedDrop(AEKey key, long amount, Actionable mode);

    default MineCellPartitionList getPartitions() {
        ItemStack stack = getItemStack();
        if (stack.has(ModDataComponents.PARTITIONS.get())) {
            MineCellPartitionList list = stack.get(ModDataComponents.PARTITIONS.get());
            if (list != null && !list.isEmpty()) {
                return list;
            }
        }
        Item single = getConfiguredTarget();
        if (single != null) {
            return new MineCellPartitionList(List.of(new MineCellPartition(single, 100, false)));
        }
        return MineCellPartitionList.EMPTY;
    }

    default long getStoredCountForTarget(Item target) {
        return 0;
    }

    default boolean isPartitionFull(MineCellPartition partition) {
        if (isFull()) {
            return true;
        }
        if (partition == null || partition.percent() <= 0) {
            return true;
        }
        long allocatedBytes = (getTier().getTotalBytes() * partition.percent()) / 100L;
        long storedCount = getStoredCountForTarget(partition.target());
        long storedBytes = (storedCount + 7L) / 8L + (long) getTier().getBytesPerType();
        return storedBytes >= allocatedBytes;
    }
}
