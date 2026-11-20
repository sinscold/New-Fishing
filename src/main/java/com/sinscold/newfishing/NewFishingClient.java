package com.sinscold.newfishing;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Точка входа клиента. Клиентский код (рендеры, item properties) живёт в пакете client/. */
@Mod(value = NewFishing.MODID, dist = Dist.CLIENT)
public class NewFishingClient {
    public NewFishingClient(ModContainer container) {
        // экран настроек: Моды → New Fishing → Настройки (и мод Configured)
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
