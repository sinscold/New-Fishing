package com.sinscold.newfishing.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.registry.ModItems;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Сумка для наживки на поясе игрока (модель из Blockbench, models-src/bait_bag.bbmodel): широкий пояс и подсумок спереди слева.
 * Рисуется в системе координат тела (как ModelPart), поэтому наклоняется вместе с телом. Нужен только при установленном Curios.
 */
public final class BaitBagRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/entity/bait_bag.png");
    private static final float TEX = 32.0F;

    private record Quad(float[][] pos, float[][] uv, float[] normal) {}

    private static final List<Quad> QUADS = new ArrayList<>();

    static {
        // пояс: (-4,10,-1.95)…(4,13,2.05), раздутие 0,3
        cube(-4, 10, -1.95F, 4, 13, 2.05F, 0.3F, 0, 0, 0, 0,
                new float[]{4, 4, 12, 7}, new float[]{0, 4, 4, 7}, new float[]{16, 4, 24, 7},
                new float[]{12, 4, 16, 7}, new float[]{12, 4, 4, 0}, new float[]{20, 0, 12, 4});
        // подсумок: (-4.3,9,-6.35)…(-2.3,13,-1.35), поворот на −90° вокруг Y через точку (−3.3, 10, −3.35)
        cube(-4.3F, 9, -6.35F, -2.3F, 13, -1.35F, 0.0F, -90, -3.3F, 10, -3.35F,
                new float[]{5, 12, 7, 16}, new float[]{0, 12, 5, 16}, new float[]{12, 12, 14, 16},
                new float[]{7, 12, 12, 16}, new float[]{7, 12, 5, 7}, new float[]{9, 7, 7, 12});
    }

    /** Регистрирует рендерер для предмета (вызывать только если Curios установлен). */
    public static void register() {
        if (ModItems.BAIT_BAG != null) {
            CuriosRendererRegistry.register(ModItems.BAIT_BAG.get(), BaitBagRenderer::new);
        }
    }

    private static void cube(float x1, float y1, float z1, float x2, float y2, float z2, float inflate,
                             float rotY, float ox, float oy, float oz,
                             float[] north, float[] east, float[] south, float[] west, float[] up, float[] down) {
        x1 -= inflate; y1 -= inflate; z1 -= inflate;
        x2 += inflate; y2 += inflate; z2 += inflate;
        face(new float[][]{{x2, y2, z1}, {x2, y1, z1}, {x1, y1, z1}, {x1, y2, z1}}, north, 0, 0, -1, rotY, ox, oz);
        face(new float[][]{{x1, y2, z2}, {x1, y1, z2}, {x2, y1, z2}, {x2, y2, z2}}, south, 0, 0, 1, rotY, ox, oz);
        face(new float[][]{{x2, y2, z2}, {x2, y1, z2}, {x2, y1, z1}, {x2, y2, z1}}, east, 1, 0, 0, rotY, ox, oz);
        face(new float[][]{{x1, y2, z1}, {x1, y1, z1}, {x1, y1, z2}, {x1, y2, z2}}, west, -1, 0, 0, rotY, ox, oz);
        face(new float[][]{{x1, y2, z1}, {x1, y2, z2}, {x2, y2, z2}, {x2, y2, z1}}, up, 0, 1, 0, rotY, ox, oz);
        face(new float[][]{{x1, y1, z2}, {x1, y1, z1}, {x2, y1, z1}, {x2, y1, z2}}, down, 0, -1, 0, rotY, ox, oz);
    }

    private static void face(float[][] corners, float[] uv, float nx, float ny, float nz, float rotY, float ox, float oz) {
        double rad = Math.toRadians(rotY);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        float[][] pos = new float[4][3];
        for (int i = 0; i < 4; i++) {
            float dx = corners[i][0] - ox;
            float dz = corners[i][2] - oz;
            // поворот вокруг оси Y, затем перевод из «видовых» координат Blockbench (Y вверх от земли) в координаты тела (Y вниз от шеи)
            pos[i][0] = -(ox + dx * cos + dz * sin); // зеркально по X
            pos[i][1] = 20.0F - corners[i][1]; // на 4 пикселя выше
            pos[i][2] = oz - dx * sin + dz * cos;
        }
        float[][] uvs = {{uv[0], uv[1]}, {uv[0], uv[3]}, {uv[2], uv[3]}, {uv[2], uv[1]}};
        float rx = nx * cos + nz * sin;
        float rz = -nx * sin + nz * cos;
        QUADS.add(new Quad(pos, uvs, new float[]{-rx, -ny, rz}));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack,
                                                                          RenderLayerParent<T, M> parent, MultiBufferSource buffer, int light,
                                                                          float limbSwing, float limbSwingAmount, float partialTicks,
                                                                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(parent.getModel() instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        poseStack.pushPose();
        humanoid.body.translateAndRotate(poseStack);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        for (Quad quad : QUADS) {
            for (int i = 0; i < 4; i++) {
                consumer.addVertex(pose, quad.pos()[i][0] / 16.0F, quad.pos()[i][1] / 16.0F, quad.pos()[i][2] / 16.0F)
                        .setColor(-1)
                        .setUv(quad.uv()[i][0] / TEX, quad.uv()[i][1] / TEX)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, quad.normal()[0], quad.normal()[1], quad.normal()[2]);
            }
        }
        poseStack.popPose();
    }
}
