package com.sinscold.newfishing.network;

import com.sinscold.newfishing.NewFishing;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Регистрация сетевых пакетов мода (мод-шина). */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class ModNetwork {
    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(FightInputPayload.TYPE, FightInputPayload.STREAM_CODEC, FightInputPayload::handle);
    }

    private ModNetwork() {}
}
