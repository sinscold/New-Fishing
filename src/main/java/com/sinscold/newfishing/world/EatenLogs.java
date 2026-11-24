package com.sinscold.newfishing.world;

import java.util.Map;

import com.sinscold.newfishing.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Выеденные брёвна: замена бревна ствола на выеденное того же дерева (дуб, акация, тропическое дерево). */
public final class EatenLogs {
    private static Map<Block, Block> eaten;

    private static Map<Block, Block> eaten() {
        if (eaten == null) {
            eaten = Map.of(
                    Blocks.OAK_LOG, ModBlocks.EATEN_OAK_LOG.get(),
                    Blocks.ACACIA_LOG, ModBlocks.EATEN_ACACIA_LOG.get(),
                    Blocks.JUNGLE_LOG, ModBlocks.EATEN_JUNGLE_LOG.get());
        }
        return eaten;
    }

    /**
     * Ствол, начинающийся с бревна base: одно из нижних брёвен (до 4-го, чтобы достать рукой) становится выеденным.
     * Возвращает true, если замена была.
     */
    public static boolean eatTrunk(LevelAccessor level, BlockPos base, RandomSource random, int flags) {
        int height = 0;
        while (height < 4 && eaten().containsKey(level.getBlockState(base.above(height)).getBlock())) {
            height++;
        }
        if (height == 0) return false;
        BlockPos pos = base.above(random.nextInt(height));
        BlockState log = level.getBlockState(pos);
        Block target = eaten().get(log.getBlock());
        BlockState state = target.defaultBlockState();
        if (log.hasProperty(RotatedPillarBlock.AXIS)) {
            state = state.setValue(RotatedPillarBlock.AXIS, log.getValue(RotatedPillarBlock.AXIS));
        }
        level.setBlock(pos, state, flags);
        return true;
    }

    private EatenLogs() {}
}
