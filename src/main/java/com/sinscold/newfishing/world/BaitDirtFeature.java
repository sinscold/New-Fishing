package com.sinscold.newfishing.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Подозрительная земля. Два режима (config `under_trees`):
 * под деревьями — земля под стволом каждого дерева чанка с шансом `chance` (холодные биомы, личинки);
 * по всему биому — `tries` случайных столбцов чанка, верхний блок земли с шансом `chance` (умеренные леса, черви).
 * Биом проверяется в каждом столбце (тег `biomes`). Ставится на шаге top_layer_modification, после деревьев.
 */
public class BaitDirtFeature extends Feature<BaitDirtFeature.Config> {
    public record Config(TagKey<Biome> biomes, boolean underTrees, int tries, float chance) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                TagKey.codec(Registries.BIOME).fieldOf("biomes").forGetter(Config::biomes),
                Codec.BOOL.optionalFieldOf("under_trees", false).forGetter(Config::underTrees),
                Codec.intRange(0, 256).optionalFieldOf("tries", 1).forGetter(Config::tries),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(Config::chance)
        ).apply(i, Config::new));
    }

    public BaitDirtFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Config config = context.config();
        int baseX = context.origin().getX() & ~15;
        int baseZ = context.origin().getZ() & ~15;
        boolean placed = false;
        if (config.underTrees()) {
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    BlockPos dirt = WorldScan.dirtUnderTrunk(level, baseX + dx, baseZ + dz);
                    if (dirt != null && random.nextFloat() < config.chance() && level.getBiome(dirt).is(config.biomes())) {
                        placed |= this.setDirt(level, dirt);
                    }
                }
            }
        } else {
            for (int i = 0; i < config.tries(); i++) {
                if (random.nextFloat() >= config.chance()) continue;
                BlockPos dirt = WorldScan.surfaceDirt(level, baseX + random.nextInt(16), baseZ + random.nextInt(16));
                if (dirt != null && level.getBiome(dirt).is(config.biomes())) {
                    placed |= this.setDirt(level, dirt);
                }
            }
        }
        return placed;
    }

    private boolean setDirt(WorldGenLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(ModBlocks.SUSPICIOUS_DIRT.get())) return false;
        level.setBlock(pos, ModBlocks.SUSPICIOUS_DIRT.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }
}
