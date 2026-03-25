package me.codexadrian.spirit.network.fabric;

import io.netty.buffer.Unpooled;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.network.packet.IPacket;
import me.codexadrian.spirit.network.packet.IPacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class NetworkHandlerImpl {

    public static <T extends IPacket<T>> void sendToServer(T packet) {
        CustomPacketPayload.Type<GenericPacketPayload<T>> payloadType = new CustomPacketPayload.Type<>(packet.getID());
        ClientPlayNetworking.send(new GenericPacketPayload<>(packet, payloadType));
    }

    public static <T> void registerClientToServerPacket(ResourceLocation location, IPacketHandler<T> handler,
            Class<T> tClass) {
        // Create the payload type for this packet
        CustomPacketPayload.Type<GenericPacketPayload<T>> payloadType = new CustomPacketPayload.Type<>(location);

        // Create a StreamCodec for the payload
        StreamCodec<RegistryFriendlyByteBuf, GenericPacketPayload<T>> codec = StreamCodec.of(
                (buf, payload) -> handler.encode(payload.packet(), buf),
                buf -> new GenericPacketPayload<>(handler.decode(buf), payloadType));

        // Register the payload type
        PayloadTypeRegistry.playC2S().register(payloadType, codec);

        // Register the receiver
        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) -> {
            T decode = payload.packet();
            context.player().getServer()
                    .execute(() -> handler.handle(decode).accept(context.player().getServer(), context.player()));
        });
    }

    // A generic payload wrapper for packets
    public record GenericPacketPayload<T>(T packet, CustomPacketPayload.Type<GenericPacketPayload<T>> payloadType)
            implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return payloadType;
        }
    }

    // A wrapper for sending packets
    public record WrappedPacketPayload<T extends IPacket<T>>(T packet) implements CustomPacketPayload {
        public static final Type<WrappedPacketPayload<?>> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(Spirit.MODID, "wrapped"));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return new Type<>(packet.getID());
        }
    }
}
