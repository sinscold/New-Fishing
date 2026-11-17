package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** 4 мембраны фантома + любой топор (бесформенно) = фантомная леска; топор теряет 1 прочность и остаётся. */
public class PhantomLineRecipe extends CustomRecipe {
    public PhantomLineRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int membranes = 0;
        int axes = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.PHANTOM_MEMBRANE)) membranes++;
            else if (stack.getItem() instanceof AxeItem) axes++;
            else return false;
        }
        return membranes == 4 && axes == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.PHANTOM_LINE.get());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof AxeItem) {
                ItemStack damaged = stack.copy();
                damaged.setDamageValue(damaged.getDamageValue() + 1);
                remaining.set(i, damaged.getDamageValue() >= damaged.getMaxDamage() ? ItemStack.EMPTY : damaged);
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 5;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PHANTOM_LINE.get();
    }
}
