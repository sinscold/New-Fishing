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

/** Модификации внутри удочки: по одному предмету в слотах «леска» и «крючок» (пустой стек = слот пуст). */
public record RodMods(ItemStack line, ItemStack hook) implements TooltipComponent {
    public static final RodMods EMPTY = new RodMods(ItemStack.EMPTY, ItemStack.EMPTY);

    public static final Codec<RodMods> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("line", ItemStack.EMPTY).forGetter(RodMods::line),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("hook", ItemStack.EMPTY).forGetter(RodMods::hook)
    ).apply(i, RodMods::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RodMods> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, RodMods::line,
            ItemStack.OPTIONAL_STREAM_CODEC, RodMods::hook,
            RodMods::new);

    public static RodMods get(ItemStack rod) {
        return rod.getOrDefault(ModDataComponents.ROD_MODS.get(), EMPTY);
    }

    public static boolean isLine(ItemStack stack) {
        return stack.is(ModTags.ROD_LINES);
    }

    /** Подходит ли предмет для слота крючка: крючок или морковка/гриб на палке. */
    public static boolean isHookSlotItem(ItemStack stack) {
        return stack.is(ModTags.ROD_HOOKS) || stack.is(ModTags.ROD_RIDING_STICKS);
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

    /** Можно ли закидывать: есть леска и настоящий крючок. */
    public boolean canFish() {
        return hasLine() && hasFishingHook();
    }

    /**
     * Номер вида удочки для модели (предикат `newfishing:rod_state`):
     * 0 пустая, 1 леска, 2 леска+крючок, 3 леска+незеритовый крючок,
     * 4 незеритовая леска, 5 незеритовая леска+крючок, 6 незеритовая леска+незеритовый крючок.
     * Морковка/гриб на палке в слоте крючка вид не меняют.
     */
    public int visualState() {
        if (!hasLine()) return 0;
        boolean netherLine = line.is(ModItems.NETHERITE_LINE.get());
        int base = netherLine ? 4 : 1;
        if (hook.is(ModItems.NETHERITE_HOOK.get())) return base + 2;
        if (hook.is(ModTags.ROD_HOOKS)) return base + 1;
        return base;
    }

    public RodMods withLine(ItemStack newLine) {
        return new RodMods(newLine, hook);
    }

    public RodMods withHook(ItemStack newHook) {
        return new RodMods(line, newHook);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RodMods other
                && ItemStack.matches(line, other.line)
                && ItemStack.matches(hook, other.hook);
    }

    @Override
    public int hashCode() {
        return 31 * ItemStack.hashItemAndComponents(line) + ItemStack.hashItemAndComponents(hook);
    }
}
