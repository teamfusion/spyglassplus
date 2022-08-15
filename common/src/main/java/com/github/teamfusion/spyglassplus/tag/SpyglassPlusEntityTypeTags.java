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

    /**
     * Contains entities pertaining to this behavior.
     */
    TagKey<EntityType<?>>
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_PASSIVE = create("discovery_enchantment_entity_behavior/passive"),
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_NEUTRAL = create("discovery_enchantment_entity_behavior/neutral"),
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_HOSTILE = create("discovery_enchantment_entity_behavior/hostile"),
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_BOSS = create("discovery_enchantment_entity_behavior/boss");

    static TagKey<EntityType<?>> create(String id) {
        return TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(SpyglassPlus.MOD_ID, id));
    }
}
