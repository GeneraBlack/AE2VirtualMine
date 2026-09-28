package de.project.ae2virtualmine.cell.partition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record MineCellPartition(
        Item target,
        int percent,
        boolean voidSecondary
) {
    public static final Codec<MineCellPartition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("target").forGetter(MineCellPartition::target),
            Codec.INT.fieldOf("percent").forGetter(MineCellPartition::percent),
            Codec.BOOL.optionalFieldOf("void_secondary", false).forGetter(MineCellPartition::voidSecondary)
    ).apply(instance, MineCellPartition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MineCellPartition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM), MineCellPartition::target,
            ByteBufCodecs.VAR_INT, MineCellPartition::percent,
            ByteBufCodecs.BOOL, MineCellPartition::voidSecondary,
            MineCellPartition::new
    );
}
