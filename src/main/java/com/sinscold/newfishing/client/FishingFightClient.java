package com.sinscold.newfishing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.entity.FishFight;
import com.sinscold.newfishing.entity.NetheriteFishingHook;
import com.sinscold.newfishing.network.FightInputPayload;
import com.sinscold.newfishing.registry.ModSounds;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Клиентская часть мини-игры вытаскивания (сервер — FishFight в NetheriteFishingHook).
 * Пока рыба на крючке: камера следит за поплавком, ходить можно (отошёл от места поклёвки дальше 5 блоков — бой отменяется на сервере), мышь водит удочку по экрану (правая половина —
 * удочка в правой руке, левая — как во второй руке), ЛКМ тянет, ПКМ отпускает. Натяжение лески слышно (скрип), шкал нет.
 * Активность проверяется раз в тик по полю player.fishing (дёшево); ввод шлётся на сервер только при изменении.
 */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class FishingFightClient {
    /** Чувствительность: сколько единиц экрана (−1…1) даёт один пиксель движения мыши. */
    private static final double MOUSE_SCALE = 0.0025;
    /** Насколько быстро камера доворачивает на поплавок за кадр (0…1). */
    private static final float CAMERA_FOLLOW = 0.25F;

    private static boolean active;
    /** Положение удочки на экране: x −1 (слева) … 1 (справа), y −1 (низ) … 1 (верх). */
    private static float rodX = 0.8F;
    private static float rodY;
    /** Плавный наклон удочки от ввода (тянешь — удочка задирается). */
    private static float pullAnim;
    private static int sentAction = -1;
    private static int soundCooldown;
    private static float sentRodX = Float.NaN;

    /** Идёт ли бой с рыбой у этого игрока (для миксина мыши, рендера лески и т.п.). */
    public static boolean isActive() {
        return active;
    }

    public static float rodX() {
        return rodX;
    }

    public static float rodY() {
        return rodY;
    }

    /** Движение мыши во время боя двигает удочку вместо камеры (MouseHandlerMixin). */
    public static void moveRod(double dx, double dy) {
        if (Minecraft.getInstance().screen != null) return;
        double sensitivity = Minecraft.getInstance().options.sensitivity().get() * 2.0;
        rodX = (float) Mth.clamp(rodX + dx * MOUSE_SCALE * sensitivity, -1.0, 1.0);
        rodY = (float) Mth.clamp(rodY - dy * MOUSE_SCALE * sensitivity, -1.0, 1.0);
    }

    private static NetheriteFishingHook fightingHook() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.fishing instanceof NetheriteFishingHook hook && hook.isFighting() ? hook : null;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        boolean now = fightingHook() != null;
        if (now != active) {
            active = now;
            sentAction = -1;
            sentRodX = Float.NaN;
            if (now) {
                rodX = 0.8F;
                rodY = 0.0F;
            }
        }
        if (!active) return;
        NetheriteFishingHook hook = fightingHook();
        if (hook != null) {
            playTensionSound(hook);
        }
        Minecraft mc = Minecraft.getInstance();
        boolean pull = mc.screen == null && mc.options.keyAttack.isDown();
        boolean release = mc.screen == null && mc.options.keyUse.isDown();
        int action = pull == release ? FishFight.INPUT_NONE : pull ? FishFight.INPUT_PULL : FishFight.INPUT_RELEASE;
        // положение удочки шлём грубо (шаг 0,1), чтобы не слать пакет на каждое движение мыши
        float roundedX = Math.round(rodX * 10.0F) / 10.0F;
        if (action != sentAction || roundedX != sentRodX) {
            sentAction = action;
            sentRodX = roundedX;
            PacketDistributor.sendToServer(new FightInputPayload((byte) action, roundedX));
        }
    }

    /** ЛКМ и ПКМ во время боя не бьют и не используют предметы: это «тянуть» и «отпускать». */
    @SubscribeEvent
    static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (active) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    /** Камера (взгляд игрока) плавно доворачивает на поплавок каждый кадр. */
    @SubscribeEvent
    static void onRenderFrame(RenderFrameEvent.Pre event) {
        if (!active) return;
        NetheriteFishingHook hook = fightingHook();
        LocalPlayer player = Minecraft.getInstance().player;
        if (hook == null || player == null) return;
        DeltaTracker delta = event.getPartialTick();
        float partial = delta.getGameTimeDeltaPartialTick(false);
        Vec3 eye = player.getEyePosition(partial);
        Vec3 to = hook.getPosition(partial).subtract(eye);
        double horizontal = Math.sqrt(to.x * to.x + to.z * to.z);
        float targetYaw = (float) (Mth.atan2(to.z, to.x) * (180.0 / Math.PI)) - 90.0F;
        float targetPitch = (float) -(Mth.atan2(to.y, horizontal) * (180.0 / Math.PI));
        float dYaw = Mth.wrapDegrees(targetYaw - player.getYRot()) * CAMERA_FOLLOW;
        float dPitch = (targetPitch - player.getXRot()) * CAMERA_FOLLOW;
        // как Entity.turn: двигаем и текущий, и прошлый угол, чтобы интерполяция не дёргалась
        player.setYRot(player.getYRot() + dYaw);
        player.yRotO += dYaw;
        player.setXRot(Mth.clamp(player.getXRot() + dPitch, -90.0F, 90.0F));
        player.xRotO = Mth.clamp(player.xRotO + dPitch, -90.0F, 90.0F);

        float target = Minecraft.getInstance().options.keyAttack.isDown() ? 1.0F
                : Minecraft.getInstance().options.keyUse.isDown() ? -1.0F : 0.0F;
        pullAnim += (target - pullAnim) * 0.2F;
    }

    /**
     * Удочка от первого лица рисуется там, куда её завела мышь: правая половина экрана — в правой руке (как обычно),
     * левая — в левой руке. Вторая рука в это время не рисуется.
     */
    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        ItemStack stack = event.getItemStack();
        boolean rodHand = stack.is(Items.FISHING_ROD)
                && (event.getHand() == InteractionHand.MAIN_HAND || !player.getMainHandItem().is(Items.FISHING_ROD));
        event.setCanceled(true);
        if (!rodHand) return;

        boolean right = rodX >= 0.0F;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        // ванильная рука стоит на (±0,56; −0,52; −0,72); удочка ездит по экрану вслед за мышью
        pose.translate(rodX * 0.7F, -0.52F + rodY * 0.25F, -0.72F);
        pose.mulPose(Axis.ZP.rotationDegrees(-rodX * 8.0F));
        pose.mulPose(Axis.XP.rotationDegrees(pullAnim * 12.0F));
        mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player, stack,
                right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                !right, pose, event.getMultiBufferSource(), event.getPackedLight());
        pose.popPose();
    }

    /**
     * Звук натяжения вместо шкал: от 0,15 натяжения леска скрипит, чем сильнее натяжение — тем чаще, громче и выше.
     * Звук {@code newfishing:line_tension} (пока ванильный скрип натяжения арбалета, Синс может заменить своим .ogg).
     */
    private static void playTensionSound(NetheriteFishingHook hook) {
        float tension = hook.fightTension();
        if (tension < 0.15F) {
            soundCooldown = 0;
            return;
        }
        if (--soundCooldown > 0) return;
        soundCooldown = Math.round(Mth.lerp(tension, 16.0F, 3.0F));
        float pitch = 0.7F + tension * 0.9F;
        float volume = 0.25F + tension * 0.5F;
        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(ModSounds.LINE_TENSION.get(), pitch, volume));
    }

    private FishingFightClient() {}
}
