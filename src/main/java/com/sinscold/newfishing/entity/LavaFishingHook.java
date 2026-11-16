package com.sinscold.newfishing.entity;

import java.util.List;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.registry.ModEntities;
import com.sinscold.newfishing.registry.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Лавовый поплавок: то же, что ванильный, но «плавает» в лаве и ловит по таблице {@code newfishing:gameplay/lava_fishing}.
 * Наследует FishingHook, чтобы игрок (player.fishing) и рендерер работали как с обычным. Не горит (см. ModEntities).
 * Забрасывается только в Незере и только с незеритовыми леской и крючком (см. RodModsEvents).
 * Незеритовая леска не рвётся: дальность не ограничена. Незеритовый крючок: добыча держится втрое дольше.
 */
public class LavaFishingHook extends FishingHook {
    public static final ResourceKey<LootTable> LAVA_FISHING = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "gameplay/lava_fishing"));

    // Время ожидания (в тиках): в лаве клюёт заметно быстрее, чем в воде. Подбирается на глаз.
    private static final int LURE_MIN = 40;
    private static final int LURE_MAX = 200;
    private static final int HOOKED_MIN = 20;
    private static final int HOOKED_MAX = 60;
    private static final int BITE_MIN = 20;
    private static final int BITE_MAX = 40;
    /** Во сколько раз дольше держится добыча на незеритовом крючке. */
    private static final int BITE_MULTIPLIER = 3;

    private static final EntityDataAccessor<Boolean> LAVA_BITING =
            SynchedEntityData.defineId(LavaFishingHook.class, EntityDataSerializers.BOOLEAN);

    private enum State { FLYING, BOBBING }

    private State state = State.FLYING;
    private int outOfLavaTime;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private int lootLuck;
    private int lureTicks;

    public LavaFishingHook(EntityType<LavaFishingHook> type, Level level) {
        super(type, level);
    }

    /** Создаёт поплавок и «бросает» его от игрока (повторяет бросок ванильного). */
    public static LavaFishingHook cast(Player player, Level level, int luck, int lureSpeed) {
        LavaFishingHook hook = new LavaFishingHook(ModEntities.LAVA_FISHING_HOOK.get(), level);
        hook.lootLuck = Math.max(0, luck);
        hook.lureTicks = Math.max(0, lureSpeed);
        hook.setOwner(player);
        player.fishing = hook;

        float xRot = player.getXRot();
        float yRot = player.getYRot();
        float f2 = Mth.cos(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-xRot * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-xRot * ((float) Math.PI / 180F));
        double x = player.getX() - (double) f3 * 0.3;
        double y = player.getEyeY();
        double z = player.getZ() - (double) f2 * 0.3;
        hook.moveTo(x, y, z, yRot, xRot);
        Vec3 motion = new Vec3(-f3, Mth.clamp(-(f5 / f4), -5.0F, 5.0F), -f2);
        double length = motion.length();
        motion = motion.multiply(
                0.6 / length + level.random.triangle(0.5, 0.0103365),
                0.6 / length + level.random.triangle(0.5, 0.0103365),
                0.6 / length + level.random.triangle(0.5, 0.0103365));
        hook.setDeltaMovement(motion);
        hook.setYRot((float) (Mth.atan2(motion.x, motion.z) * 180.0F / (float) Math.PI));
        hook.setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * 180.0F / (float) Math.PI));
        hook.yRotO = hook.getYRot();
        hook.xRotO = hook.getXRot();
        return hook;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LAVA_BITING, false);
    }

    private boolean isBiting() {
        return this.getEntityData().get(LAVA_BITING);
    }

    private void setBiting(boolean biting) {
        this.getEntityData().set(LAVA_BITING, biting);
    }

    /** Поплавок нельзя зацепить за существ: лавовый только ныряет в лаву. */
    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
    }

    private boolean shouldStop(Player player) {
        boolean holdsRod = player.getMainHandItem().is(Items.FISHING_ROD) || player.getOffhandItem().is(Items.FISHING_ROD);
        // незеритовая леска не рвётся: расстояние не ограничено; но другое измерение или сломанная/убранная удочка прекращают рыбалку
        return player.isRemoved() || !player.isAlive() || !holdsRod || player.level() != this.level();
    }

    @Override
    public void tick() {
        Player player = this.getPlayerOwner();
        if (player == null) {
            this.discard();
            return;
        }
        if (!this.level().isClientSide && this.shouldStop(player)) {
            this.discard();
            return;
        }
        if (player.fishing == null) {
            player.fishing = this;
        }

        this.baseTick();

        BlockPos pos = this.blockPosition();
        FluidState fluid = this.level().getFluidState(pos);
        boolean inLava = fluid.is(FluidTags.LAVA);
        float height = inLava ? fluid.getHeight(this.level(), pos) : 0.0F;

        if (this.state == State.FLYING) {
            if (inLava) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                this.state = State.BOBBING;
                return;
            }
            if (!this.level().isClientSide) {
                HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
                if (hit.getType() != HitResult.Type.MISS) {
                    this.onHit(hit);
                }
            }
        } else {
            Vec3 motion = this.getDeltaMovement();
            double d0 = this.getY() + motion.y - pos.getY() - height;
            if (Math.abs(d0) < 0.01) {
                d0 += Math.signum(d0) * 0.1;
            }
            this.setDeltaMovement(motion.x * 0.9, motion.y - d0 * this.random.nextFloat() * 0.2, motion.z * 0.9);
            if (inLava) {
                this.outOfLavaTime = Math.max(0, this.outOfLavaTime - 1);
                if (this.isBiting()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.1 * this.random.nextFloat() * this.random.nextFloat(), 0.0));
                }
                if (!this.level().isClientSide) {
                    this.catchingFish(pos);
                }
            } else {
                this.outOfLavaTime = Math.min(10, this.outOfLavaTime + 1);
            }
        }

        if (!inLava) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.updateRotation();
        if (this.state == State.FLYING && (this.onGround() || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
        this.reapplyPosition();
    }

    private void catchingFish(BlockPos pos) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        if (this.nibble > 0) {
            this.nibble--;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                this.setBiting(false);
            }
        } else if (this.timeUntilHooked > 0) {
            this.timeUntilHooked--;
            if (this.timeUntilHooked > 0) {
                this.fishAngle += (float) this.random.triangle(0.0, 9.188);
                float angle = this.fishAngle * ((float) Math.PI / 180F);
                float sin = Mth.sin(angle);
                float cos = Mth.cos(angle);
                double px = this.getX() + (double) (sin * (float) this.timeUntilHooked * 0.1F);
                double py = (double) ((float) Mth.floor(this.getY()) + 1.0F);
                double pz = this.getZ() + (double) (cos * (float) this.timeUntilHooked * 0.1F);
                if (serverLevel.getBlockState(BlockPos.containing(px, py - 1.0, pz)).is(Blocks.LAVA)) {
                    if (this.random.nextFloat() < 0.15F) {
                        serverLevel.sendParticles(ParticleTypes.LAVA, px, py - 0.1, pz, 1, sin, 0.1, cos, 0.0);
                    }
                    serverLevel.sendParticles(ParticleTypes.SMOKE, px, py, pz, 0, cos * 0.04F, 0.01, -sin * 0.04F, 1.0);
                }
            } else {
                this.playSound(SoundEvents.LAVA_POP, 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double y = this.getY() + 0.5;
                serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), y, this.getZ(), (int) (1.0F + this.getBbWidth() * 20.0F), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
                serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), y, this.getZ(), (int) (1.0F + this.getBbWidth() * 20.0F), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
                this.nibble = Mth.nextInt(this.random, BITE_MIN, BITE_MAX) * BITE_MULTIPLIER;
                this.setBiting(true);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured--;
            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.timeUntilHooked = Mth.nextInt(this.random, HOOKED_MIN, HOOKED_MAX);
            }
        } else {
            this.timeUntilLured = Math.max(10, Mth.nextInt(this.random, LURE_MIN, LURE_MAX) - this.lureTicks);
        }
    }

    @Override
    public int retrieve(ItemStack rod) {
        Player player = this.getPlayerOwner();
        if (this.level().isClientSide || player == null || this.shouldStop(player)) {
            return 0;
        }
        int damage = 0;
        if (this.nibble > 0) {
            LootParams params = new LootParams.Builder((ServerLevel) this.level())
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.TOOL, rod)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ATTACKING_ENTITY, this.getOwner())
                    .withLuck((float) this.lootLuck + player.getLuck())
                    .create(LootContextParamSets.FISHING);
            LootTable table = this.level().getServer().reloadableRegistries().getLootTable(LAVA_FISHING);
            List<ItemStack> items = table.getRandomItems(params);
            for (ItemStack stack : items) {
                ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
                itemEntity.setInvulnerable(true);
                double dx = player.getX() - this.getX();
                double dy = player.getY() - this.getY();
                double dz = player.getZ() - this.getZ();
                itemEntity.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
                this.level().addFreshEntity(itemEntity);
                this.level().addFreshEntity(new ExperienceOrb(this.level(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, this.random.nextInt(6) + 1));
                if (stack.is(ItemTags.FISHES) || stack.is(ModTags.NETHER_FISH)) {
                    player.awardStat(Stats.FISH_CAUGHT, 1);
                }
            }
            damage = this.onGround() ? 2 : 1;
        }
        this.discard();
        return damage;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        Player player = this.getPlayerOwner();
        if (player != null && player.fishing == this) {
            player.fishing = null;
        }
        super.remove(reason);
    }
}
