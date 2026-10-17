package com.solarbiscuit.ratcurrency.inventory;

import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side record of which coin containers are open in a menu. An open menu edits its own in-memory copy of the
 * container (and of every container it is nested in), so nothing else may write to those containers until it closes:
 * pickups and coin services skip them, and a second player can't open them.
 */
public final class ContainerLocks {
    private static final Map<UUID, CoinContainerMenu> HOLDERS = new ConcurrentHashMap<>();

    private ContainerLocks() {
    }

    public static boolean isLocked(ItemStack stack) {
        return stack.getItem() instanceof CoinContainerItem && isLockedBy(CoinContainerItem.getId(stack), null);
    }

    /** True if {@code id} is held by an open menu that does not belong to {@code ignoreOwner}. */
    static boolean isLockedBy(@Nullable UUID id, @Nullable Player ignoreOwner) {
        if (id == null) {
            return false;
        }
        CoinContainerMenu holder = HOLDERS.get(id);
        if (holder == null) {
            return false;
        }
        if (!holder.isOpenOnServer()) {
            HOLDERS.remove(id, holder);
            return false;
        }
        return holder.owner() != ignoreOwner;
    }

    static void lock(CoinContainerMenu menu, Collection<UUID> ids) {
        for (UUID id : ids) {
            HOLDERS.put(id, menu);
        }
    }

    static void unlock(CoinContainerMenu menu) {
        HOLDERS.values().removeIf(holder -> holder == menu);
    }
}
