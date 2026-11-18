package com.sinscold.newfishing.item;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModPotions;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

/** Варка зелья возвращения: бутылка неловкого зелья + потроха пустотной щуки. */
public class ReturnBrewingRecipe implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack input) {
        if (!input.is(Items.POTION)) return false;
        PotionContents contents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().isPresent() && contents.potion().get().value() == Potions.AWKWARD.value();
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(ModItems.VOID_PIKE_GUTS.get());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (this.isInput(input) && this.isIngredient(ingredient)) {
            return PotionContents.createItemStack(Items.POTION, ModPotions.RETURN);
        }
        return ItemStack.EMPTY;
    }
}
