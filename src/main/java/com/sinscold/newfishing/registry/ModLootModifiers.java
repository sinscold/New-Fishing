package com.sinscold.newfishing.registry;

import com.mojang.serialization.MapCodec;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.loot.CrateReplacesTreasureModifier;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, NewFishing.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<CrateReplacesTreasureModifier>> CRATE_REPLACES_TREASURE =
            LOOT_MODIFIERS.register("crate_replaces_treasure", () -> CrateReplacesTreasureModifier.CODEC);

    private ModLootModifiers() {}
}
