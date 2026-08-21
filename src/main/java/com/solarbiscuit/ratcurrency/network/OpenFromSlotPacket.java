package com.solarbiscuit.ratcurrency.network;

import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import com.solarbiscuit.ratcurrency.inventory.OpenContext;
import com.solarbiscuit.ratcurrency.inventory.RestoreTarget;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenFromSlotPacket {
    private final int slotIndex;

    public OpenFromSlotPacket(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public static void encode(OpenFromSlotPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.slotIndex);
    }

    public static OpenFromSlotPacket decode(FriendlyByteBuf buf) {
        return new OpenFromSlotPacket(buf.readVarInt());
    }

    public static void handle(OpenFromSlotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (packet.slotIndex < 0 || packet.slotIndex >= menu.slots.size()) {
                return;
            }
            Slot slot = menu.getSlot(packet.slotIndex);
            OpenContext openContext = OpenContext.fromMenuSlot(player, menu, slot);
            if (openContext == null) {
                return;
            }
            RestoreTarget restore = RestoreTarget.fromCurrentMenu(player);
            if (menu instanceof CoinContainerMenu coinMenu) {
                coinMenu.skipRestore();
            }
            CoinContainerMenu.open(player, openContext, restore);
        });
        context.setPacketHandled(true);
    }
}
