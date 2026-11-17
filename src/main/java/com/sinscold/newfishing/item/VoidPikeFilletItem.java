package com.sinscold.newfishing.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/** Филе пустотной щуки: при съедании телепортирует, как плод хоруса. */
public class VoidPikeFilletItem extends Item {
    public VoidPikeFilletItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            double x0 = entity.getX();
            double y0 = entity.getY();
            double z0 = entity.getZ();
            for (int i = 0; i < 16; i++) {
                double x = x0 + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                double y = Mth.clamp(y0 + (double) (entity.getRandom().nextInt(16) - 8),
                        (double) level.getMinBuildHeight(), (double) (level.getMaxBuildHeight() - 1));
                double z = z0 + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                if (entity.isPassenger()) {
                    entity.stopRiding();
                }
                Vec3 from = entity.position();
                if (entity.randomTeleport(x, y, z, true)) {
                    level.gameEvent(GameEvent.TELEPORT, from, GameEvent.Context.of(entity));
                    level.playSound(null, x0, y0, z0, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    entity.resetFallDistance();
                    break;
                }
            }
        }
        return result;
    }
}
