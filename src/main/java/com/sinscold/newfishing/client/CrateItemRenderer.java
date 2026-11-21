package com.sinscold.newfishing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sinscold.newfishing.registry.ModBlocks;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Показывает в инвентаре, руке и на земле саму модель ящика (закрытого), а не плоский спрайт. */
public class CrateItemRenderer extends BlockEntityWithoutLevelRenderer {
    public CrateItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        ResourceLocation texture = blockItem.getBlock() == ModBlocks.LAVA_CRATE.get() ? CrateRenderer.LAVA : CrateRenderer.WATER;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        CrateGeometry.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), packedLight, packedOverlay, 0.0F);
        if (stack.getItem() instanceof com.sinscold.newfishing.item.LockedCrateItem) {
            CrateGeometry.renderLock(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(CrateRenderer.LOCK)), packedLight, packedOverlay);
        }
        poseStack.popPose();
    }
}
