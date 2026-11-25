package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.recipe.CatfishCutRecipe;
import com.sinscold.newfishing.recipe.GoldStringRecipe;
import com.sinscold.newfishing.recipe.NetheriteDustRecipe;
import com.sinscold.newfishing.recipe.PhantomRayCutRecipe;
import com.sinscold.newfishing.recipe.VoidPikeCutRecipe;
import com.sinscold.newfishing.recipe.NetheriteLineRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NewFishing.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<GoldStringRecipe>> GOLD_STRING =
            SERIALIZERS.register("gold_string", () -> new SimpleCraftingRecipeSerializer<>(GoldStringRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<NetheriteLineRecipe>> NETHERITE_LINE =
            SERIALIZERS.register("netherite_line", () -> new SimpleCraftingRecipeSerializer<>(NetheriteLineRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<NetheriteDustRecipe>> NETHERITE_DUST =
            SERIALIZERS.register("netherite_dust", () -> new SimpleCraftingRecipeSerializer<>(NetheriteDustRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<CatfishCutRecipe>> CATFISH_CUT =
            SERIALIZERS.register("catfish_cut", () -> new SimpleCraftingRecipeSerializer<>(CatfishCutRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<PhantomRayCutRecipe>> PHANTOM_RAY_CUT =
            SERIALIZERS.register("phantom_ray_cut", () -> new SimpleCraftingRecipeSerializer<>(PhantomRayCutRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<VoidPikeCutRecipe>> VOID_PIKE_CUT =
            SERIALIZERS.register("void_pike_cut", () -> new SimpleCraftingRecipeSerializer<>(VoidPikeCutRecipe::new));

    private ModRecipeSerializers() {}
}
