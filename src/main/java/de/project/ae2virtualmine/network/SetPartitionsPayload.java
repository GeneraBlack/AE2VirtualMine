package de.project.ae2virtualmine.network;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.util.VirtualCellAdapter.UniversalPartition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SetPartitionsPayload(List<UniversalPartition> partitions) implements CustomPacketPayload {
    public static final Type<SetPartitionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AE2VirtualMine.MODID, "set_partitions"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPartitionsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UniversalPartition.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    SetPartitionsPayload::partitions,
                    SetPartitionsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
