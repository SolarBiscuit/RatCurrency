package com.solarbiscuit.ratcurrency.network;

import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Server -> client: the server's Rat Currency config values, used by the client while connected. */
public record ConfigSyncPacket(Map<String, Object> values) {
    private static final byte BOOLEAN = 0;
    private static final byte INT = 1;
    private static final byte LONG = 2;
    private static final byte DOUBLE = 3;
    private static final byte STRING = 4;

    public static ConfigSyncPacket fromConfig() {
        return new ConfigSyncPacket(RatCurrencyConfig.snapshot());
    }

    public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buf) {
        Map<String, Object> supported = new LinkedHashMap<>();
        packet.values.forEach((key, value) -> {
            if (value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof Double
                    || value instanceof String) {
                supported.put(key, value);
            }
        });
        buf.writeVarInt(supported.size());
        supported.forEach((key, value) -> {
            buf.writeUtf(key);
            if (value instanceof Boolean b) {
                buf.writeByte(BOOLEAN);
                buf.writeBoolean(b);
            } else if (value instanceof Integer i) {
                buf.writeByte(INT);
                buf.writeVarInt(i);
            } else if (value instanceof Long l) {
                buf.writeByte(LONG);
                buf.writeVarLong(l);
            } else if (value instanceof Double d) {
                buf.writeByte(DOUBLE);
                buf.writeDouble(d);
            } else {
                buf.writeByte(STRING);
                buf.writeUtf((String) value);
            }
        });
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            byte type = buf.readByte();
            Object value = switch (type) {
                case BOOLEAN -> buf.readBoolean();
                case INT -> buf.readVarInt();
                case LONG -> buf.readVarLong();
                case DOUBLE -> buf.readDouble();
                case STRING -> buf.readUtf();
                default -> throw new IllegalArgumentException("Unknown config value type " + type);
            };
            values.put(key, value);
        }
        return new ConfigSyncPacket(values);
    }

    public static void handle(ConfigSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConfigSync.apply(packet.values));
    }
}
