package com.github.teamfusion.spyglassplus.tag;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface SpyglassPlusEntityTypeTags {
    /**
     * Contains entities that specifically implement {@link Entity#getTargetingMargin()}.
     * <p>By default includes {@link EntityType#SHULKER} and {@link EntityType#ITEM_FRAME}.</p>
     */
    TagKey<EntityType<?>> IGNORE_MARGIN_EXPANSION_FOR_DISCOVERY_ENCHANTMENT = create("ignore_margin_expansion_for_discovery_enchantment");

    /**
     * Contains entities to be ignored by {@link SpyglassPlusEnchantments#DISCOVERY}.
     */
    TagKey<EntityType<?>> IGNORE_FOR_DISCOVERY_ENCHANTMENT = create("ignore_for_discovery_enchantment");

    static TagKey<EntityType<?>> create(String id) {
        return TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(SpyglassPlus.MOD_ID, id));
    }
}
