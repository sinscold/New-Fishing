package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = NewFishing.MODID)
public final class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, NewFishing.MODID);

    /** Зелье возвращения (бутылка): при выпивании возвращает на точку возрождения. */
    public static final DeferredHolder<Potion, Potion> RETURN =
            POTIONS.register("return", () -> new Potion("return", new MobEffectInstance(ModEffects.RETURN, 1)));

    /** Зелье Инферно: огнестойкость + поджог ударами. Длительность вдвое меньше обычных зелий: 2:00 и 4:00. */
    public static final DeferredHolder<Potion, Potion> INFERNO = POTIONS.register("inferno",
            () -> new Potion("inferno", new MobEffectInstance(ModEffects.INFERNO, 2400), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400)));
    public static final DeferredHolder<Potion, Potion> LONG_INFERNO = POTIONS.register("long_inferno",
            () -> new Potion("inferno", new MobEffectInstance(ModEffects.INFERNO, 4800), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800)));

    /** Варка: неловкое зелье + потроха пустотной щуки. */
    @SubscribeEvent
    static void onRegisterBrewing(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(new com.sinscold.newfishing.item.ReturnBrewingRecipe());
        // неловкое зелье + лавакарп = Инферно; Инферно + красная пыль = долгое Инферно
        event.getBuilder().addRecipe(new com.sinscold.newfishing.item.PotionMixRecipe(
                net.minecraft.world.item.alchemy.Potions.AWKWARD, st -> st.is(ModItems.LAVACARP.get()), INFERNO));
        event.getBuilder().addRecipe(new com.sinscold.newfishing.item.PotionMixRecipe(
                INFERNO, st -> st.is(net.minecraft.world.item.Items.REDSTONE), LONG_INFERNO));
    }

    private ModPotions() {}
}
