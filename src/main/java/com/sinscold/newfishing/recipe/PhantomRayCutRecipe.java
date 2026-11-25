package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Фантомный скат + нож = мембраны фантома. */
public class PhantomRayCutRecipe extends AbstractKnifeCutRecipe {
    /** Сколько мембран даёт один скат (подбирается). */
    public static final int MEMBRANES = 2;

    public PhantomRayCutRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    protected boolean isFish(ItemStack stack) {
        return stack.is(ModItems.PHANTOM_RAY.get());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(Items.PHANTOM_MEMBRANE, MEMBRANES);
    }

    /** Не «особый» рецепт: JEI и книга рецептов показывают его по ингредиентам и результату. */
    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
        return net.minecraft.core.NonNullList.of(net.minecraft.world.item.crafting.Ingredient.EMPTY, net.minecraft.world.item.crafting.Ingredient.of(ModItems.PHANTOM_RAY.get()), net.minecraft.world.item.crafting.Ingredient.of(com.sinscold.newfishing.registry.ModTags.KNIVES));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(Items.PHANTOM_MEMBRANE, MEMBRANES);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PHANTOM_RAY_CUT.get();
    }
}
