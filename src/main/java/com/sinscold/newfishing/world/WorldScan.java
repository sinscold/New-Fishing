package com.sinscold.newfishing.world;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Поиск земли и стволов в столбце мира (для генерации наживки). */
final class WorldScan {
    private static final int MAX_DEPTH = 48;

    /**
     * Верхний блок земли (тег `minecraft:dirt`) в столбце x, z: спускаемся сверху сквозь воздух, листья, брёвна и траву.
     * Возвращает null, если сверху не земля (камень, вода, песок…).
     */
    static BlockPos surfaceDirt(LevelAccessor level, int x, int z) {
        int top = Math.max(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        for (int i = 0; i < MAX_DEPTH && pos.getY() > level.getMinBuildHeight(); i++) {
            pos.move(0, -1, 0);
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.DIRT)) return pos.immutable();
            if (!(state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.canBeReplaced())) return null;
        }
        return null;
    }

    /** Земля прямо под стволом дерева в этом столбце (над ней бревно) или null. */
    static BlockPos dirtUnderTrunk(LevelAccessor level, int x, int z) {
        BlockPos dirt = surfaceDirt(level, x, z);
        return dirt != null && level.getBlockState(dirt.above()).is(BlockTags.LOGS) ? dirt : null;
    }

    private WorldScan() {}
}
