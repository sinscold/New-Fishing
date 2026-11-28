package com.sinscold.newfishing.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Мини-игра вытаскивания рыбы (только сервер, живёт внутри NetheriteFishingHook с момента поклёвки).
 * <p>
 * Два состояния рыбы:
 * <ul>
 * <li><b>Мечется</b> — бросается от игрока то влево, то вправо. Выматывает её только «конфронтация»: удочка
 * отведена в сторону, противоположную рывку (рыба вправо — удочка влево). Удочка в ту же сторону — натяжение
 * растёт. Тянуть (ЛКМ) в это время опасно: натяжение растёт быстро, обычная леска рвётся.</li>
 * <li><b>Выдохлась</b> — силы кончились, рыба несколько секунд стоит и восстанавливается. Сейчас можно смело
 * тянуть: поплавок идёт к игроку. Восстановилась — снова мечется.</li>
 * </ul>
 * ПКМ отпускает леску (натяжение падает, рыба уходит дальше; долго держать отпущенной — сорвётся). Ничего не жать —
 * рыба водит поплавок. Улов — когда поплавок коснётся твёрдого блока (подтянули к берегу) или дошёл до игрока
 * (это проверяет NetheriteFishingHook). Натяжение 1 или рыба дальше предела: обычная леска рвётся; незеритовая
 * и фантомная выдерживают, но рыба на пределе отдыхает и набирается сил (бой затягивается).
 * Натяжение слышно (FishingFightClient), шкал нет. Все числа ниже подбираются на глаз.
 */
public final class FishFight {
    public enum Result { CONTINUE, SNAPPED, ESCAPED }

    public static final int INPUT_NONE = 0;
    public static final int INPUT_PULL = 1;
    public static final int INPUT_RELEASE = 2;

    /** Дальше этого (по горизонтали) обычная леска рвётся. */
    public static final double MAX_DISTANCE = 30.0;
    public static final double MAX_DISTANCE_VOID = 40.0;
    /** Сколько тиков можно держать леску совсем отпущенной, пока рыба не сорвётся (обычный крючок). */
    public static final int SLACK_TICKS = 100;

    /** Скорость метаний рыбы (блоков/тик). */
    private static final double THRASH_SPEED = 0.13;
    /** Скорость подмотки выдохшейся рыбы и (малая) — мечущейся. */
    private static final double REEL_SPEED = 0.15;
    private static final double REEL_SPEED_ACTIVE = 0.02;
    /** Сколько тиков рыба отдыхает после того, как выдохлась. */
    private static final int REST_MIN = 60;
    private static final int REST_MAX = 100;

    private final float difficulty;
    private final double maxDistance;
    private final int slackLimit;

    /** Ввод игрока (из пакета), положение удочки на экране −1 (слева) … 1 (справа). */
    int input = INPUT_NONE;
    float rodX = 0.8F;

    float tension;
    float stamina = 1.0F;
    /** > 0: рыба выдохлась и отдыхает ещё столько тиков. */
    private int restTicks;
    private int restDuration;
    private int dashTicks;
    private int side = 1;
    private double speed;
    private Vec3 fishDir = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private int slackTicks;

    FishFight(float difficulty, double maxDistance, int slackLimit) {
        this.difficulty = difficulty;
        this.maxDistance = maxDistance;
        this.slackLimit = slackLimit;
    }

    /** Рыба мечется (не выдохлась). */
    boolean thrashing() {
        return this.restTicks <= 0;
    }

    Vec3 velocity() {
        return this.velocity;
    }

    /**
     * @param toPlayer единичный горизонтальный вектор от поплавка к игроку
     * @param distance горизонтальное расстояние до игрока
     */
    Result tick(RandomSource random, Vec3 toPlayer, double distance, boolean unbreakableLine) {
        Vec3 right = new Vec3(toPlayer.z, 0.0, -toPlayer.x); // правая сторона экрана, когда игрок смотрит на поплавок
        double reel = 0.0;
        double fishPull;

        if (this.restTicks > 0) {
            // выдохлась: стоит и восстанавливает силы; тянуть безопасно, рыба идёт к игроку
            this.restTicks--;
            this.stamina = 1.0F - (float) this.restTicks / (float) this.restDuration;
            this.speed *= 0.85;
            switch (this.input) {
                case INPUT_PULL -> {
                    this.tension += 0.004F;
                    reel = REEL_SPEED;
                }
                case INPUT_RELEASE -> this.tension -= 0.04F;
                default -> this.tension -= 0.015F;
            }
            this.tension = Math.min(this.tension, 0.35F); // уставшая рыба леску почти не тянет
            fishPull = 1.0;
            if (this.restTicks == 0) {
                this.dashTicks = 0; // восстановилась — сразу рывок
            }
        } else {
            // мечется: рывки от игрока то влево, то вправо
            if (--this.dashTicks <= 0) {
                this.side = random.nextFloat() < 0.75F ? -this.side : this.side;
                this.dashTicks = Mth.nextInt(random, 15, 40);
                Vec3 away = toPlayer.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : toPlayer.scale(-1.0);
                // у предела лески рыба бросается вбок, а не прочь
                double angle = distance > this.maxDistance * 0.7 ? Mth.nextDouble(random, 80.0, 110.0) : Mth.nextDouble(random, 40.0, 100.0);
                this.fishDir = away.yRot((float) Math.toRadians(angle * this.side)).normalize();
            }
            double target = THRASH_SPEED * Math.sqrt(this.difficulty) * (0.5 + 0.5 * this.stamina);
            this.speed += (target - this.speed) * 0.3;
            float effort = (float) (this.speed / THRASH_SPEED);

            // конфронтация: удочка в стороне, противоположной рывку, выматывает рыбу; в ту же сторону — дёргает леску
            float lateral = (float) this.fishDir.dot(right);
            float against = Mth.clamp(-this.rodX * lateral, -1.0F, 1.0F);
            float staminaScale = 1.0F / (float) Math.sqrt(this.difficulty);
            if (against > 0.0F) {
                this.stamina -= 0.011F * against * staminaScale;
            }
            float sideTension = against < 0.0F ? -against * 0.012F * effort : -against * 0.006F;

            switch (this.input) {
                case INPUT_PULL -> {
                    // тянуть мечущуюся рыбу — почти верный обрыв
                    this.tension += 0.035F * effort * this.difficulty + sideTension;
                    reel = REEL_SPEED_ACTIVE;
                    fishPull = 0.4;
                }
                case INPUT_RELEASE -> {
                    this.tension -= 0.04F;
                    this.stamina += 0.002F;
                    fishPull = 1.0;
                }
                default -> {
                    this.tension += 0.006F * effort * this.difficulty + sideTension - 0.004F;
                    fishPull = 0.6;
                }
            }
            if (this.stamina <= 0.0F) {
                this.stamina = 0.0F;
                this.restDuration = Mth.nextInt(random, REST_MIN, REST_MAX);
                this.restTicks = this.restDuration;
            }
        }

        if (distance < 1.5) {
            reel = 0.0;
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
            if (this.restTicks <= 0) {
                this.stamina = Math.min(1.0F, this.stamina + 0.003F); // леска на пределе: рыба набирается сил
            }
        }
        this.tension = Mth.clamp(this.tension, 0.0F, 1.0F);
        this.stamina = Mth.clamp(this.stamina, 0.0F, 1.0F);

        // слишком долго держать леску отпущенной: рыба сходит с крючка
        if (this.input == INPUT_RELEASE && this.tension <= 0.0F) {
            if (++this.slackTicks > this.slackLimit) {
                return Result.ESCAPED;
            }
        } else {
            this.slackTicks = Math.max(0, this.slackTicks - 2);
        }
        return Result.CONTINUE;
    }
}
