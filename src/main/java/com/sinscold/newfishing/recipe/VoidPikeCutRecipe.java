package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Пустотная щука + нож = филе пустотной щуки; потроха возвращаются в слот щуки. */
public class VoidPikeCutRecipe extends AbstractKnifeCutRecipe {
    public VoidPikeCutRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    protected boolean isFish(ItemStack stack) {
        return stack.is(ModItems.VOID_PIKE.get());
    }

    @Override
    protected ItemStack leftover() {
        return new ItemStack(ModItems.VOID_PIKE_GUTS.get());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.VOID_PIKE_FILLET.get());
    }

    /** Не «особый» рецепт: JEI и книга рецептов показывают его по ингредиентам и результату. */
    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
        return net.minecraft.core.NonNullList.of(net.minecraft.world.item.crafting.Ingredient.EMPTY, net.minecraft.world.item.crafting.Ingredient.of(ModItems.VOID_PIKE.get()), net.minecraft.world.item.crafting.Ingredient.of(com.sinscold.newfishing.registry.ModTags.KNIVES));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.VOID_PIKE_FILLET.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.VOID_PIKE_CUT.get();
    }
}
