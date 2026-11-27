package com.sinscold.newfishing.network;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.entity.NetheriteFishingHook;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Клиент → сервер: ввод игрока в мини-игре вытаскивания. Шлётся только когда ввод меняется (не каждый тик).
 * action: 0 ничего, 1 тянуть (ЛКМ), 2 отпускать (ПКМ); rodX: положение удочки на экране −1…1.
 */
public record FightInputPayload(byte action, float rodX) implements CustomPacketPayload {
    public static final Type<FightInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "fight_input"));
    public static final StreamCodec<ByteBuf, FightInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, FightInputPayload::action,
            ByteBufCodecs.FLOAT, FightInputPayload::rodX,
            FightInputPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void handle(FightInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().fishing instanceof NetheriteFishingHook hook) {
                hook.setFightInput(payload.action(), payload.rodX());
            }
        });
    }
}
