package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.recipe.GoldThreadRecipe;
import com.sinscold.newfishing.recipe.NetheriteLineRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NewFishing.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<GoldThreadRecipe>> GOLD_THREAD =
            SERIALIZERS.register("gold_thread", () -> new SimpleCraftingRecipeSerializer<>(GoldThreadRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<NetheriteLineRecipe>> NETHERITE_LINE =
            SERIALIZERS.register("netherite_line", () -> new SimpleCraftingRecipeSerializer<>(NetheriteLineRecipe::new));

    private ModRecipeSerializers() {}
}
