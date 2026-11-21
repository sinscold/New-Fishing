package com.sinscold.newfishing.item;

import java.util.List;
import java.util.Map;

import com.sinscold.newfishing.block.CrateBlock;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Запертый ящик: ставит тот же блок с замком (LOCKED = true). Не заменяет обычный предмет блока. */
public class LockedCrateItem extends BlockItem {
    public LockedCrateItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        return state == null ? null : state.setValue(CrateBlock.LOCKED, true);
    }

    /** Блок уже связан с обычным предметом ящика, второй предмет не должен его подменять. */
    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.newfishing.locked_crate").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
