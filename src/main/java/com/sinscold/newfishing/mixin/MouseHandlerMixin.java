package com.sinscold.newfishing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sinscold.newfishing.client.FishingFightClient;

import net.minecraft.client.MouseHandler;

/**
 * Во время вытаскивания рыбы мышь не крутит камеру (она следит за поплавком), а водит удочку по экрану.
 * Только клиент (секция "client" в newfishing.mixins.json).
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void newfishing$moveRod(CallbackInfo ci) {
        if (!FishingFightClient.isActive()) return;
        FishingFightClient.moveRod(this.accumulatedDX, this.accumulatedDY);
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
        ci.cancel();
    }
}
