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
        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.IGNORE_DISCOVERY).add(
            LEASH_KNOT,
            PAINTING
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.IGNORE_MARGIN_EXPANSION_DISCOVERY).add(
            SHULKER,
            ITEM_FRAME
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.IGNORE_STATS_DISCOVERY).add(
            SPYGLASS_STAND.get(),
            ARMOR_STAND,
            PLAYER
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.DO_NOT_RENDER_BOX_DISCOVERY).add(
            ENDER_DRAGON
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_PASSIVE).add(
            ALLAY,
            AXOLOTL,
            BAT,
            CAT,
            CHICKEN,
            COD,
            COW,
            DONKEY,
            FOX,
            FROG,
            GLOW_SQUID,
            HORSE,
            MOOSHROOM,
            MULE,
            OCELOT,
            PARROT,
            PIG,
            PUFFERFISH,
            RABBIT,
            SALMON,
            SHEEP,
            SKELETON_HORSE,
            SNOW_GOLEM,
            SQUID,
            STRIDER,
            TADPOLE,
            TROPICAL_FISH,
            TURTLE,
            VILLAGER,
            WANDERING_TRADER
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_NEUTRAL).add(
            BEE,
            CAVE_SPIDER,
            DOLPHIN,
            ENDERMAN,
            GOAT,
            IRON_GOLEM,
            LLAMA,
            PANDA,
            PIGLIN,
            POLAR_BEAR,
            SPIDER,
            TRADER_LLAMA,
            WOLF,
            ZOMBIFIED_PIGLIN
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_HOSTILE).add(
            BLAZE,
            CREEPER,
            DROWNED,
            ELDER_GUARDIAN,
            ENDERMITE,
            EVOKER,
            GHAST,
            GUARDIAN,
            HOGLIN,
            HUSK,
            MAGMA_CUBE,
            PHANTOM,
            PIGLIN_BRUTE,
            PILLAGER,
            RAVAGER,
            SHULKER,
            SILVERFISH,
            SKELETON_HORSE,
            SLIME,
            STRAY,
            VEX,
            VINDICATOR,
            WARDEN,
            WITCH,
            WITHER_SKELETON,
            ZOGLIN,
            ZOMBIE,
            ZOMBIE_VILLAGER
        );

        this.getOrCreateTagBuilder(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_BOSS).add(
            ENDER_DRAGON,
            WITHER
        );
    }
}
