package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NewFishing.MODID);

    /** Что вставлено в удочку: леска и крючок. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RodMods>> ROD_MODS =
            COMPONENTS.register("rod_mods", () -> DataComponentType.<RodMods>builder()
                    .persistent(RodMods.CODEC)
                    .networkSynchronized(RodMods.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    private ModDataComponents() {}
}
