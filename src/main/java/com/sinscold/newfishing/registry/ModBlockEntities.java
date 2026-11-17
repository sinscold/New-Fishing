package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.block.entity.CrateBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NewFishing.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrateBlockEntity>> CRATE =
            BLOCK_ENTITIES.register("crate", () -> BlockEntityType.Builder
                    .of(CrateBlockEntity::new, ModBlocks.WATER_CRATE.get(), ModBlocks.LAVA_CRATE.get())
                    .build(null));

    private ModBlockEntities() {}
}
