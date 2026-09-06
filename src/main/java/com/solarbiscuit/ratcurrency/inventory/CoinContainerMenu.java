package com.solarbiscuit.ratcurrency.inventory;

import com.solarbiscuit.ratcurrency.advancement.ModCriteria;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.registry.ModMenus;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class CoinContainerMenu extends AbstractContainerMenu {
    private final OpenContext openContext;
    private final RestoreTarget restoreTarget;
    private final CoinItemHandler handler;
    private final Layout layout;
    private final UUID openId;
    private boolean restoreOnClose = true;

    public record Layout(int columns, int rows, int imageWidth, int imageHeight, int containerX, int containerY,
                         int playerInvX, int playerInvY, int textureWidth, int textureHeight) {
        public static Layout forSlots(int slots) {
            if (slots == 6) {
                return new Layout(3, 2, 176, 166, 62, 17, 8, 84, 256, 256);
            }
            if (slots == 9) {
                return new Layout(3, 3, 176, 166, 62, 17, 8, 84, 256, 256);
            }
            if (slots == 27) {
                return new Layout(9, 3, 176, 166, 8, 18, 8, 84, 256, 256);
            }
            if (slots == 96) {
                return new Layout(12, 8, 256, 256, 20, 16, 47, 172, 256, 256);
            }
            throw new IllegalArgumentException("Unsupported coin container size: " + slots);
        }

        public int hotbarY() {
            return this.playerInvY + 58;
        }
    }

    public CoinContainerMenu(int id, Inventory playerInv, OpenContext context, RestoreTarget restore,
                             CoinItemHandler handler, UUID openId) {
        super(ModMenus.COIN_CONTAINER.get(), id);
        this.openContext = context;
        this.restoreTarget = restore;
        this.handler = handler;
        this.openId = openId;
        this.layout = Layout.forSlots(handler.getSlots());

        addContainerSlots();
        addPlayerSlots(playerInv, context.lockedPlayerSlot(playerInv.player));
    }

    public static CoinContainerMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        UUID openId = buf.readUUID();
        OpenContext context = OpenContext.read(buf);
        RestoreTarget restore = RestoreTarget.read(buf);
        CoinItemHandler handler = context.createHandler(inv.player);
        if (handler.getSlots() != size) {
            handler = new CoinItemHandler(size);
        }
        return new CoinContainerMenu(id, inv, context, restore, handler, openId);
    }

    public static void open(ServerPlayer player, OpenContext context, RestoreTarget restore) {
        ItemStack stack = context.getStack(player);
        if (!(stack.getItem() instanceof CoinContainerItem item)) {
            return;
        }
        UUID containerId = CoinContainerItem.getOrCreateId(stack);
        Component title = Component.translatable(item.getTitleKey());
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (windowId, inv, p) -> new CoinContainerMenu(windowId, inv, context, restore,
                        context.createHandler(p), containerId),
                title
        ), buf -> {
            buf.writeVarInt(item.getSlotCount());
            buf.writeUUID(containerId);
            context.write(buf);
            restore.write(buf);
        });
    }

    public OpenContext getOpenContext() {
        return this.openContext;
    }

    public RestoreTarget getRestoreTarget() {
        return this.restoreTarget;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public int getContainerSlotCount() {
        return this.handler.getSlots();
    }

    public void skipRestore() {
        this.restoreOnClose = false;
    }

    private void addContainerSlots() {
        for (int row = 0; row < this.layout.rows(); row++) {
            for (int col = 0; col < this.layout.columns(); col++) {
                int index = col + row * this.layout.columns();
                int x = this.layout.containerX() + col * 18;
                int y = this.layout.containerY() + row * 18;
                this.addSlot(new CoinSlot(this.handler, index, x, y));
            }
        }
    }

    private void addPlayerSlots(Inventory playerInv, int lockedSlot) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                this.addSlot(lockedSlot(playerInv, index, lockedSlot,
                        this.layout.playerInvX() + col * 18,
                        this.layout.playerInvY() + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(lockedSlot(playerInv, col, lockedSlot,
                    this.layout.playerInvX() + col * 18,
                    this.layout.hotbarY()));
        }
    }

    private Slot lockedSlot(Inventory playerInv, int index, int lockedSlot, int x, int y) {
        boolean locked = index == lockedSlot;
        return new Slot(playerInv, index, x, y) {
            @Override
            public boolean mayPickup(Player player) {
                return !locked && super.mayPickup(player);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return !locked && super.mayPlace(stack);
            }

            @Override
            public int getMaxStackSize() {
                ItemStack stack = this.getItem();
                int itemMax = stack.isEmpty() ? CoinHelper.COINS_PER_TIER : stack.getMaxStackSize();
                return Math.max(super.getMaxStackSize(), itemMax);
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                if (stack.isEmpty()) {
                    return super.getMaxStackSize(stack);
                }
                return Math.max(super.getMaxStackSize(stack), stack.getMaxStackSize());
            }
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            int containerSlots = this.handler.getSlots();
            if (index < containerSlots) {
                if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.handler.isItemValid(0, stack) || !this.moveItemStackTo(stack, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack live = this.openContext.getStack(player);
        if (live.isEmpty() || !(live.getItem() instanceof CoinContainerItem)) {
            return false;
        }
        UUID liveId = CoinContainerItem.getId(live);
        return this.openId.equals(liveId) && this.openContext.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer && ModCriteria.COIN_COUNT != null) {
            ModCriteria.COIN_COUNT.trigger(serverPlayer);
        }
        if (this.restoreOnClose && !this.restoreTarget.isNone() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.server.execute(() -> {
                if (serverPlayer.containerMenu == serverPlayer.inventoryMenu) {
                    this.restoreTarget.reopen(serverPlayer);
                }
            });
        }
    }
}
