package com.sinscold.newfishing.item;

import java.util.List;

import com.sinscold.newfishing.registry.ModDataComponents;
import com.sinscold.newfishing.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Пачка сигарет (пасхалка): как мешок, но только для сигарет (20 штук). В инвентаре ПКМ по пачке пустым курсором достаёт
 * одну сигарету (см. event/CigaretteEvents), ПКМ в руке кладёт одну сигарету в инвентарь.
 */
public class CigarettePackItem extends Item {
    public static final int MAX = 20;

    public CigarettePackItem(Properties properties) {
        super(properties);
    }

    public static int count(ItemStack pack) {
        return pack.getOrDefault(ModDataComponents.CIGARETTES.get(), 0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pack = player.getItemInHand(hand);
        int count = count(pack);
        if (count <= 0) {
            return InteractionResultHolder.fail(pack);
        }
        if (!level.isClientSide) {
            pack.set(ModDataComponents.CIGARETTES.get(), count - 1);
            ItemStack cigarette = new ItemStack(ModItems.CIGARETTE.get());
            if (!player.getInventory().add(cigarette)) {
                player.drop(cigarette, false);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(pack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.newfishing.cigarettes", count(stack), MAX).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return count(stack) < MAX;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * count(stack) / MAX);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xD07830;
    }
}
