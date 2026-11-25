package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.loot.LimitEnchantmentsFunction;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, NewFishing.MODID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<LimitEnchantmentsFunction>> LIMIT_ENCHANTMENTS =
            FUNCTIONS.register("limit_enchantments", () -> new LootItemFunctionType<>(LimitEnchantmentsFunction.CODEC));

    private ModLootFunctions() {}
}
