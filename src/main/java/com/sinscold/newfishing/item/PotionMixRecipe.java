package com.sinscold.newfishing.item;

import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

/** Варка: бутылка с зельем {@code from} + ингредиент → бутылка с зельем {@code to} (обычная бутылка, взрывные и туманные делает ваниль). */
public record PotionMixRecipe(Holder<Potion> from, Predicate<ItemStack> ingredient, Holder<Potion> to) implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack input) {
        if (!input.is(Items.POTION)) return false;
        PotionContents contents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().isPresent() && contents.potion().get().value() == this.from.value();
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return this.ingredient.test(stack);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack stack) {
        return this.isInput(input) && this.isIngredient(stack) ? PotionContents.createItemStack(Items.POTION, this.to) : ItemStack.EMPTY;
    }
}
