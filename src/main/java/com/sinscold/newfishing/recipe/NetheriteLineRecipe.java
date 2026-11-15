package com.sinscold.newfishing.recipe;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
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
        int threads = 0;
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.GOLD_THREAD.get())) threads++;
            else return false;
        }
        return threads == 4;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.NETHERITE_LINE.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NETHERITE_LINE.get();
    }
}
