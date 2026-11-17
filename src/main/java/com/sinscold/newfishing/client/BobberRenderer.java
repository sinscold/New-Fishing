package com.sinscold.newfishing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.entity.NetheriteFishingHook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Рендерер поплавка: 3D-модель из Blockbench (BobberGeometry) и леска. Заменяет ванильный для обычного и незеритового поплавков.
 * Начало лески — как в ванили, но сдвинуто к кончику увеличенной удочки (константы ниже подбираются на глаз).
 */
public class BobberRenderer<T extends FishingHook> extends EntityRenderer<T> {
    private static final ResourceLocation REGULAR = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/entity/bobber/fishing_hook.png");
    private static final ResourceLocation NETHERITE = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/entity/bobber/netherite_fishing_hook.png");

    /** Смещение начала лески к кончику удочки: от 3-го лица (вперёд по телу, вверх) и от 1-го (вперёд по взгляду, вверх). */
    private static final double THIRD_FORWARD = 0.30;
    private static final double THIRD_UP = 0.55;
    private static final double FIRST_FORWARD = 0.20;
    private static final double FIRST_UP = 0.12;

    public BobberRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity instanceof NetheriteFishingHook hook && hook.hasNetheriteHookTexture() ? NETHERITE : REGULAR;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player player = entity.getPlayerOwner();
        if (player == null) {
            return;
        }
        poseStack.pushPose();
        // сам поплавок
        poseStack.pushPose();
        poseStack.translate(0.0F, -0.03F, 0.0F);
        BobberGeometry.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity))), packedLight, 0);
        poseStack.popPose();

        // леска от руки (кончика удочки) к поплавку
        float attack = player.getAttackAnim(partialTicks);
        float swing = Mth.sin(Mth.sqrt(attack) * (float) Math.PI);
        Vec3 hand = this.handPos(player, swing, partialTicks);
        Vec3 hook = entity.getPosition(partialTicks).add(0.0, 0.25, 0.0);
        float dx = (float) (hand.x - hook.x);
        float dy = (float) (hand.y - hook.y);
        float dz = (float) (hand.z - hook.z);
        VertexConsumer lines = buffer.getBuffer(RenderType.lineStrip());
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i <= 16; i++) {
            stringVertex(dx, dy, dz, lines, pose, fraction(i, 16), fraction(i + 1, 16));
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private Vec3 handPos(Player player, float swing, float partialTicks) {
        int side = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
            side = -side;
        }
        Minecraft mc = Minecraft.getInstance();
        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == mc.player) {
            double fovScale = 960.0 / (double) this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 offset = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) side * 0.525F, -0.1F)
                    .scale(fovScale).yRot(swing * 0.5F).xRot(-swing * 0.7F);
            var look = this.entityRenderDispatcher.camera.getLookVector();
            var up = this.entityRenderDispatcher.camera.getUpVector();
            return player.getEyePosition(partialTicks).add(offset).add(
                    look.x() * FIRST_FORWARD + up.x() * FIRST_UP,
                    look.y() * FIRST_FORWARD + up.y() * FIRST_UP,
                    look.z() * FIRST_FORWARD + up.z() * FIRST_UP);
        }
        float bodyYaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
        double sin = Mth.sin(bodyYaw);
        double cos = Mth.cos(bodyYaw);
        float scale = player.getScale();
        double sideOffset = (double) side * 0.35 * scale;
        double forward = 0.8 * scale;
        float crouch = player.isCrouching() ? -0.1875F : 0.0F;
        return player.getEyePosition(partialTicks).add(
                -cos * sideOffset - sin * forward - sin * THIRD_FORWARD,
                (double) crouch - 0.45 * scale + THIRD_UP,
                -sin * sideOffset + cos * forward + cos * THIRD_FORWARD);
    }

    private static float fraction(int value, int max) {
        return (float) value / (float) max;
    }

    private static void stringVertex(float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose, float fraction, float nextFraction) {
        float px = x * fraction;
        float py = y * (fraction * fraction + fraction) * 0.5F + 0.25F;
        float pz = z * fraction;
        float nx = x * nextFraction - px;
        float ny = y * (nextFraction * nextFraction + nextFraction) * 0.5F + 0.25F - py;
        float nz = z * nextFraction - pz;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= length;
        ny /= length;
        nz /= length;
        consumer.addVertex(pose, px, py, pz).setColor(-16777216).setNormal(pose, nx, ny, nz);
    }
}
