package com.sinscold.newfishing.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.registry.ModItems;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * В ванильной таблице рыбалки не должно быть сокровища: каждый предмет-сокровище (лук, зачарованная книга, зачарованная
 * удочка, бирка, раковина наутилуса, седло) заменяется водяным ящиком. Содержимое ящика — в loot_table/chests/water_crate.json.
 */
public class CrateReplacesTreasureModifier extends LootModifier {
    public static final MapCodec<CrateReplacesTreasureModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, CrateReplacesTreasureModifier::new));

    public CrateReplacesTreasureModifier(LootItemCondition[] conditions) {
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
        for (int i = 0; i < generatedLoot.size(); i++) {
            if (isTreasure(generatedLoot.get(i))) {
                generatedLoot.set(i, new ItemStack(ModItems.WATER_CRATE.get()));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
