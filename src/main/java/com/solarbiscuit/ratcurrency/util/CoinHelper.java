package com.solarbiscuit.ratcurrency.util;

import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.inventory.CoinItemHandler;
import com.solarbiscuit.ratcurrency.inventory.ContainerLocks;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.item.CoinItem;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import com.solarbiscuit.ratcurrency.item.RatCoinItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

public final class CoinHelper {
    public static final int COINS_PER_TIER = 100;
    /** Most coin containers that may sit inside each other (each level adds ~4 NBT levels; vanilla caps at 512). */
    public static final int MAX_NESTING = 8;

    private CoinHelper() {
    }

    public static boolean isCoin(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof CoinItem;
    }

    public static boolean isRatCoin(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RatCoinItem;
    }

    public static boolean isCoinContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof CoinContainerItem;
    }

    public static boolean isAllowedInContainer(ItemStack stack) {
        return isCoin(stack) || isCoinContainer(stack);
    }

    public static boolean containsId(ItemStack container, UUID id) {
        if (container.isEmpty() || !(container.getItem() instanceof CoinContainerItem)) {
            return false;
        }
        if (id.equals(CoinContainerItem.getId(container))) {
            return true;
        }
        for (ItemStack inner : CoinContainerItem.readContents(container)) {
            if (containsId(inner, id)) {
                return true;
            }
        }
        return false;
    }

    /** Number of coin containers nested in {@code stack}, counting itself (0 for anything else). */
    public static int containerDepth(ItemStack stack) {
        if (!(stack.getItem() instanceof CoinContainerItem)) {
            return 0;
        }
        int deepest = 0;
        for (ItemStack inner : CoinContainerItem.readContents(stack)) {
            if (inner.getItem() instanceof CoinContainerItem) {
                deepest = Math.max(deepest, containerDepth(inner));
            }
        }
        return 1 + deepest;
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

    /** Most stacks a single denominate() call returns, so huge values with high tiers disabled can't flood the world. */
    private static final int MAX_DENOMINATED_STACKS = 64;

    public static List<ItemStack> denominate(long copperUnits) {
        List<ItemStack> drops = new ArrayList<>();
        if (copperUnits <= 0) {
            return drops;
        }
        long remaining = copperUnits;
        CoinTier[] tiers = CoinTier.values();
        long[] values = new long[tiers.length];
        values[0] = 1;
        for (int i = 1; i < tiers.length; i++) {
            values[i] = values[i - 1] * COINS_PER_TIER;
        }
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (values[i] > remaining || !RatCurrencyConfig.isCoinEnabled(tiers[i])) {
                continue;
            }
            long count = remaining / values[i];
            remaining -= count * values[i];
            while (count > 0 && drops.size() < MAX_DENOMINATED_STACKS) {
                int stackSize = (int) Math.min(COINS_PER_TIER, count);
                drops.add(new ItemStack(tiers[i].getItem(), stackSize));
                count -= stackSize;
            }
        }
        return drops;
    }

    public static ItemStack insertIntoContainers(IItemHandler inventory, ItemStack coins) {
        ItemStack remaining = coins.copy();
        for (int i = 0; i < inventory.getSlots() && !remaining.isEmpty(); i++) {
            ItemStack stored = inventory.getStackInSlot(i);
            remaining = insertInto(stored, remaining);
        }
        return remaining;
    }

    /** Inserts coins into one container item, skipping containers that are open in a menu or have no room. */
    public static ItemStack insertInto(ItemStack container, ItemStack coins) {
        if (!(container.getItem() instanceof CoinContainerItem item) || coins.isEmpty()
                || !item.mightAccept(container, coins) || ContainerLocks.isLocked(container)) {
            return coins;
        }
        return item.createHandler(container).insertItemStacked(coins);
    }

    /**
     * Merges and upgrades the player's loose coins, keeping each tier in the slots it already occupied so a pickup
     * doesn't shuffle coins into the hotbar. Compressing never needs more stacks than it started with.
     */
    public static void compressPlayerInventory(Player player) {
        Inventory inv = player.getInventory();
        List<Integer> coinSlots = new ArrayList<>();
        EnumMap<CoinTier, List<Integer>> tierSlots = new EnumMap<>(CoinTier.class);
        long[] counts = new long[CoinTier.values().length];
        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            if (slot >= Inventory.INVENTORY_SIZE && slot != Inventory.SLOT_OFFHAND) {
                continue;
            }
            CoinTier tier = CoinTier.of(inv.getItem(slot).getItem());
            if (tier != null) {
                coinSlots.add(slot);
                tierSlots.computeIfAbsent(tier, t -> new ArrayList<>()).add(slot);
                counts[tier.ordinal()] += inv.getItem(slot).getCount();
            }
        }
        if (coinSlots.size() < 2 && !needsUpgrade(counts)) {
            return;
        }
        CoinTier[] tiers = CoinTier.values();
        for (int i = 0; i < tiers.length - 1; i++) {
            counts[i + 1] += counts[i] / COINS_PER_TIER;
            counts[i] %= COINS_PER_TIER;
        }
        ItemStack[] result = new ItemStack[inv.getContainerSize()];
        List<ItemStack> overflow = new ArrayList<>();
        for (CoinTier tier : tiers) {
            long remaining = counts[tier.ordinal()];
            for (int slot : tierSlots.getOrDefault(tier, List.of())) {
                if (remaining <= 0) {
                    break;
                }
                int put = (int) Math.min(COINS_PER_TIER, remaining);
                result[slot] = new ItemStack(tier.getItem(), put);
                remaining -= put;
            }
            while (remaining > 0) {
                int put = (int) Math.min(COINS_PER_TIER, remaining);
                overflow.add(new ItemStack(tier.getItem(), put));
                remaining -= put;
            }
        }
        for (int slot : coinSlots) {
            if (result[slot] == null && !overflow.isEmpty()) {
                result[slot] = overflow.remove(0);
            }
        }
        for (int slot : coinSlots) {
            ItemStack updated = result[slot] == null ? ItemStack.EMPTY : result[slot];
            if (!ItemStack.matches(inv.getItem(slot), updated)) {
                inv.setItem(slot, updated);
            }
        }
        for (ItemStack extra : overflow) {
            // Not expected (see above), but never delete coins.
            if (!inv.add(extra)) {
                player.drop(extra, false);
            }
        }
    }

    private static boolean needsUpgrade(long[] counts) {
        for (int i = 0; i < counts.length - 1; i++) {
            if (counts[i] >= COINS_PER_TIER) {
                return true;
            }
        }
        return false;
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

    public static boolean tryTake(Player player, CoinTier tier, int count) {
        if (count <= 0) {
            return true;
        }
        if (tier == null || countCoins(player, tier) < count) {
            return false;
        }
        int remaining = takeFromInventory(player.getInventory(), tier, count);
        if (remaining > 0) {
            remaining = com.solarbiscuit.ratcurrency.compat.curios.CuriosCompat.takeCoins(player, tier, remaining);
        }
        return remaining <= 0;
    }

    private static int takeFromInventory(Inventory inv, CoinTier tier, int remaining) {
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            remaining = takeFromStack(stack, tier, remaining);
            if (stack.isEmpty()) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        return remaining;
    }

    public static int takeFromStack(ItemStack stack, CoinTier tier, int remaining) {
        if (remaining <= 0 || stack.isEmpty()) {
            return remaining;
        }
        if (CoinTier.of(stack.getItem()) == tier) {
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            return remaining - take;
        }
        if (stack.getItem() instanceof CoinContainerItem item) {
            if (ContainerLocks.isLocked(stack)) {
                return remaining;
            }
            CoinItemHandler handler = item.createHandler(stack);
            final int[] left = {remaining};
            handler.runSilent(() -> {
                for (int i = 0; i < handler.getSlots() && left[0] > 0; i++) {
                    ItemStack inner = handler.getStackInSlot(i);
                    left[0] = takeFromStack(inner, tier, left[0]);
                    if (inner.isEmpty()) {
                        handler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
                compress(handler);
            });
            return left[0];
        }
        return remaining;
    }

    public static int countInStack(ItemStack stack, CoinTier tier) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (CoinTier.of(stack.getItem()) == tier) {
            return stack.getCount();
        }
        if (stack.getItem() instanceof CoinContainerItem) {
            // Containers open in a menu can't be spent from, so they don't count either.
            if (ContainerLocks.isLocked(stack)) {
                return 0;
            }
            int total = 0;
            for (ItemStack inner : CoinContainerItem.readContents(stack)) {
                total += countInStack(inner, tier);
            }
            return total;
        }
        return 0;
    }
}
