package com.solarbiscuit.ratcurrency.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class RestoreTarget {
    public enum Kind {
        NONE,
        BLOCK,
        ENDER,
        COIN
    }

    private final Kind kind;
    @Nullable
    private final BlockPos pos;
    @Nullable
    private final OpenContext coinContext;
    @Nullable
    private final RestoreTarget nestedRestore;

    private RestoreTarget(Kind kind, @Nullable BlockPos pos, @Nullable OpenContext coinContext,
                          @Nullable RestoreTarget nestedRestore) {
        this.kind = kind;
        this.pos = pos;
        this.coinContext = coinContext;
        this.nestedRestore = nestedRestore;
    }

    public static RestoreTarget none() {
        return new RestoreTarget(Kind.NONE, null, null, null);
    }

    public static RestoreTarget block(BlockPos pos) {
        return new RestoreTarget(Kind.BLOCK, pos, null, null);
    }

    /** {@code pos} is the ender chest block the inventory was opened from, or null if it wasn't opened from one. */
    public static RestoreTarget ender(@Nullable BlockPos pos) {
        return new RestoreTarget(Kind.ENDER, pos, null, null);
    }

    public static RestoreTarget coin(OpenContext context, RestoreTarget parentRestore) {
        return new RestoreTarget(Kind.COIN, null, context, parentRestore);
    }

    public Kind getKind() {
        return this.kind;
    }

    public boolean isNone() {
        return this.kind == Kind.NONE;
    }

    public static RestoreTarget fromCurrentMenu(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof CoinContainerMenu coinMenu) {
            return coin(coinMenu.getOpenContext(), coinMenu.getRestoreTarget());
        }
        // The player's own inventory has nothing to reopen; skip the block scan below.
        if (menu == player.inventoryMenu || menu instanceof InventoryMenu) {
            return none();
        }
        if (menu instanceof ChestMenu chestMenu) {
            if (chestMenu.getContainer() == player.getEnderChestInventory()) {
                return ender(findActiveEnderChest(player));
            }
            if (!chestMenu.slots.isEmpty()) {
                BlockPos pos = OpenContext.findBlockPos(player, chestMenu.getSlot(0));
                if (pos != null) {
                    return block(pos);
                }
            }
        }
        if (menu instanceof HopperMenu || menu instanceof ShulkerBoxMenu) {
            if (!menu.slots.isEmpty()) {
                BlockPos pos = OpenContext.findBlockPos(player, menu.getSlot(0));
                if (pos != null) {
                    return block(pos);
                }
            }
        }
        if (!menu.slots.isEmpty() && menu.getSlot(0).container != player.getInventory()) {
            BlockPos pos = OpenContext.findBlockPos(player, menu.getSlot(0));
            if (pos != null) {
                return block(pos);
            }
        }
        return none();
    }

    @Nullable
    private static BlockPos findActiveEnderChest(ServerPlayer player) {
        PlayerEnderChestContainer enderInv = player.getEnderChestInventory();
        BlockPos origin = player.blockPosition();
        int range = 8;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-range, -range, -range), origin.offset(range, range, range))) {
            if (player.level().getBlockEntity(pos) instanceof EnderChestBlockEntity chest && enderInv.isActiveChest(chest)) {
                return pos.immutable();
            }
        }
        return null;
    }

    public void reopen(ServerPlayer player) {
        switch (this.kind) {
            case NONE -> {
            }
            case BLOCK -> {
                if (this.pos == null) {
                    return;
                }
                // Ask the block, not the block entity: a chest's block entity only opens its own half.
                MenuProvider provider = player.level().getBlockState(this.pos).getMenuProvider(player.level(), this.pos);
                if (provider == null && player.level().getBlockEntity(this.pos) instanceof MenuProvider beProvider) {
                    provider = beProvider;
                }
                if (provider != null) {
                    NetworkHooks.openScreen(player, provider, this.pos);
                }
            }
            case ENDER -> {
                PlayerEnderChestContainer enderInv = player.getEnderChestInventory();
                if (this.pos != null) {
                    // Re-link the chest block like EnderChestBlock.use does: lid animation and distance check.
                    if (!(player.level().getBlockEntity(this.pos) instanceof EnderChestBlockEntity chest)) {
                        return;
                    }
                    enderInv.setActiveChest(chest);
                }
                player.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> ChestMenu.threeRows(id, inv, enderInv),
                        Component.translatable("container.enderchest")));
            }
            case COIN -> {
                if (this.coinContext != null) {
                    RestoreTarget parent = this.nestedRestore != null ? this.nestedRestore : none();
                    CoinContainerMenu.open(player, this.coinContext, parent);
                }
            }
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.kind);
        switch (this.kind) {
            case BLOCK -> buf.writeBlockPos(this.pos);
            case ENDER -> buf.writeNullable(this.pos, FriendlyByteBuf::writeBlockPos);
            case COIN -> {
                this.coinContext.write(buf);
                RestoreTarget parent = this.nestedRestore != null ? this.nestedRestore : none();
                parent.write(buf);
            }
            default -> {
            }
        }
    }

    public static RestoreTarget read(FriendlyByteBuf buf) {
        Kind kind = buf.readEnum(Kind.class);
        return switch (kind) {
            case NONE -> none();
            case BLOCK -> block(buf.readBlockPos());
            case ENDER -> ender(buf.readNullable(FriendlyByteBuf::readBlockPos));
            case COIN -> coin(OpenContext.read(buf), read(buf));
        };
    }
}
