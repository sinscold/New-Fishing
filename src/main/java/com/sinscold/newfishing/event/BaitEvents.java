package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModTags;
import com.sinscold.newfishing.world.EatenLogs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;

/** Наживка: эндермит с убитого игроком эндермита; выеденные брёвна в деревьях, выращенных в жарких биомах. */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class BaitEvents {
    /** Шанс выеденного бревна в дереве, выращенном из саженца в жарком биоме. */
    private static final float GROWN_TREE_EATEN_CHANCE = 0.2F;

    @SubscribeEvent
    static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Endermite mite && event.getSource().getEntity() instanceof Player) {
            event.getDrops().add(new ItemEntity(mite.level(), mite.getX(), mite.getY(), mite.getZ(), new ItemStack(ModItems.ENDERMITE.get())));
        }
    }

    /** Саженец растёт (сам или от костной муки): после того как дерево встанет, с шансом выедаем бревно ствола. */
    @SubscribeEvent
    static void onTreeGrow(BlockGrowFeatureEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos().immutable();
        if (!level.getBiome(pos).is(ModTags.EATEN_LOG_BIOMES) || event.getRandom().nextFloat() >= GROWN_TREE_EATEN_CHANCE) return;
        // дерево ставится сразу после события, поэтому замену откладываем на следующую обработку задач сервера
        MinecraftServer server = level.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> EatenLogs.eatTrunk(level, pos, level.getRandom(), Block.UPDATE_ALL)));
    }

    private BaitEvents() {}
}
