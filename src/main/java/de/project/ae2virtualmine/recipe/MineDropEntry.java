package de.project.ae2virtualmine.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record MineDropEntry(ItemStack item, int weight, int minCount, int maxCount) {
    public static final Codec<MineDropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(MineDropEntry::item),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(MineDropEntry::weight),
            Codec.INT.optionalFieldOf("min_count", 1).forGetter(MineDropEntry::minCount),
            Codec.INT.optionalFieldOf("max_count", 1).forGetter(MineDropEntry::maxCount)
    ).apply(instance, MineDropEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MineDropEntry> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, MineDropEntry::item,
            ByteBufCodecs.VAR_INT, MineDropEntry::weight,
            ByteBufCodecs.VAR_INT, MineDropEntry::minCount,
            ByteBufCodecs.VAR_INT, MineDropEntry::maxCount,
            MineDropEntry::new
    );
}
