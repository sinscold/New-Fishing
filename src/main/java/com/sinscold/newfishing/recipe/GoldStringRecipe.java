package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Золотой слиток + топор (железный или лучше) = 4 золотые нити; топор теряет 1 прочность и остаётся. */
public class GoldStringRecipe extends CustomRecipe {
    public GoldStringRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static boolean isGoodAxe(ItemStack stack) {
        return stack.getItem() instanceof AxeItem axe && axe.getTier().getUses() >= Tiers.IRON.getUses();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int ingots = 0;
        int axes = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.GOLD_INGOT)) ingots++;
            else if (isGoodAxe(stack)) axes++;
            else return false;
        }
        return ingots == 1 && axes == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.GOLD_STRING.get(), 4);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (isGoodAxe(stack)) {
                ItemStack damaged = stack.copy();
                damaged.setDamageValue(damaged.getDamageValue() + 1);
                // сломался (прочность исчерпана) — предмет пропадает
                remaining.set(i, damaged.getDamageValue() >= damaged.getMaxDamage() ? ItemStack.EMPTY : damaged);
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.GOLD_STRING.get();
    }
}
