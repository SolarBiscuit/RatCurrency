package com.solarbiscuit.ratcurrency.util;

import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.item.CoinItem;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

public final class CoinHelper {
    public static final int COINS_PER_TIER = 100;

    private CoinHelper() {
    }

    public static boolean isCoin(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof CoinItem;
    }

    public static boolean isCoinContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof CoinContainerItem;
    }

    public static boolean isAllowedInContainer(ItemStack stack) {
        return isCoin(stack) || isCoinContainer(stack);
    }

    public static boolean containsId(ItemStack container, UUID id) {
        if (container.isEmpty() || !(container.getItem() instanceof CoinContainerItem item)) {
            return false;
        }
        UUID own = CoinContainerItem.getId(container);
        if (id.equals(own)) {
            return true;
        }
        IItemHandler handler = item.createHandler(container);
        for (int i = 0; i < handler.getSlots(); i++) {
            if (containsId(handler.getStackInSlot(i), id)) {
                return true;
            }
        }
        return false;
    }

    public static void compress(IItemHandlerModifiable handler) {
        EnumMap<CoinTier, Integer> counts = new EnumMap<>(CoinTier.class);
        for (CoinTier tier : CoinTier.values()) {
            counts.put(tier, 0);
        }

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            CoinTier tier = CoinTier.of(stack.getItem());
            if (tier != null) {
                counts.merge(tier, stack.getCount(), Integer::sum);
                handler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        CoinTier[] tiers = CoinTier.values();
        for (int i = 0; i < tiers.length - 1; i++) {
            CoinTier tier = tiers[i];
            int count = counts.get(tier);
            int upgraded = count / COINS_PER_TIER;
            counts.put(tier, count % COINS_PER_TIER);
            if (upgraded > 0) {
                counts.merge(tiers[i + 1], upgraded, Integer::sum);
            }
        }

        for (int t = tiers.length - 1; t >= 0; t--) {
            CoinTier tier = tiers[t];
            int remaining = counts.get(tier);
            int slot = 0;
            while (remaining > 0 && slot < handler.getSlots()) {
                while (slot < handler.getSlots() && !handler.getStackInSlot(slot).isEmpty()) {
                    slot++;
                }
                if (slot >= handler.getSlots()) {
                    break;
                }
                int put = Math.min(COINS_PER_TIER, remaining);
                handler.setStackInSlot(slot, new ItemStack(tier.getItem(), put));
                remaining -= put;
                slot++;
            }
        }
    }

    public static List<ItemStack> denominate(int copperUnits) {
        List<ItemStack> drops = new ArrayList<>();
        if (copperUnits <= 0) {
            return drops;
        }
        int remaining = copperUnits;
        CoinTier[] tiers = CoinTier.values();
        long[] values = new long[tiers.length];
        values[0] = 1;
        for (int i = 1; i < tiers.length; i++) {
            values[i] = values[i - 1] * COINS_PER_TIER;
        }
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (values[i] > remaining) {
                continue;
            }
            int count = (int) (remaining / values[i]);
            if (count > 0) {
                remaining -= count * values[i];
                while (count > 0) {
                    int stackSize = Math.min(COINS_PER_TIER, count);
                    drops.add(new ItemStack(tiers[i].getItem(), stackSize));
                    count -= stackSize;
                }
            }
        }
        return drops;
    }

    public static ItemStack insertIntoContainers(IItemHandler inventory, ItemStack coins) {
        ItemStack remaining = coins.copy();
        for (int i = 0; i < inventory.getSlots() && !remaining.isEmpty(); i++) {
            ItemStack stored = inventory.getStackInSlot(i);
            if (stored.getItem() instanceof CoinContainerItem container) {
                remaining = container.createHandler(stored).insertItemStacked(remaining);
            }
        }
        return remaining;
    }

    public static void compressPlayerInventory(Player player) {
        Inventory inv = player.getInventory();
        ItemStackHandler temp = new ItemStackHandler(37);
        for (int i = 0; i < 36; i++) {
            temp.setStackInSlot(i, inv.getItem(i));
        }
        temp.setStackInSlot(36, inv.getItem(Inventory.SLOT_OFFHAND));
        compress(temp);
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, temp.getStackInSlot(i));
        }
        inv.setItem(Inventory.SLOT_OFFHAND, temp.getStackInSlot(36));
    }

    public static int countCoins(Player player, CoinTier tier) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            total += countInStack(inv.getItem(i), tier);
        }
        total += com.solarbiscuit.ratcurrency.compat.curios.CuriosCompat.countCoins(player, tier);
        return total;
    }

    public static int countInStack(ItemStack stack, CoinTier tier) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (CoinTier.of(stack.getItem()) == tier) {
            return stack.getCount();
        }
        if (stack.getItem() instanceof CoinContainerItem item) {
            int total = 0;
            IItemHandler handler = item.createHandler(stack);
            for (int i = 0; i < handler.getSlots(); i++) {
                total += countInStack(handler.getStackInSlot(i), tier);
            }
            return total;
        }
        return 0;
    }
}
