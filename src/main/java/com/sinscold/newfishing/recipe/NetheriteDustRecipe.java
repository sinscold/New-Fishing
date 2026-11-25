package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Незеритовый лом + алмаз (бесформенно) = незеритовая пыль; алмаз не расходуется. */
public class NetheriteDustRecipe extends CustomRecipe {
    public NetheriteDustRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int scrap = 0;
        int diamonds = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.NETHERITE_SCRAP)) scrap++;
            else if (stack.is(Items.DIAMOND)) diamonds++;
            else return false;
        }
        return scrap == 1 && diamonds == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.NETHERITE_DUST.get());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(Items.DIAMOND)) {
                remaining.set(i, new ItemStack(Items.DIAMOND));
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    /** Не «особый» рецепт: JEI и книга рецептов показывают его по ингредиентам и результату. */
    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
        return net.minecraft.core.NonNullList.of(net.minecraft.world.item.crafting.Ingredient.EMPTY, net.minecraft.world.item.crafting.Ingredient.of(Items.NETHERITE_SCRAP), net.minecraft.world.item.crafting.Ingredient.of(Items.DIAMOND));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.NETHERITE_DUST.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NETHERITE_DUST.get();
    }
}
