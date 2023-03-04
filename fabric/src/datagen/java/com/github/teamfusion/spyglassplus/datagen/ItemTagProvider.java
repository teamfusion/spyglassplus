package com.github.teamfusion.spyglassplus.datagen;

import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import com.github.teamfusion.spyglassplus.tag.SpyglassPlusItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public final class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);

    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.getOrCreateTagBuilder(SpyglassPlusItemTags.SCOPING_ITEMS).add(Items.SPYGLASS).add(SpyglassPlusItems.BINOCULARS.getId());
        this.getOrCreateTagBuilder(SpyglassPlusItemTags.SPYGLASS_STAND_ITEMS).add(Items.SPYGLASS);
    }
}
