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

    private ModBlocks() {}
}
