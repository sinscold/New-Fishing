package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, NewFishing.MODID);

    /** Звук порванной лески (assets/newfishing/sounds/line_torn.ogg). */
    public static final DeferredHolder<SoundEvent, SoundEvent> LINE_TORN = SOUNDS.register("line_torn",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "line_torn")));

    /** Блоп: поплавок попал в лаву. */
    public static final DeferredHolder<SoundEvent, SoundEvent> LAVA_BLOP = SOUNDS.register("lava_blop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "lava_blop")));
    /** Булб: клёв в лаве. */
    public static final DeferredHolder<SoundEvent, SoundEvent> LAVA_BULB = SOUNDS.register("lava_bulb",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "lava_bulb")));

    /** Попытка открыть запертый ящик. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CRATE_LOCKED = SOUNDS.register("crate_locked",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "crate_locked")));

    private ModSounds() {}
}
