package com.solarbiscuit.ratcurrency.item;

import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import com.solarbiscuit.ratcurrency.inventory.CoinItemHandler;
import com.solarbiscuit.ratcurrency.inventory.OpenContext;
import com.solarbiscuit.ratcurrency.inventory.RestoreTarget;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class CoinContainerItem extends Item {
    public static final String TAG_ID = "Id";
    public static final String TAG_ITEMS = "Items";

    private final int slotCount;
    private final String titleKey;

    public CoinContainerItem(int slotCount, String titleKey, Properties properties) {
        super(properties);
        this.slotCount = slotCount;
        this.titleKey = titleKey;
    }

    public int getSlotCount() {
        return this.slotCount;
    }

    public String getTitleKey() {
        return this.titleKey;
    }

    public boolean isItemAllowed(ItemStack stack) {
        return CoinHelper.isAllowedInContainer(stack);
    }

    public int getSlotLimit() {
        return CoinHelper.COINS_PER_TIER;
    }

    public boolean shouldCompress() {
        return true;
    }

    public CoinItemHandler createHandler(ItemStack stack) {
        return new CoinItemHandler(stack, this.slotCount);
    }

    /** Reads the stored contents (index = slot) without building a handler or stamping an Id. */
    public static NonNullList<ItemStack> readContents(ItemStack stack) {
        int size = stack.getItem() instanceof CoinContainerItem item ? item.getSlotCount() : 0;
        NonNullList<ItemStack> contents = NonNullList.withSize(size, ItemStack.EMPTY);
        ListTag items = storedItems(stack);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot >= 0 && slot < size) {
                contents.set(slot, ItemStack.of(entry));
            }
        }
        return contents;
    }

    /**
     * Cheap NBT-only check used before building a handler for pickups: false only when every slot is taken and no
     * stack of this item has room left.
     */
    public boolean mightAccept(ItemStack container, ItemStack coins) {
        if (!isItemAllowed(coins)) {
            return false;
        }
        ListTag items = storedItems(container);
        if (items.size() < this.slotCount) {
            return true;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(coins.getItem());
        String id = key == null ? "" : key.toString();
        int limit = Math.min(getSlotLimit(), coins.getMaxStackSize());
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            if (id.equals(entry.getString("id")) && entry.getInt("Count") < limit) {
                return true;
            }
        }
        return false;
    }

    private static ListTag storedItems(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            return new ListTag();
        }
        return tag.getCompound(TAG_ITEMS).getList("Items", Tag.TAG_COMPOUND);
    }

    public static UUID getOrCreateId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(TAG_ID)) {
            tag.putUUID(TAG_ID, UUID.randomUUID());
        }
        return tag.getUUID(TAG_ID);
    }

    @Nullable
    public static UUID getId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID(TAG_ID)) {
            return tag.getUUID(TAG_ID);
        }
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            getOrCreateId(stack);
            CoinContainerMenu.open(serverPlayer, OpenContext.hand(hand), RestoreTarget.none());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Read the saved NBT directly: building a CoinItemHandler stamps a random Id onto the stack, which
        // corrupted the creative tab's template stacks. Only non-empty stacks are saved, so the size is the count.
        int used = storedItems(stack).size();
        tooltip.add(Component.translatable("item.ratcurrency.coin_container.slots", used, this.slotCount));
    }
}
