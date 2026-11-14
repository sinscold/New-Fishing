package com.sinscold.newfishing;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.sinscold.newfishing.registry.ModCreativeTabs;
import com.sinscold.newfishing.registry.ModItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(NewFishing.MODID)
public class NewFishing {
    public static final String MODID = "newfishing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NewFishing(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
    }
}
