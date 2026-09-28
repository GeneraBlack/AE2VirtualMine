package de.project.ae2virtualmine.compat.jei;

import de.project.ae2virtualmine.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class VirtualMineJeiPlugin implements IModPlugin {

    public static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath("ae2virtualmine", "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new VirtualMiningCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(VirtualMiningCategory.RECIPE_TYPE, VirtualMiningRecipeMaker.createRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(VirtualMiningCategory.RECIPE_TYPE,
                ModItems.MINE_CELL_1K.get(),
                ModItems.MINE_CELL_4K.get(),
                ModItems.MINE_CELL_16K.get(),
                ModItems.MINE_CELL_64K.get(),
                ModItems.MINE_CELL_256K.get()
        );
    }
}
