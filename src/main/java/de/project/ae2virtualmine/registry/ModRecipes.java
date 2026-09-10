package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.recipe.MineDropRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AE2VirtualMine.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AE2VirtualMine.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MineDropRecipe>> MINE_DROP_TYPE =
            RECIPE_TYPES.register("mine_drop", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(AE2VirtualMine.MODID, "mine_drop")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MineDropRecipe>> MINE_DROP_SERIALIZER =
            SERIALIZERS.register("mine_drop", () -> new RecipeSerializer<>(MineDropRecipe.CODEC, MineDropRecipe.STREAM_CODEC));
}