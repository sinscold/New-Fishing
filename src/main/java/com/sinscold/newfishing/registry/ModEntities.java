package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.entity.BlindEye;
import com.sinscold.newfishing.entity.NetheriteFishingHook;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NewFishing.MODID);

    /** Лавовый поплавок: не горит, ловит только в лаве (см. NetheriteFishingHook). */
    public static final DeferredHolder<EntityType<?>, EntityType<NetheriteFishingHook>> NETHERITE_FISHING_HOOK =
            ENTITIES.register("netherite_fishing_hook", () -> EntityType.Builder.<NetheriteFishingHook>of(NetheriteFishingHook::new, MobCategory.MISC)
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(5)
                    .build("netherite_fishing_hook"));

    /** Брошенное слепое око. */
    public static final DeferredHolder<EntityType<?>, EntityType<BlindEye>> BLIND_EYE =
            ENTITIES.register("blind_eye", () -> EntityType.Builder.<BlindEye>of(BlindEye::new, MobCategory.MISC)
                    .noSave()
                    .noSummon()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(4)
                    .build("blind_eye"));

    private ModEntities() {}
}
