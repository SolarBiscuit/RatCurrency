package com.solarbiscuit.ratcurrency.network;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratlib.network.NetworkChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    private static NetworkChannel channel;

    private ModNetwork() {
    }

    public static void register() {
        channel = new NetworkChannel(RatCurrency.MOD_ID, PROTOCOL);
        channel.register(ConfigSyncPacket.class, NetworkDirection.PLAY_TO_CLIENT,
                ConfigSyncPacket::encode, ConfigSyncPacket::decode, ConfigSyncPacket::handle);
    }

    public static void sendConfig(ServerPlayer player) {
        channel.sendTo(player, ConfigSyncPacket.fromConfig());
    }

    /** Re-sends the config to everyone online, e.g. after the server's config file was edited. */
    public static void sendConfigToAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            ConfigSyncPacket packet = ConfigSyncPacket.fromConfig();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                channel.sendTo(player, packet);
            }
        });
    }
}
