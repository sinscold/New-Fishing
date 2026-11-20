package com.sinscold.newfishing.loot;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.Config;
import com.sinscold.newfishing.registry.ModItems;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Рыбалка в воде по биомам (шансы в {@link FishingBiomes}): заново выбирает категорию улова (рыба, мусор, сокровище)
 * по биому поплавка, добавляет особые предметы по биому игрока и превращает сокровище в водяной ящик
 * (шанс ящика настраивается в Config). Ванильного сокровища (лук, книга и т.д.) больше нет: оно лежит в ящике.
 */
public class FishingModifier extends LootModifier {
    public static final MapCodec<FishingModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, FishingModifier::new));

    public FishingModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (origin == null) {
            return generatedLoot;
        }
        ServerLevel level = context.getLevel();
        Holder<Biome> floatBiome = level.getBiome(BlockPos.containing(origin));
        Entity hookEntity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Player owner = hookEntity instanceof FishingHook hook ? hook.getPlayerOwner() : null;
        Holder<Biome> playerBiome = owner != null ? level.getBiome(owner.blockPosition()) : floatBiome;

        double[] w = FishingBiomes.weights(floatBiome, playerBiome, context.getLuck());
        RandomSource random = context.getRandom();
        double roll = random.nextDouble() * (w[0] + w[1] + w[2]);

        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        boolean treasure = roll < w[2];
        boolean junk = !treasure && roll < w[2] + w[1];
        if (treasure) {
            if (random.nextDouble() < Config.waterCrateChance()) {
                result.add(new ItemStack(ModItems.WATER_CRATE.get()));
                return result;
            }
            // ящик не выпал: сокровища в ванильном виде нет, поэтому считаем уловом рыбу
            treasure = false;
        }
        if (junk) {
            level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_JUNK).getRandomItems(context, result::add);
            if (random.nextDouble() < FishingBiomes.PLAYER_BIOME_ITEM_CHANCE) {
                if (FishingBiomes.isJungle(playerBiome)) replace(result, FishingBiomes.JUNGLE_JUNK, random);
                else if (FishingBiomes.isCold(playerBiome)) replace(result, FishingBiomes.TAIGA_JUNK, random);
                else if (FishingBiomes.isDry(playerBiome)) replace(result, FishingBiomes.DESERT_JUNK, random);
            }
        } else {
            level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_FISH).getRandomItems(context, result::add);
            if (FishingBiomes.isCold(playerBiome) && random.nextDouble() < FishingBiomes.PLAYER_BIOME_ITEM_CHANCE) {
                result.clear();
                result.add(new ItemStack(FishingBiomes.COLD_FISH));
            }
        }
        return result;
    }

    private static void replace(List<ItemStack> result, List<Item> options, RandomSource random) {
        result.clear();
        result.add(new ItemStack(options.get(random.nextInt(options.size()))));
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
