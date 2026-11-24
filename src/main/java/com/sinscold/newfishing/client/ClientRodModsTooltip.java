package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Три слота под подсказкой удочки (как содержимое мешка): леска, крючок и наживка. Слот 18×18 в стиле инвентаря, иконка пустого слота 16×16. */
public class ClientRodModsTooltip implements ClientTooltipComponent {
    private static final ResourceLocation FRAME = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_tooltip_modification_slots.png");
    private static final ResourceLocation LINE_SLOT = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_slot_line.png");
    private static final ResourceLocation HOOK_SLOT = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_slot_hook.png");
    private static final ResourceLocation BAIT_SLOT = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_slot_bait.png");
    /** Размер рамки (rod_tooltip_modification_slots.png) и позиции внутренних областей слотов 16×16. */
    private static final int FRAME_W = 56;
    private static final int FRAME_H = 22;
    private static final int ICON = 16;
    private static final int LINE_X = 2;
    private static final int HOOK_X = 20;
    private static final int BAIT_X = 38;
    private static final int SLOT_Y = 2;

    private final RodMods mods;
    /** Для сумки для наживки: три слота наживки вместо деталей удочки (иначе null). */
    private final java.util.List<ItemStack> bagItems;

    public ClientRodModsTooltip(RodMods mods) {
        this.mods = mods;
        this.bagItems = null;
    }

    private ClientRodModsTooltip(java.util.List<ItemStack> bagItems) {
        this.mods = RodMods.EMPTY;
        this.bagItems = bagItems;
    }

    /** Подсказка сумки для наживки: та же рамка, три слота наживки с количеством. */
    public static ClientRodModsTooltip forBag(com.sinscold.newfishing.item.BaitBag.Contents contents) {
        return new ClientRodModsTooltip(contents.items());
    }

    @Override
    public int getHeight() {
        return FRAME_H + 7; // отступ под рамкой до строк зачарований
    }

    @Override
    public int getWidth(Font font) {
        return FRAME_W;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.blit(FRAME, x, y + 1, 0, 0, FRAME_W, FRAME_H, FRAME_W, FRAME_H);
        if (this.bagItems != null) {
            int[] xs = {LINE_X, HOOK_X, BAIT_X};
            for (int i = 0; i < xs.length; i++) {
                ItemStack stack = i < this.bagItems.size() ? this.bagItems.get(i) : ItemStack.EMPTY;
                drawSlot(graphics, BAIT_SLOT, stack, x + xs[i], y + 1 + SLOT_Y);
                if (!stack.isEmpty()) {
                    graphics.renderItemDecorations(font, stack, x + xs[i], y + 1 + SLOT_Y);
                }
            }
            return;
        }
        drawSlot(graphics, LINE_SLOT, mods.line(), x + LINE_X, y + 1 + SLOT_Y);
        drawSlot(graphics, HOOK_SLOT, mods.hook(), x + HOOK_X, y + 1 + SLOT_Y);
        drawSlot(graphics, BAIT_SLOT, mods.bait(), x + BAIT_X, y + 1 + SLOT_Y);
    }

    private static void drawSlot(GuiGraphics graphics, ResourceLocation emptyIcon, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            graphics.blit(emptyIcon, x, y, 0, 0, ICON, ICON, ICON, ICON);
        } else {
            graphics.renderItem(stack, x, y);
        }
    }
}
