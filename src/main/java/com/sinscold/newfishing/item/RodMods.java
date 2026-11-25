package com.sinscold.newfishing.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.registry.ModDataComponents;
import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModTags;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Модификации внутри удочки: по одному предмету в слотах «леска» и «крючок» и стопка наживки в слоте «наживка»
 * (пустой стек = слот пуст).
 */
public record RodMods(ItemStack line, ItemStack hook, ItemStack bait) implements TooltipComponent {
    public static final RodMods EMPTY = new RodMods(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);

    /** Для старых вызовов без наживки. */
    public RodMods(ItemStack line, ItemStack hook) {
        this(line, hook, ItemStack.EMPTY);
    }

    public static final Codec<RodMods> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("line", ItemStack.EMPTY).forGetter(RodMods::line),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("hook", ItemStack.EMPTY).forGetter(RodMods::hook),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("bait", ItemStack.EMPTY).forGetter(RodMods::bait)
    ).apply(i, RodMods::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RodMods> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, RodMods::line,
            ItemStack.OPTIONAL_STREAM_CODEC, RodMods::hook,
            ItemStack.OPTIONAL_STREAM_CODEC, RodMods::bait,
            RodMods::new);

    /** Записывает детали в удочку и сразу пересчитывает их силу рыбалки (единственное место пересчёта). */
    public static void set(ItemStack rod, RodMods mods) {
        rod.set(ModDataComponents.ROD_MODS.get(), mods);
        rod.set(ModDataComponents.FISHING_POWER.get(), (float) FishingScore.partsPower(mods));
    }

    public static RodMods get(ItemStack rod) {
        return rod.getOrDefault(ModDataComponents.ROD_MODS.get(), EMPTY);
    }

    /** Держит ли игрок удочку, с которой ещё можно рыбачить (есть леска и рыболовный крючок). */
    public static boolean holdsFishableRod(net.minecraft.world.entity.player.Player player) {
        ItemStack rod = player.getMainHandItem().is(Items.FISHING_ROD) ? player.getMainHandItem()
                : player.getOffhandItem().is(Items.FISHING_ROD) ? player.getOffhandItem() : ItemStack.EMPTY;
        return !rod.isEmpty() && get(rod).canFish();
    }

    public static boolean isLine(ItemStack stack) {
        return stack.is(ModTags.ROD_LINES);
    }

    /** Подходит ли предмет для слота крючка: крючок или морковка/гриб на палке. */
    public static boolean isHookSlotItem(ItemStack stack) {
        return stack.is(ModTags.ROD_HOOKS) || stack.is(ModTags.ROD_RIDING_STICKS);
    }

    /** Подходит ли предмет для слота наживки (тег `newfishing:rod_baits`). */
    public static boolean isBait(ItemStack stack) {
        return stack.is(ModTags.ROD_BAITS);
    }

    /** Удочка в руке игрока (основная рука в приоритете) или пустой стек. */
    public static ItemStack heldRod(net.minecraft.world.entity.player.Player player) {
        return player.getMainHandItem().is(Items.FISHING_ROD) ? player.getMainHandItem()
                : player.getOffhandItem().is(Items.FISHING_ROD) ? player.getOffhandItem() : ItemStack.EMPTY;
    }

    public boolean hasBait() {
        return !bait.isEmpty();
    }

    public boolean hasLine() {
        return !line.isEmpty();
    }

    public boolean hasHook() {
        return !hook.isEmpty();
    }

    /** В слоте именно рыболовный крючок (а не морковка/гриб на палке). */
    public boolean hasFishingHook() {
        return hook.is(ModTags.ROD_HOOKS);
    }

    /** Эндер-поплавок: единственный, что зависает в пустоте Края. */
    public boolean hasEnderHook() {
        return hook.is(ModItems.ENDER_HOOK.get());
    }

    public boolean hasPhantomLine() {
        return line.is(ModItems.PHANTOM_LINE.get());
    }

    public boolean hasNetheriteLine() {
        return line.is(ModItems.NETHERITE_LINE.get());
    }

    /** Незеритовый крючок: поплавок для любых жидкостей, цепляет существ. */
    public boolean hasNetheriteHook() {
        return hook.is(ModItems.NETHERITE_HOOK.get());
    }

    /** Можно ли закидывать: есть леска и настоящий крючок. */
    public boolean canFish() {
        return hasLine() && hasFishingHook();
    }

    /**
     * Номер вида удочки для модели (предикат `newfishing:rod_state`):
     * 16 обычная леска + эндер-поплавок, 17 незеритовая + эндер, 18 фантомная + эндер.
     * 11 фантомная леска, 12 +крючок, 13 +незеритовый крючок, 14 +морковка, 15 +искажённый гриб.
     * 0 пустая, 1 леска, 2 леска+крючок, 3 леска+незеритовый крючок,
     * 4 незеритовая леска, 5 незеритовая леска+крючок, 6 незеритовая леска+незеритовый крючок.
     * С морковкой на палке: 7 (обычная леска), 9 (незеритовая); с грибом на палке: 8 и 10.
     */
    public int visualState() {
        if (!hasLine()) return 0;
        if (hook.is(ModItems.ENDER_HOOK.get())) {
            if (line.is(ModItems.PHANTOM_LINE.get())) return 18;
            if (line.is(ModItems.NETHERITE_LINE.get())) return 17;
            return 16;
        }
        if (line.is(ModItems.PHANTOM_LINE.get())) {
            if (hook.is(Items.CARROT)) return 14;
            if (hook.is(Items.WARPED_FUNGUS)) return 15;
            if (hook.is(ModItems.NETHERITE_HOOK.get())) return 13;
            if (hook.is(ModTags.ROD_HOOKS)) return 12;
            return 11;
        }
        boolean netherLine = line.is(ModItems.NETHERITE_LINE.get());
        int base = netherLine ? 4 : 1;
        if (hook.is(Items.CARROT)) return netherLine ? 9 : 7;
        if (hook.is(Items.WARPED_FUNGUS)) return netherLine ? 10 : 8;
        if (hook.is(ModItems.NETHERITE_HOOK.get())) return base + 2;
        if (hook.is(ModTags.ROD_HOOKS)) return base + 1;
        return base;
    }

    /** Вид лески для модели закинутой удочки (предикат `newfishing:rod_line`): 0 нет, 1 обычная, 2 незеритовая, 3 фантомная. */
    public int lineKind() {
        if (!hasLine()) return 0;
        if (line.is(ModItems.PHANTOM_LINE.get())) return 3;
        if (line.is(ModItems.NETHERITE_LINE.get())) return 2;
        return 1;
    }

    public RodMods withLine(ItemStack newLine) {
        return new RodMods(newLine, hook, bait);
    }

    public RodMods withHook(ItemStack newHook) {
        return new RodMods(line, newHook, bait);
    }

    public RodMods withBait(ItemStack newBait) {
        return new RodMods(line, hook, newBait);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RodMods other
                && ItemStack.matches(line, other.line)
                && ItemStack.matches(hook, other.hook)
                && ItemStack.matches(bait, other.bait);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * ItemStack.hashItemAndComponents(line) + ItemStack.hashItemAndComponents(hook))
                + ItemStack.hashItemAndComponents(bait) + bait.getCount();
    }
}
