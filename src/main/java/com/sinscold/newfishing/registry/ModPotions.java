package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.alchemy.Potions;

@EventBusSubscriber(modid = NewFishing.MODID)
public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, NewFishing.MODID);

    /** Зелье возвращения (бутылка): при выпивании возвращает на точку возрождения. */
    public static final DeferredHolder<Potion, Potion> RETURN =
            POTIONS.register("return", () -> new Potion("return", new MobEffectInstance(ModEffects.RETURN, 1)));

    /** Варка: неловкое зелье + потроха пустотной щуки. */
    @SubscribeEvent
    static void onRegisterBrewing(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, ModItems.VOID_PIKE_GUTS.get(), RETURN);
    }

    private ModPotions() {}
}
