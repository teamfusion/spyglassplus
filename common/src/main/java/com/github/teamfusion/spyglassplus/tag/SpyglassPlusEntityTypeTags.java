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
     * Contains entities to be ignored by {@link SpyglassPlusEnchantments#DISCOVERY}.
     */
    TagKey<EntityType<?>> IGNORE_DISCOVERY = create("ignore_discovery");

    /**
     * Contains entities that specifically implement {@link Entity#getTargetingMargin()}.
     * <p>By default includes {@link EntityType#SHULKER} and {@link EntityType#ITEM_FRAME}.</p>
     */
    TagKey<EntityType<?>> IGNORE_MARGIN_EXPANSION_DISCOVERY = create("ignore_margin_expansion_discovery");

    /**
     * Contains entities that should ignore rendering stats for {@link SpyglassPlusEnchantments#DISCOVERY}.
     */
    TagKey<EntityType<?>> IGNORE_STATS_DISCOVERY = create("ignore_stats_discovery");

    /**
     * Contains entities to be rendered in the box for {@link SpyglassPlusEnchantments#DISCOVERY}.
     */
    TagKey<EntityType<?>> DO_NOT_RENDER_BOX_DISCOVERY = create("do_not_render_box_discovery");

    /**
     * Contains entities pertaining to this behavior.
     */
    TagKey<EntityType<?>>
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_PASSIVE = createDiscoveryBehavior("passive"),
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_NEUTRAL = createDiscoveryBehavior("neutral"),
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_HOSTILE = createDiscoveryBehavior("hostile"),
        DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_BOSS    = createDiscoveryBehavior("boss");

    static TagKey<EntityType<?>> create(String id) {
        return TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(SpyglassPlus.MOD_ID, id));
    }

    static TagKey<EntityType<?>> createDiscoveryBehavior(String id) {
        return create("discovery_enchantment_entity_behavior/" + id);
    }
}
