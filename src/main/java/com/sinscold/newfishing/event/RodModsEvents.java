package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;
import com.sinscold.newfishing.registry.ModDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Слоты модификаций удочки (механика как у мешка) и запрет заброса без лески и крючка.
 * В инвентаре: ПКМ предметом на курсоре по удочке — вставить; ПКМ пустым курсором по удочке — достать (сначала крючок).
 */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class RodModsEvents {

    @SubscribeEvent
    static void onStackedOnRod(ItemStackedOnOtherEvent event) {
        ItemStack rod = event.getStackedOnItem();
        if (!rod.is(Items.FISHING_ROD)) return;
        if (event.getClickAction() != ClickAction.SECONDARY) return;
        Player player = event.getPlayer();
        if (!event.getSlot().allowModification(player)) return;

        ItemStack carried = event.getCarriedItem();
        RodMods mods = RodMods.get(rod);

        if (carried.isEmpty()) {
            // достать: сначала крючок, потом леску
            ItemStack removed;
            if (mods.hasHook()) {
                removed = mods.hook();
                rod.set(ModDataComponents.ROD_MODS.get(), mods.withHook(ItemStack.EMPTY));
            } else if (mods.hasLine()) {
                removed = mods.line();
                rod.set(ModDataComponents.ROD_MODS.get(), mods.withLine(ItemStack.EMPTY));
            } else {
                return;
            }
            event.getCarriedSlotAccess().set(removed);
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        } else if (RodMods.isLine(carried) && !mods.hasLine()) {
            rod.set(ModDataComponents.ROD_MODS.get(), mods.withLine(carried.split(1)));
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        } else if (RodMods.isHookSlotItem(carried) && mods.hasLine() && !mods.hasHook()) {
            rod.set(ModDataComponents.ROD_MODS.get(), mods.withHook(carried.split(1)));
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        } else {
            return;
        }
        event.setCanceled(true);
    }

    /** Без лески и/или крючка удочку закинуть нельзя (смотать уже закинутую можно). */
    @SubscribeEvent
    static void onUseRod(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.FISHING_ROD)) return;
        Player player = event.getEntity();
        if (player.fishing != null) return;

        // морковка/гриб на палке в слоте крючка: ускорение свиньи/лавохода вместо заброса
        if (player.getVehicle() instanceof ItemSteerable steerable) {
            ItemStack stick = RodMods.get(stack).hook();
            int damage = player.getVehicle() instanceof Pig && stick.is(Items.CARROT_ON_A_STICK) ? 7
                    : player.getVehicle() instanceof Strider && stick.is(Items.WARPED_FUNGUS_ON_A_STICK) ? 1 : 0;
            if (damage > 0) {
                if (!event.getLevel().isClientSide && steerable.boost()) {
                    stack.hurtAndBreak(damage, player, LivingEntity.getSlotForHand(event.getHand()));
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        RodMods mods = RodMods.get(stack);
        if (mods.canFish()) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        if (!event.getLevel().isClientSide) {
            String key = !mods.hasLine() && !mods.hasFishingHook() ? "both" : !mods.hasLine() ? "line" : "hook";
            player.displayClientMessage(Component.translatable("message.newfishing.rod_missing_" + key), true);
        }
    }

    private RodModsEvents() {}
}
