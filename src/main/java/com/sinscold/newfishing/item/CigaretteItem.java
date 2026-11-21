package com.sinscold.newfishing.item;

import com.sinscold.newfishing.registry.ModTags;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Сигарета: закуривается в правой руке при зажигалке (огниво) в левой. Свежие сигареты стакаются, надкуренная получает
 * прочность 3 (по одной затяжке) и перестаёт стакаться. Закуривание ничего не даёт, кроме скрытого достижения «Кайф».
 */
public class CigaretteItem extends Item {
    public static final int USES = 3;
    private static final ResourceLocation ADVANCEMENT = ResourceLocation.fromNamespaceAndPath("newfishing", "kayf");

    public CigaretteItem(Properties properties) {
        super(properties);
    }

    /** Надкурена ли сигарета (получила прочность и перестала стакаться). */
    public static boolean isSmoked(ItemStack stack) {
        return stack.has(DataComponents.MAX_DAMAGE);
    }

    private static void markSmoked(ItemStack stack) {
        stack.set(DataComponents.MAX_STACK_SIZE, 1);
        stack.set(DataComponents.MAX_DAMAGE, USES);
        stack.set(DataComponents.DAMAGE, 0);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.getOffhandItem().is(ModTags.LIGHTERS)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.newfishing.need_lighter"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!isSmoked(stack) && stack.getCount() > 1) {
            // из стопки закуривается одна, остальные возвращаются в инвентарь
            ItemStack single = stack.copyWithCount(1);
            ItemStack rest = stack.copyWithCount(stack.getCount() - 1);
            player.setItemInHand(hand, single);
            if (!player.getInventory().add(rest) && !level.isClientSide) {
                player.drop(rest, false);
            }
            stack = single;
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && remainingUseDuration % 6 == 0) {
            Vec3 look = entity.getLookAngle();
            level.addParticle(ParticleTypes.SMOKE, entity.getX() + look.x * 0.4, entity.getEyeY() - 0.15 + look.y * 0.4,
                    entity.getZ() + look.z * 0.4, 0.0, 0.02, 0.0);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel) {
            Vec3 look = entity.getLookAngle();
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, entity.getX() + look.x * 0.5, entity.getEyeY() - 0.15 + look.y * 0.5,
                    entity.getZ() + look.z * 0.5, 6, 0.1, 0.05, 0.1, 0.01);
            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.3F, 1.6F);
            if (entity instanceof ServerPlayer player) {
                AdvancementHolder advancement = serverLevel.getServer().getAdvancements().get(ADVANCEMENT);
                if (advancement != null) {
                    player.getAdvancements().award(advancement, "smoke");
                }
            }
            // зажигалка тратит 1 прочность на каждую затяжку
            ItemStack lighter = entity.getOffhandItem();
            if (lighter.is(ModTags.LIGHTERS)) {
                lighter.hurtAndBreak(1, entity, EquipmentSlot.OFFHAND);
            }
        }
        if (!isSmoked(stack)) {
            markSmoked(stack);
        }
        stack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
        return stack;
    }
}
