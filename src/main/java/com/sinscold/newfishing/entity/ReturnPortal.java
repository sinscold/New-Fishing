package com.sinscold.newfishing.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Портал от зелья возвращения, живёт 10 минут. Рабочий портал стоит у точки возрождения: пропускает одного игрока
 * (любого) обратно на место, где выпили зелье. Портал на месте, где выпили, только для вида (не телепортирует).
 * Оба портала связаны общим `link`: когда через рабочий прошли, исчезают оба.
 */
public class ReturnPortal extends Entity {
    private static final int LIFETIME = 12000;
    /** Первые тики портал не срабатывает, чтобы не вернуть игрока сразу. */
    private static final int ARMING = 60;

    /** После перемещения игрок 2 секунды не может войти ни в один портал. */
    private static final int COOLDOWN = 40;
    private static final java.util.Map<java.util.UUID, Long> LAST_TELEPORT = new java.util.concurrent.ConcurrentHashMap<>();

    public static void markTeleported(ServerPlayer player) {
        LAST_TELEPORT.put(player.getUUID(), player.serverLevel().getGameTime());
    }

    private static boolean onCooldown(ServerPlayer player) {
        Long last = LAST_TELEPORT.get(player.getUUID());
        return last != null && player.serverLevel().getGameTime() - last < COOLDOWN;
    }

    private ResourceKey<Level> targetDimension;
    private Vec3 target = Vec3.ZERO;
    private int age;
    /** Рабочий портал (телепортирует) или только для вида. */
    private boolean functional;
    /** Общий номер пары порталов. */
    private java.util.UUID link;

    public ReturnPortal(EntityType<? extends ReturnPortal> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setTarget(ResourceKey<Level> dimension, Vec3 target) {
        this.targetDimension = dimension;
        this.target = target;
        this.functional = true;
    }

    public void setLink(java.util.UUID link) {
        this.link = link;
    }

    /** Звук перехода между измерениями у игрока, который прибыл на место (как после ванильного портала). */
    public static void playArrivalSound(ServerPlayer player) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelEventPacket(
                net.minecraft.world.level.block.LevelEvent.SOUND_PORTAL_TRAVEL, net.minecraft.core.BlockPos.ZERO, 0, false));
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
    public void tick() {
        super.tick();
        this.age++;
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL, this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                    this.getY() + this.random.nextDouble() * 2.0, this.getZ() + (this.random.nextDouble() - 0.5) * 0.8, 0.0, 0.05, 0.0);
            return;
        }
        if (this.age >= LIFETIME) {
            this.discard();
            return;
        }
        if (!this.functional || this.targetDimension == null || this.age < ARMING) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(0.3))) {
            if (onCooldown(player)) {
                continue;
            }
            ServerLevel to = serverLevel.getServer().getLevel(this.targetDimension);
            if (to == null) {
                continue;
            }
            player.teleportTo(to, this.target.x, this.target.y, this.target.z, player.getYRot(), player.getXRot());
            markTeleported(player);
            playArrivalSound(player);
            this.discard();
            // портал «для вида» стоит там, куда прибыл игрок: убираем и его
            if (this.link != null) {
                for (ReturnPortal other : to.getEntitiesOfClass(ReturnPortal.class, new net.minecraft.world.phys.AABB(this.target, this.target).inflate(4.0))) {
                    if (this.link.equals(other.link)) {
                        other.discard();
                    }
                }
            }
            return;
        }
    }
}
