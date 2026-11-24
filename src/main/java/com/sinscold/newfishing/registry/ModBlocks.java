package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.block.CrateBlock;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NewFishing.MODID);

    public static final DeferredBlock<CrateBlock> WATER_CRATE = BLOCKS.register("water_crate",
            () -> new CrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noOcclusion(), "water_crate"));
    public static final DeferredBlock<CrateBlock> LAVA_CRATE = BLOCKS.register("lava_crate",
            () -> new CrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(1.0F).sound(SoundType.NETHER_BRICKS).noOcclusion(), "lava_crate"));

    public static final DeferredBlock<com.sinscold.newfishing.block.BeerCrateBlock> BEER_CRATE = BLOCKS.register("beer_crate",
            () -> new com.sinscold.newfishing.block.BeerCrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOD).noOcclusion()));

    /** Подозрительная земля: лопатой из неё выбивается наживка (личинка или червяк, по биому; см. loot_table/blocks). */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> SUSPICIOUS_DIRT = BLOCKS.register("suspicious_dirt",
            () -> new net.minecraft.world.level.block.Block(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.DIRT)));

    /** Выеденные брёвна (жаркие биомы): сломав, получаешь жука. */
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> EATEN_OAK_LOG = eatenLog("eaten_oak_log", net.minecraft.world.level.block.Blocks.OAK_LOG);
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> EATEN_ACACIA_LOG = eatenLog("eaten_acacia_log", net.minecraft.world.level.block.Blocks.ACACIA_LOG);
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> EATEN_JUNGLE_LOG = eatenLog("eaten_jungle_log", net.minecraft.world.level.block.Blocks.JUNGLE_LOG);

    private static DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> eatenLog(String name, net.minecraft.world.level.block.Block base) {
        return BLOCKS.register(name, () -> new net.minecraft.world.level.block.RotatedPillarBlock(
                BlockBehaviour.Properties.ofFullCopy(base).strength(1.5F)));
    }

    private ModBlocks() {}
}
