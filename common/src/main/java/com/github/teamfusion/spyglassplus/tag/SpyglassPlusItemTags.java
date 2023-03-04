package com.github.teamfusion.spyglassplus.tag;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface SpyglassPlusItemTags {
    /**
     * Contains items that can scope, usually those who implement {@link ISpyglass}.
     */
    TagKey<Item> SCOPING_ITEMS = create("scoping_items");

    /**
     * Contains items valid for a {@link SpyglassPlusEntityType#SPYGLASS_STAND}.
     */
    TagKey<Item> SPYGLASS_STAND_ITEMS = create("spyglass_stand_items");

    static TagKey<Item> create(String id) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(SpyglassPlus.MOD_ID, id));
    }
}
