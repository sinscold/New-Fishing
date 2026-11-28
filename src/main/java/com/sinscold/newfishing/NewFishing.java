package com.sinscold.newfishing;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.sinscold.newfishing.registry.ModCreativeTabs;
import com.sinscold.newfishing.registry.ModDataComponents;
import com.sinscold.newfishing.registry.ModBlockEntities;
import com.sinscold.newfishing.registry.ModBlocks;
import com.sinscold.newfishing.registry.ModEffects;
import com.sinscold.newfishing.registry.ModEntities;
import com.sinscold.newfishing.registry.ModPotions;
import com.sinscold.newfishing.registry.ModSounds;
import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModLootConditions;
import com.sinscold.newfishing.registry.ModLootModifiers;
import com.sinscold.newfishing.registry.ModRecipeSerializers;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;

@Mod(NewFishing.MODID)
public class NewFishing {
    public static final String MODID = "newfishing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NewFishing(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        LOGGER.info("New Fishing: сборка с мини-игрой вытаскивания (ветка claude/great-pasteur-5zsawy)"); // убрать при слиянии в main
        ModLootConditions.CONDITIONS.register(modEventBus);
        com.sinscold.newfishing.registry.ModLootFunctions.FUNCTIONS.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        com.sinscold.newfishing.registry.ModFeatures.FEATURES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
    }
}
