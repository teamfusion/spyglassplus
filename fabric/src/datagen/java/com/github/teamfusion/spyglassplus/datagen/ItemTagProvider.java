package com.github.teamfusion.spyglassplus.datagen;

import com.github.teamfusion.spyglassplus.tag.SpyglassPlusItemTags;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

import java.util.Arrays;

import static com.github.teamfusion.spyglassplus.item.SpyglassPlusItems.*;
import static net.minecraft.item.Items.*;

public final class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagProvider(FabricDataGenerator gen, BlockTagProvider blockTagProvider) {
        super(gen, blockTagProvider);
    }

    @Override
    protected void generateTags() {
        this.add(SpyglassPlusItemTags.SCOPING_ITEMS, BINOCULARS).add(
            SPYGLASS
        );

        this.add(SpyglassPlusItemTags.SPYGLASS_STAND_ITEMS).add(
            SPYGLASS
        );
    }

    @SafeVarargs
    public final FabricTagBuilder<Item> add(TagKey<Item> tag, RegistrySupplier<Item>... items) {
        FabricTagBuilder<Item> builder = this.getOrCreateTagBuilder(tag);
        Arrays.stream(items).map(RegistrySupplier::get).forEach(builder::add);
        return builder;
    }
}
