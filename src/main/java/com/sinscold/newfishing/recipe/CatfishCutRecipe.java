package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Разделка сома без Farmer's Delight: обсидиановый сом + кирка (алмазная или лучше), бесформенно.
 * Результат: филе сома; обсидиан возвращается в слот сома, кирка теряет 1 прочность и остаётся.
 */
public class CatfishCutRecipe extends CustomRecipe {
    public CatfishCutRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static boolean isGoodPickaxe(ItemStack stack) {
        return stack.getItem() instanceof PickaxeItem pick && pick.getTier().getUses() >= Tiers.DIAMOND.getUses();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int catfish = 0;
        int pickaxes = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.OBSIDIAN_CATFISH.get())) catfish++;
            else if (isGoodPickaxe(stack)) pickaxes++;
            else return false;
        }
        return catfish == 1 && pickaxes == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.OBSIDIAN_CATFISH_FILLET.get());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ModItems.OBSIDIAN_CATFISH.get())) {
                remaining.set(i, new ItemStack(Items.OBSIDIAN));
            } else if (isGoodPickaxe(stack)) {
                ItemStack damaged = stack.copy();
                damaged.setDamageValue(damaged.getDamageValue() + 1);
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
        return ModRecipeSerializers.CATFISH_CUT.get();
    }
}
