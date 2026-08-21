package com.solarbiscuit.ratcurrency.network;

import com.solarbiscuit.ratcurrency.RatCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(RatCurrency.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OpenFromSlotPacket.class, OpenFromSlotPacket::encode, OpenFromSlotPacket::decode,
                OpenFromSlotPacket::handle);
    }
}
