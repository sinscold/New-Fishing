package com.sinscold.newfishing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sinscold.newfishing.item.RodRiding;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/** Лавоход слушается игрока и с удочкой, в слоте крючка которой лежит гриб на палке. */
@Mixin(Strider.class)
public abstract class StriderMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true, require = 0)
    private void newfishing$rodSteering(CallbackInfoReturnable<LivingEntity> cir) {
        Strider self = (Strider) (Object) this;
        if (self.isSaddled() && self.getFirstPassenger() instanceof Player player
                && RodRiding.holdsRidingStick(player, Items.WARPED_FUNGUS)) {
            cir.setReturnValue(player);
        }
    }
}
