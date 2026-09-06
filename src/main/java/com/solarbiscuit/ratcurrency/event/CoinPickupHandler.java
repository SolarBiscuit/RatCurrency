package com.solarbiscuit.ratcurrency.event;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.advancement.ModCriteria;
import com.solarbiscuit.ratcurrency.compat.curios.CuriosCompat;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.wrapper.InvWrapper;

@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID)
public class CoinPickupHandler {
    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItem();
        if (!CoinHelper.isCoin(stack) && !CoinHelper.isRatCoin(stack)) {
            return;
        }
        Player player = event.getEntity();
        int original = stack.getCount();
        ItemStack remaining = stack.copy();
        remaining = CuriosCompat.insertIntoBelt(player, remaining);
        remaining = CoinHelper.insertIntoContainers(new InvWrapper(player.getInventory()), remaining);
        int taken = original - remaining.getCount();
        if (taken <= 0) {
            return;
        }
        player.take(itemEntity, taken);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        if (remaining.isEmpty()) {
            stack.setCount(0);
            event.setCanceled(true);
        } else {
            stack.setCount(remaining.getCount());
        }
    }

    @SubscribeEvent
    public static void afterPickup(PlayerEvent.ItemPickupEvent event) {
        if (!CoinHelper.isCoin(event.getStack())) {
            return;
        }
        Player player = event.getEntity();
        CoinHelper.compressPlayerInventory(player);
        if (player instanceof ServerPlayer serverPlayer && ModCriteria.COIN_COUNT != null) {
            ModCriteria.COIN_COUNT.trigger(serverPlayer);
        }
    }
}
