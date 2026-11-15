package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Два слота под подсказкой удочки (как содержимое мешка): леска и крючок. Слот 18×18 в стиле инвентаря, иконка пустого слота 16×16. */
public class ClientRodModsTooltip implements ClientTooltipComponent {
    private static final ResourceLocation LINE_SLOT = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_slot_line.png");
    private static final ResourceLocation HOOK_SLOT = ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "textures/gui/rod_slot_hook.png");
    private static final int SLOT = 18;
    private static final int ICON = 16;
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
        drawSlot(graphics, LINE_SLOT, mods.line(), x, y + 2);
        drawSlot(graphics, HOOK_SLOT, mods.hook(), x + SLOT + GAP, y + 2);
    }

    private static void drawSlot(GuiGraphics graphics, ResourceLocation emptyIcon, ItemStack stack, int x, int y) {
        // фон слота как в инвентаре: тёмная рамка сверху/слева, светлая снизу/справа, серая заливка
        graphics.fill(x, y, x + SLOT, y + SLOT, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + SLOT, y + SLOT, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0xFF8B8B8B);
        if (stack.isEmpty()) {
            graphics.blit(emptyIcon, x + 1, y + 1, 0, 0, ICON, ICON, ICON, ICON);
        } else {
            graphics.renderItem(stack, x + 1, y + 1);
        }
    }
}
