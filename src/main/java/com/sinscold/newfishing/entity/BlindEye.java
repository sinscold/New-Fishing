package com.sinscold.newfishing.entity;

import com.sinscold.newfishing.registry.ModItems;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Брошенное слепое око: 10 секунд парит, покачиваясь вверх и вниз, затем с шансом 4 к 5 падает предметом
 * (как обычное око эндера), иначе разбивается.
 */
public class BlindEye extends Entity implements ItemSupplier {
    private static final int LIFETIME = 200;
    private int life;
    private double baseY;

    public BlindEye(EntityType<? extends BlindEye> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.BLIND_EYE.get());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.life == 0) {
            this.baseY = this.getY();
        }
        this.life++;
        // покачивание вверх и вниз
        this.setPos(this.getX(), this.baseY + Math.sin(this.life * 0.12) * 0.6 + Math.min(this.life, 20) * 0.02, this.getZ());
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.PORTAL, this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() - 0.2 + this.random.nextDouble() * 0.5, this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.2, -0.1, (this.random.nextDouble() - 0.5) * 0.2);
        } else if (this.life >= LIFETIME) {
            // как у ока эндера: в конце парения звук, затем предмет падает или око разбивается
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
            this.discard();
            if (this.random.nextInt(5) > 0) {
                this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
            } else {
                this.level().levelEvent(2003, this.blockPosition(), 0);
            }
        }
    }
}
