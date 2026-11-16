package com.sinscold.newfishing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sinscold.newfishing.item.RodRiding;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/** Свинья слушается игрока и с удочкой, в слоте крючка которой лежит морковка на палке. */
@Mixin(Pig.class)
public abstract class PigMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true, require = 0)
    private void newfishing$rodSteering(CallbackInfoReturnable<LivingEntity> cir) {
        Pig self = (Pig) (Object) this;
        if (self.isSaddled() && self.getFirstPassenger() instanceof Player player
                && RodRiding.holdsRidingStick(player, Items.CARROT)) {
            cir.setReturnValue(player);
        }
    }
}
