package com.sinscold.newfishing.event;

import java.util.ArrayList;
import java.util.List;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.registry.ModTags;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * Торговля рыбаков: новичок покупает 8–16 штук одной любой рыбы за 1–2 изумруда,
 * мастер покупает 2–4 рыбы из Незера за 6 или 8 изумрудов.
 */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class FisherTrades {
    @SubscribeEvent
    static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.FISHERMAN) return;
        event.getTrades().get(1).add(new BuyFish(ItemTags.FISHES, 8, 16, 1, 2, 12, 2));
        event.getTrades().get(5).add(new BuyFish(ModTags.NETHER_FISH, 2, 4, 6, 8, 8, 30));
    }

    /** Житель покупает рыбу одного вида за изумруды. */
    private record BuyFish(TagKey<Item> fish, int minCount, int maxCount, int minEmeralds, int maxEmeralds, int maxUses, int xp)
            implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            List<Holder<Item>> options = new ArrayList<>();
            BuiltInRegistries.ITEM.getTag(this.fish).ifPresent(set -> set.forEach(options::add));
            Item item = options.isEmpty() ? Items.COD : options.get(random.nextInt(options.size())).value();
            int count = this.minCount + random.nextInt(this.maxCount - this.minCount + 1);
            int emeralds = this.minEmeralds + random.nextInt(this.maxEmeralds - this.minEmeralds + 1);
            return new MerchantOffer(new ItemCost(item, count), new ItemStack(Items.EMERALD, emeralds), this.maxUses, this.xp, 0.05F);
        }
    }

    private FisherTrades() {}
}
