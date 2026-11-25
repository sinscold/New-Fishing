package com.sinscold.newfishing.loot;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.registry.ModLootFunctions;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * `newfishing:limit_enchantments`: ставится после `enchant_with_levels`. Починка остаётся только с шансом
 * `mending_chance`, лишние чары сверх `max` убираются случайно (работает и для книг).
 */
public class LimitEnchantmentsFunction extends LootItemConditionalFunction {
    public static final MapCodec<LimitEnchantmentsFunction> CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i).and(i.group(
            Codec.intRange(0, 64).optionalFieldOf("max", 3).forGetter(f -> f.max),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("mending_chance", 1.0F).forGetter(f -> f.mendingChance)
    )).apply(i, LimitEnchantmentsFunction::new));

    private final int max;
    private final float mendingChance;

    protected LimitEnchantmentsFunction(List<LootItemCondition> conditions, int max, float mendingChance) {
        super(conditions);
        this.max = max;
        this.mendingChance = mendingChance;
    }

    @Override
    public LootItemFunctionType<LimitEnchantmentsFunction> getType() {
        return ModLootFunctions.LIMIT_ENCHANTMENTS.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        EnchantmentHelper.updateEnchantments(stack, enchantments -> {
            if (random.nextFloat() >= this.mendingChance) {
                enchantments.removeIf(holder -> holder.is(Enchantments.MENDING));
            }
            List<Holder<Enchantment>> present = new ArrayList<>(enchantments.keySet());
            while (present.size() > this.max) {
                Holder<Enchantment> removed = present.remove(random.nextInt(present.size()));
                enchantments.removeIf(holder -> holder.equals(removed));
            }
        });
        return stack;
    }
}
