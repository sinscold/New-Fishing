package com.sinscold.newfishing.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.registry.ModDataComponents;
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

    public static boolean isHook(ItemStack stack) {
        return stack.is(ModTags.ROD_HOOKS);
    }

    public boolean hasLine() {
        return !line.isEmpty();
    }

    public boolean hasHook() {
        return !hook.isEmpty();
    }

    public boolean isComplete() {
        return hasLine() && hasHook();
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
