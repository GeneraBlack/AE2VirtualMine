package de.project.ae2virtualmine.compat.jei;

import de.project.ae2virtualmine.recipe.MineDropEntry;
import de.project.ae2virtualmine.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class VirtualMiningCategory implements IRecipeCategory<VirtualMiningRecipe> {

    public static final RecipeType<VirtualMiningRecipe> RECIPE_TYPE =
            RecipeType.create("ae2virtualmine", "mining", VirtualMiningRecipe.class);

    private final IDrawable icon;

    public VirtualMiningCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.MINE_CELL_1K.get()));
    }

    @Override
    public RecipeType<VirtualMiningRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ae2virtualmine.category.mining");
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VirtualMiningRecipe recipe, IFocusGroup focuses) {
        // Target slot (input)
        builder.addInputSlot(8, 14)
                .addItemStack(new ItemStack(recipe.target()))
                .setStandardSlotBackground();

        // Primary drop slot (output)
        MineDropEntry primary = recipe.getPrimaryDrop();
        if (primary != null) {
            builder.addOutputSlot(64, 14)
                    .addItemStack(primary.createStack())
                    .setOutputSlotBackground()
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.translatable("jei.ae2virtualmine.primary_drop").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                        tooltip.add(Component.translatable("jei.ae2virtualmine.drop_weight", primary.weight() + "%").withStyle(ChatFormatting.GRAY));
                        if (primary.maxCount() > primary.minCount()) {
                            tooltip.add(Component.translatable("jei.ae2virtualmine.drop_amount_range", primary.minCount(), primary.maxCount()).withStyle(ChatFormatting.GRAY));
                        } else {
                            tooltip.add(Component.translatable("jei.ae2virtualmine.drop_amount", primary.minCount()).withStyle(ChatFormatting.GRAY));
                        }
                    });
        }

        // Secondary drop slots (up to 3)
        List<MineDropEntry> secondaries = recipe.getSecondaryDrops();
        for (int i = 0; i < secondaries.size() && i < 3; i++) {
            MineDropEntry sec = secondaries.get(i);
            int slotX = 90 + (i * 24);
            builder.addOutputSlot(slotX, 14)
                    .addItemStack(sec.createStack())
                    .setOutputSlotBackground()
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.translatable("jei.ae2virtualmine.secondary_drop").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                        tooltip.add(Component.translatable("jei.ae2virtualmine.drop_weight", sec.weight() + "%").withStyle(ChatFormatting.GRAY));
                        if (sec.maxCount() > sec.minCount()) {
                            tooltip.add(Component.translatable("jei.ae2virtualmine.drop_amount_range", sec.minCount(), sec.maxCount()).withStyle(ChatFormatting.GRAY));
                        } else {
                            tooltip.add(Component.translatable("jei.ae2virtualmine.drop_amount", sec.minCount()).withStyle(ChatFormatting.GRAY));
                        }
                        tooltip.add(Component.translatable("jei.ae2virtualmine.secondary_voidable").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                    });
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, VirtualMiningRecipe recipe, IFocusGroup focuses) {
        // Recipe arrow
        builder.addRecipeArrow().setPosition(36, 14);

        // Header: Required Cell Tier
        builder.addText(
                Component.translatable("jei.ae2virtualmine.min_tier", recipe.minTier().getTierName()).withStyle(ChatFormatting.DARK_AQUA),
                150, 9
        ).setPosition(8, 2);

        // Chances below slots
        MineDropEntry primary = recipe.getPrimaryDrop();
        if (primary != null) {
            builder.addText(Component.literal(primary.weight() + "%").withStyle(ChatFormatting.DARK_GREEN), 26, 9)
                    .setPosition(60, 34);
        }

        List<MineDropEntry> secondaries = recipe.getSecondaryDrops();
        for (int i = 0; i < secondaries.size() && i < 3; i++) {
            MineDropEntry sec = secondaries.get(i);
            int slotX = 90 + (i * 24);
            builder.addText(Component.literal(sec.weight() + "%").withStyle(ChatFormatting.GOLD), 26, 9)
                    .setPosition(slotX - 4, 34);
        }

        // Footer: Cycle time and Energy consumption
        builder.addText(
                Component.translatable("jei.ae2virtualmine.cycle_details",
                        String.format(Locale.ROOT, "%.1fs", recipe.cycleTicks() / 20.0),
                        (int) recipe.energyPerDrop()
                ).withStyle(ChatFormatting.DARK_GRAY),
                150, 9
        ).setPosition(8, 48);
    }
}
