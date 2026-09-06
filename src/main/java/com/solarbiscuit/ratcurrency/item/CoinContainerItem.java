package com.solarbiscuit.ratcurrency.item;

import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import com.solarbiscuit.ratcurrency.inventory.CoinItemHandler;
import com.solarbiscuit.ratcurrency.inventory.OpenContext;
import com.solarbiscuit.ratcurrency.inventory.RestoreTarget;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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
        int used = 0;
        CoinItemHandler handler = createHandler(stack);
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                used++;
            }
        }
        tooltip.add(Component.translatable("item.ratcurrency.coin_container.slots", used, this.slotCount));
    }
}
