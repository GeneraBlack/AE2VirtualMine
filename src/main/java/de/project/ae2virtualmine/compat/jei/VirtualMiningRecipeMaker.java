package de.project.ae2virtualmine.compat.jei;

import de.project.ae2virtualmine.cell.MineCellTier;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.recipe.MineDropEntry;
import de.project.ae2virtualmine.recipe.MineDropRecipe;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class VirtualMiningRecipeMaker {

    public static List<VirtualMiningRecipe> createRecipes() {
        Map<Item, VirtualMiningRecipe> recipeMap = new LinkedHashMap<>();

        int cycleTicks = VirtualMineConfig.SPEC.isLoaded() ? VirtualMineConfig.BASE_TICK_INTERVAL.get() : 100;
        double energyPerDrop = VirtualMineConfig.SPEC.isLoaded() ? VirtualMineConfig.ENERGY_PER_DROP.get() : 50.0;

        // 1. Built-in drop tables
        boolean builtinEnabled = !VirtualMineConfig.SPEC.isLoaded() || VirtualMineConfig.ENABLE_BUILTIN_DROPS.get();
        if (builtinEnabled) {
            for (Map.Entry<Item, List<MineDropEntry>> entry : MineDropRegistry.getBuiltinDrops().entrySet()) {
                Item target = entry.getKey();
                List<MineDropEntry> drops = entry.getValue();
                MineCellTier minTier = MineCellTier.TIER_1K;
                if (target == Items.ANCIENT_DEBRIS) {
                    minTier = MineCellTier.TIER_4K;
                }
                recipeMap.put(target, new VirtualMiningRecipe(target, drops, minTier, cycleTicks, energyPerDrop));
            }
        }

        // 2. Datapack custom recipes
        RecipeManager recipeManager = getRecipeManager();
        if (recipeManager != null) {
            for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
                if (holder.value() instanceof MineDropRecipe r) {
                    MineCellTier minTier = getTierFromNumber(r.minTier());
                    for (ItemStack stack : r.target().getItems()) {
                        Item targetItem = stack.getItem();
                        recipeMap.put(targetItem, new VirtualMiningRecipe(targetItem, r.drops(), minTier, cycleTicks, energyPerDrop));
                    }
                }
            }
        }

        // 3. Dynamic fallback for modded ores (if enabled)
        boolean dynamicFallback = !VirtualMineConfig.SPEC.isLoaded() || VirtualMineConfig.ENABLE_DYNAMIC_FALLBACK.get();
        if (dynamicFallback) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (!recipeMap.containsKey(item) && MineDropRegistry.isOreOrMiningResource(item)) {
                    List<MineDropEntry> drops = MineDropRegistry.getDropEntries(item, null, null);
                    if (!drops.isEmpty()) {
                        recipeMap.put(item, new VirtualMiningRecipe(item, drops, MineCellTier.TIER_1K, cycleTicks, energyPerDrop));
                    }
                }
            }
        }

        return new ArrayList<>(recipeMap.values());
    }

    private static RecipeManager getRecipeManager() {
        try {
            if (ServerLifecycleHooks.getCurrentServer() != null) {
                return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
            }
        } catch (Throwable ignored) {
        }
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.level != null && mc.level.getServer() != null) {
                return mc.level.getServer().getRecipeManager();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static MineCellTier getTierFromNumber(int minTier) {
        int index = Math.max(0, Math.min(minTier - 1, MineCellTier.values().length - 1));
        return MineCellTier.values()[index];
    }
}
