package com.solarbiscuit.ratcurrency.economy;

import com.solarbiscuit.ratcurrency.compat.curios.CuriosCompat;
import com.solarbiscuit.ratcurrency.item.CoinItem;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import com.solarbiscuit.ratlib.economy.CoinService;
import com.solarbiscuit.ratlib.economy.DualCost;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.List;

public final class RatCoinService implements CoinService {
    @Override
    public boolean isCoin(ItemStack stack) {
        return CoinHelper.isCoin(stack);
    }

    @Override
    public boolean isGoldCoin(ItemStack stack) {
        return CoinTier.of(stack.getItem()) == CoinTier.GOLD;
    }

    @Override
    public Item coin(String path) {
        CoinTier tier = CoinTier.byPath(path);
        return tier == null ? Items.AIR : tier.getItem();
    }

    @Override
    public ItemStack stack(String path, int count) {
        Item item = coin(path);
        if (item == Items.AIR || count <= 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    @Override
    public int coinsPerTier() {
        return CoinHelper.COINS_PER_TIER;
    }

    @Override
    public int tierIndex(String path) {
        CoinTier tier = CoinTier.byPath(path);
        return tier == null ? -1 : tier.ordinal();
    }

    @Override
    public List<ItemStack> denominate(int copperUnits) {
        return CoinHelper.denominate(copperUnits);
    }

    @Override
    public List<ItemStack> mixedFromCopper(int copperValue) {
        return CoinHelper.denominate(copperValue);
    }

    @Override
    public List<ItemStack> randomPurse(RandomSource random, int maxGold) {
        int maxCopper = Math.max(1, maxGold) * CoinHelper.COINS_PER_TIER * CoinHelper.COINS_PER_TIER;
        return CoinHelper.denominate(1 + random.nextInt(maxCopper));
    }

    @Override
    public int count(Player player, String tierPath) {
        CoinTier tier = CoinTier.byPath(tierPath);
        return tier == null ? 0 : CoinHelper.countCoins(player, tier);
    }

    @Override
    public boolean tryTake(Player player, String tierPath, int count) {
        CoinTier tier = CoinTier.byPath(tierPath);
        return tier != null && CoinHelper.tryTake(player, tier, count);
    }

    @Override
    public ItemStack insert(Player player, ItemStack coins) {
        if (coins.isEmpty() || !(coins.getItem() instanceof CoinItem)) {
            return coins;
        }
        ItemStack remaining = CuriosCompat.insertIntoBelt(player, coins);
        remaining = CoinHelper.insertIntoContainers(new InvWrapper(player.getInventory()), remaining);
        if (!remaining.isEmpty() && player.getInventory().add(remaining)) {
            remaining = ItemStack.EMPTY;
        }
        return remaining;
    }

    @Override
    public DualCost exact(String path, int count, int emeraldFallback) {
        ItemStack coins = stack(path, count);
        if (!coins.isEmpty()) {
            return new DualCost(coins, ItemStack.EMPTY);
        }
        return DualCost.emeralds(emeraldFallback);
    }

    @Override
    public DualCost floorWithVariance(String floorPath, int floorCount, RandomSource random, int emeraldFallback) {
        int extra = random.nextInt(coinsPerTier() + 1);
        int total = floorCount + Math.max(0, extra);
        int index = tierIndex(floorPath);
        if (index < 0) {
            return DualCost.emeralds(emeraldFallback);
        }
        CoinTier[] tiers = CoinTier.values();
        if (total >= coinsPerTier() && index + 1 < tiers.length) {
            int nextCount = total / coinsPerTier();
            int remainder = total % coinsPerTier();
            ItemStack higher = new ItemStack(tiers[index + 1].getItem(), nextCount);
            ItemStack lower = remainder > 0 ? stack(floorPath, remainder) : ItemStack.EMPTY;
            return new DualCost(higher, lower);
        }
        ItemStack coins = stack(floorPath, total);
        if (coins.isEmpty()) {
            return DualCost.emeralds(emeraldFallback);
        }
        return new DualCost(coins, ItemStack.EMPTY);
    }
}
