package com.sinscold.newfishing.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Мини-игра вытаскивания рыбы (только сервер, живёт внутри NetheriteFishingHook с момента поклёвки).
 * <p>
 * Рыба чередует рывки (бежит прочь или в сторону, поплавок притапливается) и отдых (замедляется и останавливается).
 * Игрок: ЛКМ — тянет (подматывает поплавок и выматывает рыбу), ПКМ — отпускает (натяжение падает, рыба уходит дальше),
 * ничего не нажимает — ничего не происходит, рыба просто водит поплавок.
 * Тянуть, пока рыба бежит, — быстро растёт натяжение; тянуть, когда она остановилась, — безопасно.
 * Удочка, отведённая в сторону, противоположную рывку рыбы, гасит натяжение (и наоборот).
 * Натяжение 1 или рыба дальше предела: обычная леска рвётся, незеритовая и фантомная выдерживают — бой просто
 * затягивается (подтянуть нельзя, рыба отдыхает и набирается сил). Долго держать леску отпущенной — рыба сорвётся
 * (на незеритовом крючке втрое дольше). Силы рыбы 0 — улов.
 * Все числа ниже подбираются на глаз.
 */
public final class FishFight {
    public enum Result { CONTINUE, CAUGHT, SNAPPED, ESCAPED }

    public static final int INPUT_NONE = 0;
    public static final int INPUT_PULL = 1;
    public static final int INPUT_RELEASE = 2;

    /** Дальше этого (по горизонтали) обычная леска рвётся. */
    public static final double MAX_DISTANCE = 30.0;
    public static final double MAX_DISTANCE_VOID = 40.0;
    /** Сколько тиков можно держать леску совсем отпущенной, пока рыба не сорвётся (обычный крючок). */
    public static final int SLACK_TICKS = 100;

    /** Скорость рыбы при рывке (блоков/тик) у свежей и у уставшей рыбы. */
    private static final double RUN_SPEED_TIRED = 0.06;
    private static final double RUN_SPEED_FRESH = 0.16;
    /** Скорость подмотки, когда рыба стоит. */
    private static final double REEL_SPEED = 0.13;

    private final float difficulty;
    private final double maxDistance;
    private final int slackLimit;

    /** Ввод игрока (из пакета), положение удочки на экране −1 (слева) … 1 (справа). */
    int input = INPUT_NONE;
    float rodX = 0.8F;

    float tension;
    float stamina = 1.0F;
    private boolean running;
    private int phaseTicks;
    private double speed;
    private Vec3 fishDir = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private int slackTicks;

    FishFight(float difficulty, double maxDistance, int slackLimit) {
        this.difficulty = difficulty;
        this.maxDistance = maxDistance;
        this.slackLimit = slackLimit;
    }

    boolean running() {
        return this.running;
    }

    Vec3 velocity() {
        return this.velocity;
    }

    /**
     * @param toPlayer единичный горизонтальный вектор от поплавка к игроку
     * @param distance горизонтальное расстояние до игрока
     */
    Result tick(RandomSource random, Vec3 toPlayer, double distance, boolean unbreakableLine) {
        // смена фаз: рывок → отдых → рывок…; уставшая рыба бегает короче и отдыхает дольше
        if (--this.phaseTicks <= 0) {
            this.running = !this.running;
            if (this.running) {
                this.phaseTicks = (int) (Mth.nextInt(random, 25, 60) * (0.6F + 0.4F * this.stamina));
                this.fishDir = this.pickDirection(random, toPlayer, distance);
            } else {
                this.phaseTicks = (int) (Mth.nextInt(random, 20, 45) * (1.4F - 0.4F * this.stamina));
            }
        }
        double target = this.running ? (RUN_SPEED_TIRED + (RUN_SPEED_FRESH - RUN_SPEED_TIRED) * this.stamina) * Math.sqrt(this.difficulty) : 0.0;
        // рывок начинается резко, а замедляется плавно — по замедлению видно, что можно тянуть
        this.speed += (target - this.speed) * (target > this.speed ? 0.35 : 0.08);
        float effort = (float) this.speed;

        // удочка в стороне, противоположной рывку, гасит натяжение; в ту же сторону — усиливает
        Vec3 right = new Vec3(toPlayer.z, 0.0, -toPlayer.x); // правая сторона экрана, когда игрок смотрит на поплавок
        float lateral = (float) this.fishDir.dot(right);
        float sideFactor = 1.0F - 0.45F * Mth.clamp(-this.rodX * lateral, -1.0F, 1.0F);

        boolean strained = this.tension >= 1.0F; // только с неразрывной леской: на пределе подтянуть нельзя
        double fishPull;
        double reel = 0.0;
        float staminaScale = 1.0F / (float) Math.sqrt(this.difficulty);
        switch (this.input) {
            case INPUT_PULL -> {
                this.tension += (0.003F + 0.12F * effort * this.difficulty) * sideFactor;
                if (!strained) {
                    this.stamina -= (0.007F + 0.04F * effort) * staminaScale;
                    reel = REEL_SPEED * Math.max(0.0, 1.0 - this.speed / 0.2);
                }
                fishPull = 0.3;
            }
            case INPUT_RELEASE -> {
                this.tension -= 0.04F;
                this.stamina += 0.0015F;
                fishPull = 1.0;
            }
            default -> {
                this.tension += 0.08F * effort * this.difficulty * sideFactor - 0.012F;
                this.stamina -= 0.0015F * (effort / (float) RUN_SPEED_FRESH) * staminaScale;
                fishPull = 0.7;
            }
        }
        if (distance < 2.0) {
            reel = 0.0; // поплавок уже у ног
        }
        this.velocity = this.fishDir.scale(this.speed * fishPull).add(toPlayer.scale(reel));

        // предел расстояния: обычная леска рвётся, неразрывная не пускает рыбу дальше
        if (distance > this.maxDistance) {
            if (!unbreakableLine) {
                return Result.SNAPPED;
            }
            double outward = -this.velocity.dot(toPlayer);
            if (outward > 0.0) {
                this.velocity = this.velocity.add(toPlayer.scale(outward));
            }
            this.tension = Math.max(this.tension, 0.9F);
        }

        if (this.tension >= 1.0F) {
            if (!unbreakableLine) {
                return Result.SNAPPED;
            }
            this.tension = 1.0F;
            this.stamina += 0.002F; // пока леска на пределе, рыба отдыхает: бой затягивается
        }
        this.tension = Mth.clamp(this.tension, 0.0F, 1.0F);
        this.stamina = Math.min(1.0F, this.stamina);

        // слишком долго держать леску отпущенной: рыба сходит с крючка
        if (this.input == INPUT_RELEASE && this.tension <= 0.0F) {
            if (++this.slackTicks > this.slackLimit) {
                return Result.ESCAPED;
            }
        } else {
            this.slackTicks = Math.max(0, this.slackTicks - 2);
        }

        return this.stamina <= 0.0F ? Result.CAUGHT : Result.CONTINUE;
    }

    /** Куда бежит рыба: в основном прочь от игрока (±80°), иногда в сторону; у предела лески — чаще вбок. */
    private Vec3 pickDirection(RandomSource random, Vec3 toPlayer, double distance) {
        Vec3 away = toPlayer.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : toPlayer.scale(-1.0);
        boolean sideways = random.nextFloat() < (distance > this.maxDistance * 0.7 ? 0.7F : 0.25F);
        double angle = sideways
                ? (random.nextBoolean() ? 1 : -1) * Mth.nextDouble(random, 80.0, 120.0)
                : Mth.nextDouble(random, -80.0, 80.0);
        return away.yRot((float) Math.toRadians(angle)).normalize();
    }
}
