package de.project.ae2virtualmine.cell.partition;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record MineCellPartitionList(List<MineCellPartition> partitions) {

    public static final MineCellPartitionList EMPTY = new MineCellPartitionList(List.of());

    public static final Codec<MineCellPartitionList> CODEC =
            MineCellPartition.CODEC.listOf().xmap(MineCellPartitionList::new, MineCellPartitionList::partitions);

    public static final StreamCodec<RegistryFriendlyByteBuf, MineCellPartitionList> STREAM_CODEC =
            MineCellPartition.STREAM_CODEC.apply(ByteBufCodecs.list()).map(MineCellPartitionList::new, MineCellPartitionList::partitions);

    public boolean isEmpty() {
        return partitions == null || partitions.isEmpty();
    }

    public int size() {
        return partitions == null ? 0 : partitions.size();
    }

    public int getTotalPercent() {
        if (partitions == null) return 0;
        int total = 0;
        for (MineCellPartition p : partitions) {
            total += p.percent();
        }
        return total;
    }

    public int getUnallocatedPercent() {
        return Math.max(0, 100 - getTotalPercent());
    }

    @Nullable
    public MineCellPartition getPartition(Item item) {
        if (partitions == null) return null;
        for (MineCellPartition p : partitions) {
            if (p.target() == item) {
                return p;
            }
        }
        return null;
    }

    public boolean contains(Item item) {
        return getPartition(item) != null;
    }

    public long getAllocatedByteLimit(MineCellPartition partition, long totalBytes) {
        if (partition == null || partition.percent() <= 0) {
            return 0;
        }
        return (totalBytes * partition.percent()) / 100L;
    }

    public MineCellPartitionList withUpdated(List<MineCellPartition> newPartitions) {
        return new MineCellPartitionList(new ArrayList<>(newPartitions));
    }
}
