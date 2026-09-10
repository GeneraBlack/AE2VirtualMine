package de.project.ae2virtualmine.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.project.ae2virtualmine.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public record MineDropRecipe(Ingredient target, int minTier, List<MineDropEntry> drops) implements Recipe<SingleRecipeInput> {

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
    public boolean matches(SingleRecipeInput input, Level level) {
        return target.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        if (!drops.isEmpty()) {
            return drops.get(0).createStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.MINE_DROP_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.MINE_DROP_TYPE.get();
    }
}