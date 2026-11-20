package com.sinscold.newfishing.mixin;

import com.sinscold.newfishing.registry.ModPotions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;

/** Зелье возвращения нельзя улучшать и превращать во взрывное или туманное: варка с ним запрещена. */
@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true, require = 0)
    private void newfishing$noReturnUpgrades(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        PotionContents contents = input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents.potion().isPresent() && contents.potion().get().value() == ModPotions.RETURN.get()) {
            cir.setReturnValue(false);
        }
    }
}
