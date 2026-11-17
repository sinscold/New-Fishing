package com.sinscold.newfishing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.block.CrateBlock;
import com.sinscold.newfishing.block.entity.CrateBlockEntity;
import com.sinscold.newfishing.registry.ModBlocks;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Рисует ящик моделью сундука со своей текстурой и анимацией крышки.
 * Текстуры: assets/newfishing/textures/entity/chest/water_crate.png и lava_crate.png (раскладка 64×64 как у сундука).
 * Собственная модель из Blockbench позже заменит этот рендерер.
 */
public class CrateRenderer implements BlockEntityRenderer<CrateBlockEntity> {
    private static final Material WATER = new Material(Sheets.CHEST_SHEET,
            ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "entity/chest/water_crate"));
    private static final Material LAVA = new Material(Sheets.CHEST_SHEET,
            ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "entity/chest/lava_crate"));

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public CrateRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
    }

    @Override
    public void render(CrateBlockEntity crate, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = crate.getBlockState();
        poseStack.pushPose();
        float yRot = state.getValue(CrateBlock.FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        float openness = crate.getOpenNess(partialTick);
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;

        Material material = state.is(ModBlocks.LAVA_CRATE.get()) ? LAVA : WATER;
        VertexConsumer consumer = material.buffer(buffer, RenderType::entityCutout);
        this.lid.xRot = -(openness * ((float) Math.PI / 2F));
        this.lock.xRot = this.lid.xRot;
        this.lid.render(poseStack, consumer, packedLight, packedOverlay);
        this.lock.render(poseStack, consumer, packedLight, packedOverlay);
        this.bottom.render(poseStack, consumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
