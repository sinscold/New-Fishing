package com.sinscold.newfishing.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

/** Выеденные брёвна в деревьях жарких биомов: каждый ствол чанка с шансом `chance` (шаг top_layer_modification). */
public class EatenLogFeature extends Feature<EatenLogFeature.Config> {
    public record Config(TagKey<Biome> biomes, float chance) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                TagKey.codec(Registries.BIOME).fieldOf("biomes").forGetter(Config::biomes),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 0.2F).forGetter(Config::chance)
        ).apply(i, Config::new));
    }

    public EatenLogFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int baseX = context.origin().getX() & ~15;
        int baseZ = context.origin().getZ() & ~15;
        boolean placed = false;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                BlockPos dirt = WorldScan.dirtUnderTrunk(level, baseX + dx, baseZ + dz);
                if (dirt != null && random.nextFloat() < context.config().chance() && level.getBiome(dirt).is(context.config().biomes())) {
                    placed |= EatenLogs.eatTrunk(level, dirt.above(), random, Block.UPDATE_CLIENTS);
                }
            }
        }
        return placed;
    }
}
