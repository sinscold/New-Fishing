package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** Меняет ванильную удочку: прочность 500 и урон в руке 2. */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class FishingRodDefaults {
    public static final int MAX_DURABILITY = 500;

    @SubscribeEvent
    static void onModifyDefaults(ModifyDefaultComponentsEvent event) {
        event.modify(Items.FISHING_ROD, builder -> builder
                .set(DataComponents.MAX_DAMAGE, MAX_DURABILITY)
                .set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                        // база игрока 1 + 1 = 2 урона; id такие же, как у ванильного оружия (для подсказки)
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_damage"),
                                        1.0, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_speed"),
                                        -2.4, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build()));
    }

    private FishingRodDefaults() {}
}
