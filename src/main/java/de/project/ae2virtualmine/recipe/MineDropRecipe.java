package de.project.ae2virtualmine.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.project.ae2virtualmine.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public record MineDropRecipe(Ingredient target, int minTier, List<MineDropEntry> drops) implements Recipe<SingleRecipeInput> {

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return target.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        if (!drops.isEmpty()) {
            return drops.get(0).item().copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        if (!drops.isEmpty()) {
            return drops.get(0).item();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MINE_DROP_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MINE_DROP_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<MineDropRecipe> {
        public static final MapCodec<MineDropRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("target").forGetter(MineDropRecipe::target),
                Codec.INT.optionalFieldOf("min_tier", 1).forGetter(MineDropRecipe::minTier),
                MineDropEntry.CODEC.listOf().fieldOf("drops").forGetter(MineDropRecipe::drops)
        ).apply(instance, MineDropRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MineDropRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, MineDropRecipe::target,
                ByteBufCodecs.VAR_INT, MineDropRecipe::minTier,
                MineDropEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), MineDropRecipe::drops,
                MineDropRecipe::new
        );

        @Override
        public MapCodec<MineDropRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MineDropRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
