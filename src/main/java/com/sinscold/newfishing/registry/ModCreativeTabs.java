package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewFishing.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newfishing"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.LAVACARP.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FISHING_LINE.get());
                        output.accept(ModItems.FISHING_HOOK.get());
                        output.accept(ModItems.NETHERITE_HOOK.get());
                        output.accept(ModItems.NETHERITE_LINE.get());
                        output.accept(ModItems.PHANTOM_LINE.get());
                        output.accept(ModItems.BLIND_EYE.get());
                        output.accept(ModItems.ENDER_HOOK.get());
                        output.accept(ModItems.PHANTOM_RAY.get());
                        output.accept(ModItems.VOID_PIKE.get());
                        output.accept(ModItems.VOID_PIKE_FILLET.get());
                        output.accept(ModItems.VOID_PIKE_GUTS.get());
                        output.accept(ModItems.GOLD_STRING.get());
                        output.accept(ModItems.NETHERITE_DUST.get());
                        output.accept(ModItems.LAVACARP.get());
                        output.accept(ModItems.OBSIDIAN_CATFISH.get());
                        output.accept(ModItems.OBSIDIAN_CATFISH_FILLET.get());
                        output.accept(ModItems.COOKED_OBSIDIAN_CATFISH_FILLET.get());
                        output.accept(ModItems.WATER_CRATE.get());
                        output.accept(ModItems.LAVA_CRATE.get());
                    }).build());

    private ModCreativeTabs() {}
}
