package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModTags;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

/** Разделка рыбы ножом (бесформенно): рыба + нож из тега newfishing:knives. Нож теряет 1 прочность и остаётся. */
public abstract class AbstractKnifeCutRecipe extends CustomRecipe {
    protected AbstractKnifeCutRecipe(CraftingBookCategory category) {
        super(category);
    }

    protected abstract boolean isFish(ItemStack stack);

    /** Что остаётся в слоте рыбы (потроха и т.п.). */
    protected ItemStack leftover() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int fish = 0;
        int knives = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (isFish(stack)) fish++;
            else if (stack.is(ModTags.KNIVES)) knives++;
            else return false;
        }
        return fish == 1 && knives == 1;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (isFish(stack)) {
                remaining.set(i, leftover());
            } else if (stack.is(ModTags.KNIVES)) {
                ItemStack damaged = stack.copy();
                if (damaged.isDamageableItem()) {
                    damaged.setDamageValue(damaged.getDamageValue() + 1);
                    remaining.set(i, damaged.getDamageValue() >= damaged.getMaxDamage() ? ItemStack.EMPTY : damaged);
                } else {
                    remaining.set(i, damaged);
                }
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}
