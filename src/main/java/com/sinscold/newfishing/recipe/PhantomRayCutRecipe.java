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

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PHANTOM_RAY_CUT.get();
    }
}
