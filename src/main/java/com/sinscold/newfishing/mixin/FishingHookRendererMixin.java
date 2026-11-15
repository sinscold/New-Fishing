package com.sinscold.newfishing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Сдвигает начало лески (позиция руки в ванильном рендерере) к кончику увеличенной удочки.
 * Числа подбираются на глаз: меняйте константы ниже.
 */
@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    /** От 3-го лица: вперёд по направлению тела и вверх (в блоках). */
    private static final double THIRD_FORWARD = 0.30;
    private static final double THIRD_UP = 0.55;
    /** От 1-го лица: вперёд по взгляду и вверх по экрану (в блоках). */
    private static final double FIRST_FORWARD = 0.20;
    private static final double FIRST_UP = 0.12;

    @Inject(method = "getPlayerHandPos", at = @At("RETURN"), cancellable = true, require = 0)
    private void newfishing$moveLineStart(CallbackInfoReturnable<Vec3> cir) {
        // аргументы метода не захватываем (так Mixin не сломается при отличии сигнатуры): владельца находим как ближайшего игрока к точке
        Minecraft mc = Minecraft.getInstance();
        Vec3 pos = cir.getReturnValue();
        if (mc.level == null || pos == null) return;
        Player player = null;
        double best = 9.0;
        for (Player candidate : mc.level.players()) {
            double dist = candidate.getEyePosition().distanceToSqr(pos);
            if (dist < best) {
                best = dist;
                player = candidate;
            }
        }
        if (player == null) return;
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            var look = mc.gameRenderer.getMainCamera().getLookVector();
            var up = mc.gameRenderer.getMainCamera().getUpVector();
            cir.setReturnValue(pos.add(
                    look.x() * FIRST_FORWARD + up.x() * FIRST_UP,
                    look.y() * FIRST_FORWARD + up.y() * FIRST_UP,
                    look.z() * FIRST_FORWARD + up.z() * FIRST_UP));
        } else {
            double yaw = Math.toRadians(player.yBodyRot);
            cir.setReturnValue(pos.add(-Math.sin(yaw) * THIRD_FORWARD, THIRD_UP, Math.cos(yaw) * THIRD_FORWARD));
        }
    }
}
