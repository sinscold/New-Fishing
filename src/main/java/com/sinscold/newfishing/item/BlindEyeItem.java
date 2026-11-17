package com.sinscold.newfishing.item;

import com.sinscold.newfishing.entity.BlindEye;
import com.sinscold.newfishing.registry.ModEntities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Слепое око: запускается как око эндера, но никуда не летит, а парит на месте и покачивается. */
public class BlindEyeItem extends Item {
    public BlindEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BlindEye eye = new BlindEye(ModEntities.BLIND_EYE.get(), level);
            Vec3 look = player.getLookAngle();
            eye.setPos(player.getX() + look.x * 1.5, player.getEyeY() - 0.2 + look.y * 1.5, player.getZ() + look.z * 1.5);
            level.addFreshEntity(eye);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
