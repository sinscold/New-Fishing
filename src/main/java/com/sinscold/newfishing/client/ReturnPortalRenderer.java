package com.sinscold.newfishing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.entity.ReturnPortal;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Портал возвращения: анимированный спрайт Синса (textures/entity/return_portal.png, кадры 16×16 друг под другом),
 * плоскость поворачивается к камере вокруг вертикали, светится в темноте. Кадр выбирается по возрасту сущности.
 */
public class ReturnPortalRenderer extends EntityRenderer<ReturnPortal> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/entity/return_portal.png");
    /** Число кадров в полосе (высота / ширина картинки) и тиков на кадр. */
    private static final int FRAMES = 32;
    private static final int TICKS_PER_FRAME = 2;
    /** Размер портала в блоках (подбирается на глаз). */
    private static final float SIZE = 2.0F;

    public ReturnPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ReturnPortal entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, SIZE / 2.0F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.camera.getYRot()));
        poseStack.scale(SIZE, SIZE, SIZE);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        int frame = (entity.tickCount / TICKS_PER_FRAME) % FRAMES;
        float v0 = frame / (float) FRAMES;
        float v1 = (frame + 1) / (float) FRAMES;
        vertex(consumer, pose, -0.5F, -0.5F, 0.0F, v1);
        vertex(consumer, pose, 0.5F, -0.5F, 1.0F, v1);
        vertex(consumer, pose, 0.5F, 0.5F, 1.0F, v0);
        vertex(consumer, pose, -0.5F, 0.5F, 0.0F, v0);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ReturnPortal entity) {
        return TEXTURE;
    }
}
