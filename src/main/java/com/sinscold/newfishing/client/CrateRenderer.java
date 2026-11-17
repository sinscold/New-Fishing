package com.sinscold.newfishing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.block.CrateBlock;
import com.sinscold.newfishing.block.entity.CrateBlockEntity;
import com.sinscold.newfishing.registry.ModBlocks;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Рисует ящик своей моделью из Blockbench (см. CrateGeometry) с анимацией крышки.
 * Текстуры: assets/newfishing/textures/entity/crate/water_crate.png и lava_crate.png (64×64).
 */
public class CrateRenderer implements BlockEntityRenderer<CrateBlockEntity> {
    public static final ResourceLocation WATER = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/entity/crate/water_crate.png");
    public static final ResourceLocation LAVA = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/entity/crate/lava_crate.png");

    public CrateRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrateBlockEntity crate, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = crate.getBlockState();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.getValue(CrateBlock.FACING).toYRot()));
        ResourceLocation texture = state.is(ModBlocks.LAVA_CRATE.get()) ? LAVA : WATER;
        CrateGeometry.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), packedLight, packedOverlay, crate.getOpenNess(partialTick));
        poseStack.popPose();
    }
}
