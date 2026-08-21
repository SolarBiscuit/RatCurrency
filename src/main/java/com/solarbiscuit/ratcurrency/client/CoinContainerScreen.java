package com.solarbiscuit.ratcurrency.client;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CoinContainerScreen extends AbstractContainerScreen<CoinContainerMenu> {
    private static final ResourceLocation POUCH = ResourceLocation.fromNamespaceAndPath(RatCurrency.MOD_ID, "textures/gui/pouch.png");
    private static final ResourceLocation CASE = ResourceLocation.fromNamespaceAndPath(RatCurrency.MOD_ID, "textures/gui/case.png");
    private static final ResourceLocation CRATE = ResourceLocation.fromNamespaceAndPath(RatCurrency.MOD_ID, "textures/gui/crate.png");

    public CoinContainerScreen(CoinContainerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        CoinContainerMenu.Layout layout = menu.getLayout();
        this.imageWidth = layout.imageWidth();
        this.imageHeight = layout.imageHeight();
        this.inventoryLabelX = layout.playerInvX();
        this.inventoryLabelY = layout.playerInvY() - 11;
        this.titleLabelX = layout.containerX();
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.menu.getRestoreTarget().isNone()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.ratcurrency.back"), button -> this.onClose())
                    .bounds(this.leftPos + this.imageWidth - 50, this.topPos + 3, 46, 16)
                    .build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = textureForSlots(this.menu.getContainerSlotCount());
        CoinContainerMenu.Layout layout = this.menu.getLayout();
        graphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight,
                layout.textureWidth(), layout.textureHeight());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private static ResourceLocation textureForSlots(int slots) {
        if (slots == 9) {
            return POUCH;
        }
        if (slots == 96) {
            return CRATE;
        }
        return CASE;
    }
}
