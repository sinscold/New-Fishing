package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Верстак 3×3: незеритовая пыль в центре и 4 золотые нити в любых из оставшихся 8 слотов = незеритовая леска. */
public class NetheriteLineRecipe extends CustomRecipe {
    public NetheriteLineRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) return false;
        if (!input.getItem(4).is(ModItems.NETHERITE_DUST.get())) return false;
        int strings = 0;
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.GOLD_STRING.get())) strings++;
            else return false;
        }
        return strings == 4;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.NETHERITE_LINE.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    /** Не «особый» рецепт: JEI и книга рецептов показывают его по ингредиентам и результату. */
    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
        // для JEI: пыль в центре, нити крестом (в верстаке нити можно класть в любые 4 слота)
        net.minecraft.world.item.crafting.Ingredient E = net.minecraft.world.item.crafting.Ingredient.EMPTY;
        net.minecraft.world.item.crafting.Ingredient S = net.minecraft.world.item.crafting.Ingredient.of(ModItems.GOLD_STRING.get());
        return net.minecraft.core.NonNullList.of(net.minecraft.world.item.crafting.Ingredient.EMPTY, E, S, E, S, net.minecraft.world.item.crafting.Ingredient.of(ModItems.NETHERITE_DUST.get()), S, E, S, E);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.NETHERITE_LINE.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NETHERITE_LINE.get();
    }
}
