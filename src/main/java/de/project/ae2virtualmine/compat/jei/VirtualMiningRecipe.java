package de.project.ae2virtualmine.compat.jei;

import de.project.ae2virtualmine.cell.MineCellTier;
import de.project.ae2virtualmine.recipe.MineDropEntry;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record VirtualMiningRecipe(
        Item target,
        List<MineDropEntry> drops,
        MineCellTier minTier,
        int cycleTicks,
        double energyPerDrop
) {
    @Nullable
    public MineDropEntry getPrimaryDrop() {
        return drops.isEmpty() ? null : drops.get(0);
    }

    public List<MineDropEntry> getSecondaryDrops() {
        return drops.size() > 1 ? drops.subList(1, drops.size()) : List.of();
    }
}
