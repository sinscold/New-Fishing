package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.world.BaitDirtFeature;
import com.sinscold.newfishing.world.EatenLogFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Генерация наживки: подозрительная земля и выеденные брёвна (настройка в data/newfishing/worldgen и neoforge/biome_modifier). */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, NewFishing.MODID);

    public static final DeferredHolder<Feature<?>, BaitDirtFeature> BAIT_DIRT = FEATURES.register("bait_dirt", BaitDirtFeature::new);
    public static final DeferredHolder<Feature<?>, EatenLogFeature> EATEN_LOG = FEATURES.register("eaten_log", EatenLogFeature::new);

    private ModFeatures() {}
}
