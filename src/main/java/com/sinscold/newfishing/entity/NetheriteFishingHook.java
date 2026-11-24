package com.sinscold.newfishing.entity;

import java.util.List;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.LineBreak;
import com.sinscold.newfishing.item.RodMods;
import com.sinscold.newfishing.registry.ModSounds;
import com.sinscold.newfishing.registry.ModEntities;
import com.sinscold.newfishing.registry.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Особый поплавок: плавает на любой жидкости (вода, лава, модовые), цепляет существ, а в Крае плавает в «зоне пустоты».
 * Используется для любого заброса (наживка и очки рыбалки, см. FishingScore). В лаве выдерживает только полный незеритовый комплект
 * (иначе леска или поплавок сгорают и рыбалка отменяется).
 * Клюёт только в жидкости, для которой есть таблица лута:
 * вода — ванильная {@code gameplay/fishing}; лава — {@code newfishing:gameplay/lava_fishing} (только в Незере и только на
 * незеритовой леске); другая жидкость {@code ns:path} — {@code ns:gameplay/fishing/path} (если такая таблица есть).
 * Для пустоты (Край, Y от 10 до 50) нужны фантомная леска и эндер-поплавок (остальные поплавки тонут): ловит по таблице {@code newfishing:gameplay/void_fishing}.
 * Обычная леска рвётся 1 к 7 при вытаскивании поклёвки (LineBreak), незеритовая нет. Добыча держится втрое дольше.
 * Наследует FishingHook, чтобы игрок (player.fishing) и рендерер работали как с обычным.
 */
public class NetheriteFishingHook extends FishingHook {
    public static final ResourceKey<LootTable> LAVA_FISHING = lootKey(NewFishing.MODID, "gameplay/lava_fishing");
    public static final ResourceKey<LootTable> VOID_FISHING = lootKey(NewFishing.MODID, "gameplay/void_fishing");
    /** Зона пустоты в Крае: поплавок «застревает» на любой высоте в этом диапазоне. */
    public static final double VOID_MIN_Y = 10.0;
    public static final double VOID_MAX_Y = 50.0;
    /** Дальше этого расстояния от игрока (по горизонтали) поплавок в пустоте разворачивается к нему; вдвое дальше — леска обрывается. */
    private static final double VOID_LEASH = 40.0;

    // Время ожидания (в тиках). Вода — как в ваниле, лава быстрее. Подбирается на глаз.
    private static final int LAVA_LURE_MIN = 100;
    private static final int LAVA_LURE_MAX = 400;
    private static final int WATER_LURE_MIN = 100;
    private static final int WATER_LURE_MAX = 600;
    private static final int VOID_LURE_MIN = 300;
    private static final int VOID_LURE_MAX = 1000;
    private static final int HOOKED_MIN = 20;
    private static final int HOOKED_MAX = 80;
    private static final int BITE_MIN = 20;
    private static final int BITE_MAX = 40;
    /** Во сколько раз дольше держится добыча на незеритовом крючке. */
    private static final int BITE_MULTIPLIER = 3;

    /** Какие детали удочки установлены (биты), синхронизируется с клиентом: от этого зависит и физика поплавка, и вид. */
    private static final EntityDataAccessor<Integer> PARTS =
            SynchedEntityData.defineId(NetheriteFishingHook.class, EntityDataSerializers.INT);
    private static final int PART_NETHERITE_LINE = 1;
    private static final int PART_PHANTOM_LINE = 2;
    private static final int PART_NETHERITE_HOOK = 4;
    private static final int PART_ENDER_HOOK = 8;
    private static final EntityDataAccessor<Boolean> BITING =
            SynchedEntityData.defineId(NetheriteFishingHook.class, EntityDataSerializers.BOOLEAN);

    private int outOfFluidTime;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    /** Удача лута и множитель ожидания рыбы из очков рыбалки (FishingScore), считаются при забросе. */
    private float lootLuck;
    private double waitFactor = 1.0;
    /** Наживка: 0 подходит, 1 нет наживки, 2 наживка не для этого измерения. Обновляется раз в 10 тиков. */
    private int baitState = 1;
    /** Сколько тиков поплавок ждёт в жидкости без подходящей наживки (подсказка через 5 секунд, один раз). */
    private int noBaitTicks;
    private static final int NO_BAIT_HINT_TICKS = 100;
    /** Шанс, что наживка потратится после улова. */
    private static final float BAIT_USE_CHANCE = 1.0F / 3.0F;
    private boolean flying = true;
    private ResourceKey<LootTable> biteTable;
    private Vec3 voidAnchor;
    private int jerkCooldown;
    private int dashTicks;
    private int dashDuration;
    private double dashDistance;
    private Vec3 dashDir = Vec3.ZERO;
    private boolean dashFast;

    public NetheriteFishingHook(EntityType<NetheriteFishingHook> type, Level level) {
        super(type, level);
    }

    private static ResourceKey<LootTable> lootKey(String namespace, String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    /** Создаёт поплавок и «бросает» его от игрока (повторяет бросок ванильного). */
    public static NetheriteFishingHook cast(Player player, Level level, double score, boolean netheriteLine, boolean phantomLine, boolean netheriteHook, boolean enderHook) {
        NetheriteFishingHook hook = new NetheriteFishingHook(ModEntities.NETHERITE_FISHING_HOOK.get(), level);
        hook.lootLuck = com.sinscold.newfishing.item.FishingScore.lootLuck(score);
        hook.waitFactor = com.sinscold.newfishing.item.FishingScore.waitFactor(score);
        hook.baitState = baitStateOf(player, level);
        hook.getEntityData().set(PARTS, (netheriteLine ? PART_NETHERITE_LINE : 0) | (phantomLine ? PART_PHANTOM_LINE : 0)
                | (netheriteHook ? PART_NETHERITE_HOOK : 0) | (enderHook ? PART_ENDER_HOOK : 0));
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
        builder.define(BITING, false);
        builder.define(PARTS, 0);
    }

    /** Для рендерера: рисовать поплавок с текстурой незеритового крючка. */
    public boolean hasNetheriteHookTexture() {
        return this.hasNetheriteHook();
    }

    /** Для рендерера: поплавок с текстурой эндер-крючка. */
    public boolean hasEnderHookTexture() {
        return this.hasEnderHook();
    }

    /** Цвет лески для рендерера: 0 обычная, 1 незеритовая, 2 фантомная. */
    public int lineColorKind() {
        return this.hasPhantomLine() ? 2 : this.hasNetheriteLine() ? 1 : 0;
    }

    private boolean hasNetheriteLine() {
        return (this.getEntityData().get(PARTS) & PART_NETHERITE_LINE) != 0;
    }

    private boolean hasPhantomLine() {
        return (this.getEntityData().get(PARTS) & PART_PHANTOM_LINE) != 0;
    }

    private boolean hasNetheriteHook() {
        return (this.getEntityData().get(PARTS) & PART_NETHERITE_HOOK) != 0;
    }

    private boolean hasEnderHook() {
        return (this.getEntityData().get(PARTS) & PART_ENDER_HOOK) != 0;
    }

    private boolean isBiting() {
        return this.getEntityData().get(BITING);
    }

    private void setBiting(boolean biting) {
        this.getEntityData().set(BITING, biting);
    }

    private boolean shouldStop(Player player) {
        // леска не ограничивает дальность; но другое измерение, убранная удочка или вытащенная из неё леска/крючок прекращают рыбалку
        if (player.isRemoved() || !player.isAlive() || player.level() != this.level()) return true;
        // осмотр удочки дорогой по сравнению с остальным, поэтому раз в 10 тиков; мгновенная отмена идёт по событию извлечения детали
        if (this.tickCount % 10 != 0) return false;
        if (!RodMods.holdsFishableRod(player)) return true;
        this.baitState = baitStateOf(player, this.level()); // наживку могли насадить уже во время рыбалки
        return false;
    }

    private static int baitStateOf(Player player, Level level) {
        ItemStack bait = RodMods.get(RodMods.heldRod(player)).bait();
        if (bait.isEmpty()) return 1;
        return com.sinscold.newfishing.item.FishingScore.baitWorks(bait, level) ? 0 : 2;
    }

    /** Таблица лута для жидкости в этой точке или null, если здесь не клюёт. */
    private ResourceKey<LootTable> tableFor(FluidState state, boolean inVoid) {
        if (inVoid) {
            return this.hasPhantomLine() ? VOID_FISHING : null; // inVoid бывает только у эндер-поплавка
        }
        if (state.is(FluidTags.LAVA)) {
            // клёв и анимации в лаве везде (на незеритовой леске), а улов только в Незере (см. retrieve)
            return this.hasNetheriteLine() ? LAVA_FISHING : null;
        }
        if (state.is(FluidTags.WATER)) {
            return BuiltInLootTables.FISHING;
        }
        Fluid fluid = state.getType() instanceof FlowingFluid flowing ? flowing.getSource() : state.getType();
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        ResourceKey<LootTable> key = lootKey(id.getNamespace(), "gameplay/fishing/" + id.getPath());
        LootTable table = this.level().getServer() == null ? LootTable.EMPTY
                : this.level().getServer().reloadableRegistries().getLootTable(key);
        return table == LootTable.EMPTY ? null : key;
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

        // зацепили существо: висим на нём
        Entity hooked = this.getHookedIn();
        if (hooked != null) {
            if (hooked.isRemoved() || hooked.level().dimension() != this.level().dimension()) {
                this.discard();
                return;
            }
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(hooked.getX(), hooked.getY(0.8), hooked.getZ());
            return;
        }

        BlockPos pos = this.blockPosition();
        FluidState fluid = this.level().getFluidState(pos);
        boolean inFluid = !fluid.isEmpty();
                // в пустоте парит только эндер-поплавок; остальные поплавки тонут (падают в бездну)
        boolean inVoid = this.hasEnderHook() && !inFluid && this.level().dimension() == Level.END && this.getY() <= VOID_MAX_Y;
        boolean floating = inFluid || inVoid;
        float height = inFluid ? fluid.getHeight(this.level(), pos) : 0.0F;

        // в лаве выдерживает только полный незеритовый комплект: любая другая леска сгорает сразу, обычный крючок тоже
        if (inFluid && fluid.is(FluidTags.LAVA) && !(this.hasNetheriteLine() && this.hasNetheriteHook())) {
            if (!this.level().isClientSide) {
                LineBreak.burn(player, this, !this.hasNetheriteLine());
            }
            return;
        }

        if (this.flying) {
            if (floating) {
                if (!this.level().isClientSide && inFluid && fluid.is(FluidTags.LAVA)) {
                    this.playSound(ModSounds.LAVA_BLOP.get(), 1.0F, 1.0F);
                }
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                this.flying = false;
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
            if (inVoid) {
                // в пустоте нет поверхности: поплавок повисает и дёргается рывками на небольшое расстояние
                this.setDeltaMovement(motion.x * 0.9, motion.y * 0.6, motion.z * 0.9);
                if (!this.level().isClientSide) {
                    this.voidTick();
                }
            } else if (!inFluid) {
                // над жидкостью плавучести нет: только затухание по горизонтали, вертикаль даёт гравитация
                this.setDeltaMovement(motion.x * 0.9, motion.y, motion.z * 0.9);
            } else {
                // поверхность жидкости: поднимаемся вверх по столбцу, пока жидкость того же вида
                int topY = pos.getY();
                while (topY - pos.getY() < 16
                        && this.level().getFluidState(new BlockPos(pos.getX(), topY + 1, pos.getZ())).getType().isSame(fluid.getType())) {
                    topY++;
                }
                BlockPos topPos = new BlockPos(pos.getX(), topY, pos.getZ());
                double surface = topY + this.level().getFluidState(topPos).getHeight(this.level(), topPos);
                double d0 = this.getY() + motion.y - surface;
                if (Math.abs(d0) < 0.01) {
                    d0 += Math.signum(d0) * 0.1;
                }
                double vy = motion.y - d0 * this.random.nextFloat() * 0.2;
                if (d0 < -0.3) {
                    vy += Math.min(0.08, -d0 * 0.1); // ушёл глубоко: дополнительно выталкиваем к поверхности
                }
                this.setDeltaMovement(motion.x * 0.9, vy, motion.z * 0.9);
            }
            if (floating) {
                this.outOfFluidTime = Math.max(0, this.outOfFluidTime - 1);
                if (this.isBiting()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.1 * this.random.nextFloat() * this.random.nextFloat(), 0.0));
                }
                if (!this.level().isClientSide) {
                    ResourceKey<LootTable> table = this.tableFor(fluid, inVoid);
                    if (table != null) {
                        this.catchingFish(pos, fluid, inVoid, table);
                    }
                }
            } else {
                this.outOfFluidTime = Math.min(10, this.outOfFluidTime + 1);
            }
        }

        if (!floating) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.updateRotation();
        if (this.flying && (this.onGround() || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
        this.reapplyPosition();
        // зона пустоты по высоте ограничена жёстко: ниже нижней границы поплавок поднимается обратно
        if (inVoid && !this.flying && this.getY() < VOID_MIN_Y + 0.5) {
            this.setPos(this.getX(), VOID_MIN_Y + 0.5, this.getZ());
            this.setDeltaMovement(this.getDeltaMovement().x, Math.max(0.0, this.getDeltaMovement().y), this.getDeltaMovement().z);
        }
    }

    /**
     * Рывок в случайную сторону на distance блоков за duration тиков. Обычный рывок: разгон и плавное замедление
     * (скорость ~ 6u(1−u)); при поклёвке (fast): резкий старт и быстрое затухание. Если поплавок ушёл далеко от места
     * остановки, рывок направлен обратно.
     */
    private void startDash(double distance, int duration, boolean fast) {
        Player owner = this.getPlayerOwner();
        double ownerDistance = owner == null ? 0.0 : Math.hypot(owner.getX() - this.getX(), owner.getZ() - this.getZ());
        if (owner != null && ownerDistance > VOID_LEASH) {
            // слишком далеко от игрока: рывок в его сторону
            this.dashDir = new Vec3(owner.getX() - this.getX(), (owner.getY() + 20.0 - this.getY()) * 0.2, owner.getZ() - this.getZ()).normalize();
        } else if (this.voidAnchor != null && this.position().distanceTo(this.voidAnchor) > 40.0) {
            this.dashDir = this.voidAnchor.subtract(this.position()).normalize();
        } else {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            this.dashDir = new Vec3(Math.cos(angle), (this.random.nextDouble() - 0.5) * 0.1, Math.sin(angle)).normalize();
        }
        // не выходить за границы зоны по высоте
        if (this.getY() > VOID_MAX_Y - 3.0) {
            this.dashDir = new Vec3(this.dashDir.x, -0.3, this.dashDir.z).normalize();
        } else if (this.getY() < VOID_MIN_Y + 3.0) {
            this.dashDir = new Vec3(this.dashDir.x, 0.3, this.dashDir.z).normalize();
        }
        this.dashDistance = distance;
        this.dashDuration = duration;
        this.dashTicks = duration;
        this.dashFast = fast;
    }

    /** Поведение поплавка в зоне пустоты: рывки на 1–3 блока и одиночные частицы эндер-жемчуга из поплавка. */
    private void voidTick() {
        if (this.voidAnchor == null) {
            this.voidAnchor = this.position();
            NewFishing.LOGGER.info("Поплавок в зоне пустоты на высоте Y={} (зона {}…{})", this.getY(), VOID_MIN_Y, VOID_MAX_Y);
        }
        Player leashOwner = this.getPlayerOwner();
        if (leashOwner != null && Math.hypot(leashOwner.getX() - this.getX(), leashOwner.getZ() - this.getZ()) > VOID_LEASH * 2.0) {
            this.discard(); // леска оборвалась от слишком большого расстояния
            return;
        }
        if (this.dashTicks > 0) {
            double u = ((double) (this.dashDuration - this.dashTicks) + 0.5) / (double) this.dashDuration;
            double profile = this.dashFast ? 3.0 * (1.0 - u) * (1.0 - u) : 6.0 * u * (1.0 - u);
            this.setDeltaMovement(this.dashDir.scale(this.dashDistance * profile / (double) this.dashDuration));
            this.dashTicks--;
            // одиночная частица, вылетающая из поплавка
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.1, this.getZ(), 0,
                    (this.random.nextDouble() - 0.5) * 2.0, this.random.nextDouble() - 0.3, (this.random.nextDouble() - 0.5) * 2.0, 0.5);
        } else if (this.jerkCooldown-- <= 0) {
            this.jerkCooldown = 20 + this.random.nextInt(50);
            double distance = 2.0 + this.random.nextDouble() * 3.0; // 2–5 блоков, средняя скорость 0,15 блока/тик
            this.startDash(distance, (int) Math.ceil(distance / 0.15), false);
        }
    }

    private void catchingFish(BlockPos pos, FluidState fluid, boolean inVoid, ResourceKey<LootTable> table) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        boolean lava = !inVoid && fluid.is(FluidTags.LAVA);
        if (this.nibble > 0) {
            this.nibble--;
            if (inVoid && this.nibble % 6 == 0) {
                // пока клюёт в пустоте, из поплавка идёт мерцание: видно, что пора вытаскивать
                serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.3, this.getZ(), 3, 0.15, 0.2, 0.15, 0.03);
            }
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
                if (inVoid) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL, px, this.getY(), pz, 2, 0.05, 0.05, 0.05, 0.1);
                } else if (!serverLevel.getFluidState(BlockPos.containing(px, py - 1.0, pz)).isEmpty()) {
                    if (lava) {
                        // подходящая рыба в лаве: частицы лавы
                        serverLevel.sendParticles(ParticleTypes.LAVA, px, py, pz, 1, 0.05, 0.0, 0.05, 0.0);
                    } else {
                        if (this.random.nextFloat() < 0.15F) {
                            serverLevel.sendParticles(ParticleTypes.BUBBLE, px, py - 0.1, pz, 1, sin, 0.1, cos, 0.0);
                        }
                        float dx = sin * 0.04F;
                        float dz = cos * 0.04F;
                        serverLevel.sendParticles(ParticleTypes.FISHING, px, py, pz, 0, dz, 0.01, -dx, 1.0);
                        serverLevel.sendParticles(ParticleTypes.FISHING, px, py, pz, 0, -dz, 0.01, dx, 1.0);
                    }
                }
            } else {
                double y = this.getY() + 0.5;
                int count = (int) (1.0F + this.getBbWidth() * 20.0F);
                if (inVoid) {
                    this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F); // звук разбивающегося глаза эндера: пора вытаскивать
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.3F, 1.5F);
                    serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), y, this.getZ(), 24, 0.3, 0.3, 0.3, 0.12);
                    this.startDash(6.0 + this.random.nextDouble() * 4.0, 12, true); // при поклёвке резко улетает в сторону
                    serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), y, this.getZ(), count * 2, this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
                } else if (lava) {
                    this.playSound(ModSounds.LAVA_BULB.get(), 1.0F, 1.0F);
                    serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), y, this.getZ(), count, this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), y, this.getZ(), count, this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
                } else {
                    this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                    serverLevel.sendParticles(ParticleTypes.BUBBLE, this.getX(), y, this.getZ(), count, this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
                    serverLevel.sendParticles(ParticleTypes.FISHING, this.getX(), y, this.getZ(), count, this.getBbWidth(), 0.0, this.getBbWidth(), 0.2);
                }
                this.nibble = Mth.nextInt(this.random, BITE_MIN, BITE_MAX) * BITE_MULTIPLIER;
                this.biteTable = table;
                this.setBiting(true);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured--;
            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.timeUntilHooked = Mth.nextInt(this.random, HOOKED_MIN, HOOKED_MAX);
            }
        } else if (this.baitState != 0) {
            // без подходящей наживки рыба к поплавку не плывёт; через 5 секунд подсказка (один раз за заброс)
            if (++this.noBaitTicks == NO_BAIT_HINT_TICKS && this.getPlayerOwner() != null) {
                this.getPlayerOwner().displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        this.baitState == 1 ? "message.newfishing.forgot_bait" : "message.newfishing.bait_wrong_dimension"), true);
            }
        } else {
            int min = inVoid ? VOID_LURE_MIN : lava ? LAVA_LURE_MIN : WATER_LURE_MIN;
            int max = inVoid ? VOID_LURE_MAX : lava ? LAVA_LURE_MAX : WATER_LURE_MAX;
            this.timeUntilLured = Math.max(10, (int) (Mth.nextInt(this.random, min, max) * this.waitFactor));
        }
    }

    @Override
    public int retrieve(ItemStack rod) {
        Player player = this.getPlayerOwner();
        if (this.level().isClientSide || player == null || this.shouldStop(player)) {
            return 0;
        }
        int damage = 0;
        Entity hooked = this.getHookedIn();
        if (hooked != null) {
            // подтягиваем зацепленное существо к игроку
            Vec3 pull = new Vec3(player.getX() - this.getX(), player.getY() - this.getY(), player.getZ() - this.getZ()).scale(0.1);
            hooked.setDeltaMovement(hooked.getDeltaMovement().add(pull));
            damage = 5;
        } else if (this.nibble > 0 && this.biteTable == LAVA_FISHING && this.level().dimension() != Level.NETHER) {
            // в лаве вне Незера рыба клюёт, но улова нет
        } else if (this.nibble > 0 && this.biteTable != null) {
            if (LineBreak.shouldSnap(player)) {
                // обычная леска порвалась: добыча потеряна, рыбалка окончена, удочка цела
                LineBreak.announce(player, this);
                this.discard();
                return 0;
            }
            LootParams params = new LootParams.Builder((ServerLevel) this.level())
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.TOOL, rod)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ATTACKING_ENTITY, this.getOwner())
                    .withLuck(this.lootLuck)
                    .create(LootContextParamSets.FISHING);
            LootTable table = this.level().getServer().reloadableRegistries().getLootTable(this.biteTable);
            List<ItemStack> items = table.getRandomItems(params);
            // лавовый ящик: отдельный настраиваемый шанс (Config.LAVA_CRATE_CHANCE), заменяет обычный улов
            if (this.biteTable == LAVA_FISHING && this.random.nextDouble() < com.sinscold.newfishing.Config.lavaCrateChance()) {
                items = List.of(new ItemStack(com.sinscold.newfishing.registry.ModItems.LOCKED_LAVA_CRATE.get()));
            }
            for (ItemStack stack : items) {
                ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
                itemEntity.setInvulnerable(true);
                double dx = player.getX() - this.getX();
                double dy = player.getY() - this.getY();
                double dz = player.getZ() - this.getZ();
                itemEntity.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
                this.level().addFreshEntity(itemEntity);
                this.level().addFreshEntity(new ExperienceOrb(this.level(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, this.random.nextInt(6) + 1));
                if (stack.is(ItemTags.FISHES) || stack.is(ModTags.NETHER_FISH) || stack.is(ModTags.VOID_FISH)) {
                    player.awardStat(Stats.FISH_CAUGHT, 1);
                }
            }
            damage = this.onGround() ? 2 : 1;
            this.useBait(rod);
            com.sinscold.newfishing.item.BaitBag.refillRod(player, rod);
        }
        this.discard();
        return damage;
    }

    /** После улова наживка тратится с шансом 1 к 3. */
    private void useBait(ItemStack rod) {
        RodMods mods = RodMods.get(rod);
        if (!mods.hasBait() || this.random.nextFloat() >= BAIT_USE_CHANCE) return;
        ItemStack bait = mods.bait().copy();
        bait.shrink(1);
        rod.set(com.sinscold.newfishing.registry.ModDataComponents.ROD_MODS.get(), mods.withBait(bait));
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
