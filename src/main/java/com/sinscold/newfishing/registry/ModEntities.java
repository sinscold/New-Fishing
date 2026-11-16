package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.entity.LavaFishingHook;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NewFishing.MODID);

    /** Лавовый поплавок: не горит, ловит только в лаве (см. LavaFishingHook). */
    public static final DeferredHolder<EntityType<?>, EntityType<LavaFishingHook>> LAVA_FISHING_HOOK =
            ENTITIES.register("lava_fishing_hook", () -> EntityType.Builder.<LavaFishingHook>of(LavaFishingHook::new, MobCategory.MISC)
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(5)
                    .build("lava_fishing_hook"));

    private ModEntities() {}
}
