package de.project.ae2virtualmine.network;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.cell.partition.MineCellPartitionList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetPartitionsPayload(MineCellPartitionList partitions) implements CustomPacketPayload {
    public static final Type<SetPartitionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AE2VirtualMine.MODID, "set_partitions"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPartitionsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    MineCellPartitionList.STREAM_CODEC,
                    SetPartitionsPayload::partitions,
                    SetPartitionsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
