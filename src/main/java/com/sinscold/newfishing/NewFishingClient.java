package com.sinscold.newfishing;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/** Точка входа клиента. Клиентский код (рендеры, item properties) живёт в пакете client/. */
@Mod(value = NewFishing.MODID, dist = Dist.CLIENT)
public class NewFishingClient {
    public NewFishingClient() {}
}
