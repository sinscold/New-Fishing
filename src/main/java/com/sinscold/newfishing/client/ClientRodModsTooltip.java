package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Два слота под подсказкой удочки (как содержимое мешка): леска и крючок со своими спрайтами слотов. */
public class ClientRodModsTooltip implements ClientTooltipComponent {
    private static final ResourceLocation LINE_SLOT = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_slot_line.png");
    private static final ResourceLocation HOOK_SLOT = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_slot_hook.png");
    private static final int SLOT = 18;
    private static final int GAP = 2;

    private final RodMods mods;

    public ClientRodModsTooltip(RodMods mods) {
        this.mods = mods;
    }

    @Override
    public int getHeight() {
        return SLOT + 4;
    }

    @Override
    public int getWidth(Font font) {
        return SLOT * 2 + GAP;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        drawSlot(graphics, font, LINE_SLOT, mods.line(), x, y + 2);
        drawSlot(graphics, font, HOOK_SLOT, mods.hook(), x + SLOT + GAP, y + 2);
    }

    private static void drawSlot(GuiGraphics graphics, Font font, ResourceLocation texture, ItemStack stack, int x, int y) {
        graphics.blit(texture, x, y, 0, 0, SLOT, SLOT, SLOT, SLOT);
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + 1, y + 1);
        }
    }
}
