package com.sinscold.newfishing.event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * Убирает ванильные рецепты, которые мод заменяет: удочку (вместо неё рецепт из 3 палок),
 * морковку и гриб на палке (теперь они вставляются в слот крючка удочки).
 * Делается после загрузки рецептов, поэтому не зависит от порядка датапаков.
 */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class RemoveVanillaRecipes {
    private static final Set<ResourceLocation> REMOVED = Set.of(
            ResourceLocation.withDefaultNamespace("fishing_rod"),
            ResourceLocation.withDefaultNamespace("carrot_on_a_stick"),
            ResourceLocation.withDefaultNamespace("warped_fungus_on_a_stick"));

    @SubscribeEvent
    static void onAddReloadListeners(AddReloadListenerEvent event) {
        RecipeManager manager = event.getServerResources().getRecipeManager();
        event.addListener((ResourceManagerReloadListener) resourceManager -> {
            List<RecipeHolder<?>> kept = manager.getRecipes().stream()
                    .filter(holder -> !REMOVED.contains(holder.id()))
                    .collect(Collectors.toList());
            manager.replaceRecipes(kept);
        });
    }

    private RemoveVanillaRecipes() {}
}
