package com.sinscold.newfishing.item;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Пачка сигарет (пасхалка): ПКМ удерживать — закуриваешь (поднимает пачку к лицу, идёт дым), в конце тратится 1 прочность
 * (20 сигарет) и выдаётся скрытое достижение «Кайф». Больше ничего не даёт. Длительность задаёт компонент еды (по умолчанию 1,6 секунды).
 */
public class CigarettePackItem extends Item {
    private static final ResourceLocation ADVANCEMENT = ResourceLocation.fromNamespaceAndPath("newfishing", "kayf");

    public CigarettePackItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
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
        }
        stack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
        return stack;
    }
}
