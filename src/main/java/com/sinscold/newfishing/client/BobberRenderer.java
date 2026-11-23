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
    /** Размер «пикселя» лески (блоков) и её текстура (один белый пиксель, цвет задаётся вершинами). */
    private static final float PIXEL = 1.0F / 16.0F;
    private static final ResourceLocation LINE = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/entity/bobber/line.png");

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
        BobberGeometry.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity))), packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        // леска от руки (кончика удочки) к поплавку
        float attack = player.getAttackAnim(partialTicks);
        float swing = Mth.sin(Mth.sqrt(attack) * (float) Math.PI);
        Vec3 hand = this.handPos(player, swing, partialTicks);
        Vec3 hook = entity.getPosition(partialTicks).add(0.0, 0.25, 0.0);
        float dx = (float) (hand.x - hook.x);
        float dy = (float) (hand.y - hook.y);
        float dz = (float) (hand.z - hook.z);
        Vec3 cameraRel = this.entityRenderDispatcher.camera.getPosition().subtract(entity.getPosition(partialTicks));
        this.renderPixelLine(poseStack, buffer, dx, dy, dz, cameraRel, packedLight, this.lineColor(entity));
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private int lineColor(T entity) {
        int kind = entity instanceof NetheriteFishingHook hook ? hook.lineColorKind() : 0;
        return switch (kind) {
            case 1 -> 0xFF6B3F3F; // незеритовая
            case 2 -> 0xFF8FA0E0; // фантомная
            default -> 0xFF1A1A1A; // обычная
        };
    }

    /**
     * Леска из «пикселей» в 2D по сетке: вся леска лежит в одной плоскости, которая проходит через её концы (руку и поплавок)
     * и повёрнута вокруг этой линии к камере. В плоскости строится сетка (вертикаль мира и перпендикуляр к ней), кривая
     * провисания растеризуется по клеткам, получается ступенчатая лесенка. Все квадратики копланарны, поэтому нет
     * разрывов от разной глубины. Оттенок слегка меняется от клетки к клетке.
     */
    private void renderPixelLine(PoseStack poseStack, MultiBufferSource buffer, float dx, float dy, float dz, Vec3 camRel, int light, int color) {
        org.joml.Vector3f chord = new org.joml.Vector3f(dx, dy, dz);
        float length = chord.length();
        if (length < 1.0E-3F) {
            return;
        }
        chord.div(length);
        float originY = 0.25F;
        // нормаль плоскости: направление на камеру без составляющей вдоль лески
        org.joml.Vector3f toCamera = new org.joml.Vector3f((float) camRel.x - dx * 0.5F, (float) camRel.y - originY - dy * 0.5F, (float) camRel.z - dz * 0.5F);
        org.joml.Vector3f normal = new org.joml.Vector3f(toCamera).sub(new org.joml.Vector3f(chord).mul(toCamera.dot(chord)));
        if (normal.lengthSquared() < 1.0E-6F) {
            normal.set(0.0F, 0.0F, 1.0F).sub(new org.joml.Vector3f(chord).mul(chord.z));
            if (normal.lengthSquared() < 1.0E-6F) {
                normal.set(1.0F, 0.0F, 0.0F);
            }
        }
        normal.normalize();
        // ось «вверх» в плоскости: вертикаль мира, спроецированная на плоскость
        org.joml.Vector3f axisB = new org.joml.Vector3f(0.0F, 1.0F, 0.0F).sub(new org.joml.Vector3f(normal).mul(normal.y));
        if (axisB.lengthSquared() < 1.0E-6F) {
            axisB.set(chord);
        }
        axisB.normalize();
        org.joml.Vector3f axisA = new org.joml.Vector3f(axisB).cross(normal).normalize();
        org.joml.Vector3f halfA = new org.joml.Vector3f(axisA).mul(PIXEL * 0.5F);
        org.joml.Vector3f halfB = new org.joml.Vector3f(axisB).mul(PIXEL * 0.5F);

        int samples = Mth.clamp((int) (length / PIXEL) * 3, 24, 1500);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(LINE));
        PoseStack.Pose pose = poseStack.last();
        int lastA = Integer.MIN_VALUE;
        int lastB = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i <= samples; i++) {
            float t = (float) i / (float) samples;
            float px = dx * t;
            float py = dy * (t * t + t) * 0.5F;
            float pz = dz * t;
            int ca = Math.round((px * axisA.x + py * axisA.y + pz * axisA.z) / PIXEL);
            int cb = Math.round((px * axisB.x + py * axisB.y + pz * axisB.z) / PIXEL);
            if (ca == lastA && cb == lastB) {
                continue;
            }
            lastA = ca;
            lastB = cb;
            float a = ca * PIXEL;
            float b = cb * PIXEL;
            float x = axisA.x * a + axisB.x * b;
            float y = axisA.y * a + axisB.y * b + originY;
            float z = axisA.z * a + axisB.z * b;
            quad(consumer, pose, x, y, z, halfA, halfB, light, shade(color, index++));
        }
    }

    /** Лёгкая вариация яркости: три оттенка по кругу. */
    private static int shade(int color, int index) {
        float k = switch (index % 3) {
            case 0 -> 1.0F;
            case 1 -> 0.85F;
            default -> 1.12F;
        };
        int r = Mth.clamp((int) (((color >> 16) & 0xFF) * k), 0, 255);
        int g = Mth.clamp((int) (((color >> 8) & 0xFF) * k), 0, 255);
        int b = Mth.clamp((int) ((color & 0xFF) * k), 0, 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
                             org.joml.Vector3f right, org.joml.Vector3f up, int light, int color) {
        float[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        for (float[] c : corners) {
            consumer.addVertex(pose, x + right.x * c[0] + up.x * c[1], y + right.y * c[0] + up.y * c[1], z + right.z * c[0] + up.z * c[1])
                    .setColor(color)
                    .setUv(0.5F, 0.5F)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0.0F, 1.0F, 0.0F);
        }
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
}
