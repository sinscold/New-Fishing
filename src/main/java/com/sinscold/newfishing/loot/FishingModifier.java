package com.sinscold.newfishing.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.Config;
import com.sinscold.newfishing.registry.ModItems;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Глобальный модификатор ванильной рыбалки: ванильного «сокровища» (лук, зачарованная книга, зачарованная удочка, бирка,
 * раковина наутилуса, седло) больше нет, вместо него с шансом {@link Config#waterCrateChance()} выпадает водяной ящик
 * (иначе предмет просто пропадает). Содержимое ящика: loot_table/chests/water_crate.json.
 * Лут по биомам временно убран (был в коммите «Торговля рыбаков, пиксельная леска, рыбалка по биомам», файл loot/FishingBiomes.java).
 */
public class FishingModifier extends LootModifier {
    public static final MapCodec<FishingModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, FishingModifier::new));

    public FishingModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    private static boolean isTreasure(ItemStack stack) {
        return stack.is(Items.BOW) || stack.is(Items.ENCHANTED_BOOK) || stack.is(Items.NAME_TAG)
                || stack.is(Items.NAUTILUS_SHELL) || stack.is(Items.SADDLE)
                // удочка в мусоре не зачарована, а в сокровище зачарована
                || (stack.is(Items.FISHING_ROD) && stack.isEnchanted());
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        for (ItemStack stack : generatedLoot) {
            if (!isTreasure(stack)) {
                result.add(stack);
            } else if (context.getRandom().nextDouble() < Config.waterCrateChance()) {
                result.add(new ItemStack(ModItems.LOCKED_WATER_CRATE.get()));
            }
        }
        return result;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
