package com.solarbiscuit.ratcurrency.client;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.network.ModNetwork;
import com.solarbiscuit.ratcurrency.network.OpenFromSlotPacket;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return;
        }
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (!screen.getMenu().getCarried().isEmpty()) {
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem() || !(slot.getItem().getItem() instanceof CoinContainerItem)) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new OpenFromSlotPacket(slot.index));
        event.setCanceled(true);
    }
}
