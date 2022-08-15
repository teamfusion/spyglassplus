package com.github.teamfusion.spyglassplus.datagen;

import com.github.teamfusion.spyglassplus.tag.SpyglassPlusEntityTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import static com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType.*;
import static net.minecraft.entity.EntityType.*;

public class EntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public EntityTypeTagProvider(FabricDataGenerator gen) {
        super(gen);
    }

    @Override
    protected void generateTags() {
        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.IGNORE_MARGIN_EXPANSION_FOR_DISCOVERY_ENCHANTMENT).add(
            SHULKER,
            ITEM_FRAME
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.IGNORE_FOR_DISCOVERY_ENCHANTMENT).add(
            SPYGLASS_STAND.get(),
            ITEM_FRAME
        );
    }
}
