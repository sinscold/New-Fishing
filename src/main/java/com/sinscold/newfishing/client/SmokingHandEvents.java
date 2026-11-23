package com.sinscold.newfishing.client;

import com.mojang.math.Axis;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.registry.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * От первого лица во время курения сигарета плавно переезжает в центр экрана тем же сдвигом, которым ваниль ставит заряженный арбалет
 * (-0,641864 по горизонтали и поворот на 10°). Сдвиг применяется к позе перед отрисовкой основной руки и снимается перед
 * отрисовкой второй, чтобы зажигалка осталась на месте.
 */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class SmokingHandEvents {
    private static final float SHIFT_X = -0.641864F;
    private static final float TURN = 10.0F;
    private static float appliedFactor = 0.0F;

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            appliedFactor = 0.0F;
            if (event.getItemStack().is(ModItems.CIGARETTE.get()) && player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                float factor = Mth.clamp((player.getTicksUsingItem() + event.getPartialTick()) / 8.0F, 0.0F, 1.0F);
                event.getPoseStack().translate(SHIFT_X * factor, 0.0F, 0.0F);
                event.getPoseStack().mulPose(Axis.YP.rotationDegrees(TURN * factor));
                appliedFactor = factor;
            }
        } else if (appliedFactor != 0.0F) {
            event.getPoseStack().mulPose(Axis.YP.rotationDegrees(-TURN * appliedFactor));
            event.getPoseStack().translate(-SHIFT_X * appliedFactor, 0.0F, 0.0F);
            appliedFactor = 0.0F;
        }
    }

    private SmokingHandEvents() {}
}
