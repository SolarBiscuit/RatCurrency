package com.solarbiscuit.ratcurrency.network;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/** Client side of the config sync. Only loaded on the client. */
@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID, value = Dist.CLIENT)
public final class ClientConfigSync {
    private ClientConfigSync() {
    }

    static void apply(Map<String, Object> values) {
        // Singleplayer and LAN hosts run the server in this JVM with this same config: nothing to override.
        if (Minecraft.getInstance().getSingleplayerServer() != null) {
            return;
        }
        RatCurrencyConfig.applyServerValues(values);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RatCurrencyConfig.clearServerValues();
    }
}
