package com.solarbiscuit.ratcurrency.event;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.advancement.ModCriteria;
import com.solarbiscuit.ratcurrency.compat.curios.CuriosCompat;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID)
public class CoinPickupHandler {
    // ItemEntity.target: the only player allowed to pick the item up (set by /give when the inventory overflows).
    // Private with no getter in 1.20.1; vanilla checks it after this event fires, so we have to check it ourselves.
    @Nullable
    private static final Field TARGET = findTargetField();

    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItem();
        if (!CoinHelper.isCoin(stack) && !CoinHelper.isRatCoin(stack)) {
            return;
        }
        Player player = event.getEntity();
        if (isReservedForSomeoneElse(itemEntity, player)) {
            return;
        }
        int original = stack.getCount();
        ItemStack remaining = stack.copy();
        remaining = CuriosCompat.insertIntoBelt(player, remaining);
        remaining = CoinHelper.insertIntoContainers(new InvWrapper(player.getInventory()), remaining);
        int taken = original - remaining.getCount();
        if (taken <= 0) {
            return;
        }
        ItemStack absorbed = stack.copyWithCount(taken);
        player.take(itemEntity, taken);
        // Vanilla only awards the stat and fires ItemPickupEvent for what it adds itself, so do it for the
        // coins that went into containers (quest mods, the coin-count advancement, inventory compression).
        player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), taken);
        ForgeEventFactory.firePlayerItemPickupEvent(player, itemEntity, absorbed);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        if (remaining.isEmpty()) {
            stack.setCount(0);
            event.setCanceled(true);
        } else {
            stack.setCount(remaining.getCount());
        }
    }

    private static boolean isReservedForSomeoneElse(ItemEntity itemEntity, Player player) {
        if (TARGET == null) {
            return false;
        }
        try {
            Object target = TARGET.get(itemEntity);
            return target != null && !target.equals(player.getUUID());
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    @Nullable
    private static Field findTargetField() {
        try {
            return ObfuscationReflectionHelper.findField(ItemEntity.class, "f_265881_");
        } catch (RuntimeException e) {
            RatCurrency.LOGGER.warn("Could not find ItemEntity.target; coins reserved for a player may be routed into others' containers", e);
            return null;
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
