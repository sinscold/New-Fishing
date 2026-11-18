package com.sinscold.newfishing.effect;

import com.sinscold.newfishing.entity.ReturnPortal;
import com.sinscold.newfishing.registry.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Эффект зелья возвращения: телепортирует выпившего на его точку возрождения. На месте, где выпили, остаётся портал на
 * точку возрождения, а у точки возрождения появляется портал обратно (каждый одноразовый и живёт 10 минут).
 */
public class ReturnEffect extends InstantenousMobEffect {
    public ReturnEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entity, int amplifier, double health) {
        if (!(entity instanceof ServerPlayer player) || !(entity.level() instanceof ServerLevel from)) {
            return;
        }
        ResourceKey<Level> toDimension = player.getRespawnDimension();
        ServerLevel to = player.server.getLevel(toDimension);
        BlockPos respawn = player.getRespawnPosition();
        if (to == null) {
            to = player.server.overworld();
            respawn = null;
        }
        if (respawn == null) {
            respawn = to.getSharedSpawnPos();
        }
        Vec3 destination = Vec3.atBottomCenterOf(respawn);
        Vec3 origin = player.position();

        // портал на месте, где выпили: ведёт на точку возрождения
        spawnPortal(from, origin, to.dimension(), destination);
        player.teleportTo(to, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        ReturnPortal.markTeleported(player);
        // портал у точки возрождения: ведёт обратно на место, где выпили (чуть в стороне, чтобы не сработать сразу)
        spawnPortal(to, destination.add(1.5, 0.0, 0.0), from.dimension(), origin);
    }

    private static void spawnPortal(ServerLevel level, Vec3 pos, ResourceKey<Level> targetDimension, Vec3 target) {
        ReturnPortal portal = new ReturnPortal(ModEntities.RETURN_PORTAL.get(), level);
        portal.setPos(pos.x, pos.y, pos.z);
        portal.setTarget(targetDimension, target);
        level.addFreshEntity(portal);
    }
}
