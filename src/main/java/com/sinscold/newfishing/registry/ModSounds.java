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

    private ModSounds() {}
}
