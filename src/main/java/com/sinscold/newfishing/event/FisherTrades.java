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
 * Торговля рыбаков: новичок покупает 8–16 штук одной любой рыбы за 1 изумруд (до 4 сделок),
 * мастер покупает 2–4 рыбы из Незера за 3 или 4 изумруда (до 3 сделок).
 */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class FisherTrades {
    @SubscribeEvent
    static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.FISHERMAN) return;
        event.getTrades().get(1).add(new BuyFish(ItemTags.FISHES, 8, 16, 1, 1, 4, 2));
        event.getTrades().get(5).add(new BuyFish(ModTags.NETHER_FISH, 2, 4, 3, 4, 3, 30));
        // ученик (уровень 2) продаёт сумку для наживки за 12 изумрудов (только если стоит Curios)
        if (com.sinscold.newfishing.registry.ModItems.BAIT_BAG != null) {
            event.getTrades().get(2).add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 12),
                    new ItemStack(com.sinscold.newfishing.registry.ModItems.BAIT_BAG.get()), 3, 10, 0.05F));
        }
        // эксперт (уровень 4) продаёт 4 наживки профи за 16 изумрудов
        event.getTrades().get(4).add((trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 16),
                new ItemStack(com.sinscold.newfishing.registry.ModItems.PRO_BAIT.get(), 4), 4, 10, 0.05F));
        // удочки, которые продаёт рыбак, приходят с обычной леской и обычным крючком
        event.getTrades().replaceAll((level, listings) -> {
            List<VillagerTrades.ItemListing> wrapped = new ArrayList<>(listings.size());
            for (VillagerTrades.ItemListing listing : listings) {
                wrapped.add((trader, random) -> withRodParts(listing.getOffer(trader, random)));
            }
            return wrapped;
        });
    }

    private static MerchantOffer withRodParts(MerchantOffer offer) {
        if (offer != null && offer.getResult().is(Items.FISHING_ROD) && !com.sinscold.newfishing.item.RodMods.get(offer.getResult()).hasLine()) {
            offer.getResult().set(com.sinscold.newfishing.registry.ModDataComponents.ROD_MODS.get(), new com.sinscold.newfishing.item.RodMods(
                    new ItemStack(com.sinscold.newfishing.registry.ModItems.FISHING_LINE.get()),
                    new ItemStack(com.sinscold.newfishing.registry.ModItems.FISHING_HOOK.get())));
        }
        return offer;
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
