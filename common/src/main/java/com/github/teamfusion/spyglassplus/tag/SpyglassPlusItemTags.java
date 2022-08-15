package com.github.teamfusion.spyglassplus.tag;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface SpyglassPlusItemTags {
    /**
     * Contains items that can scope, usually those who implement {@link ISpyglass}.
     */
    TagKey<Item> SCOPING_ITEMS = create("scoping_items");

    static TagKey<Item> create(String id) {
        return TagKey.of(Registry.ITEM_KEY, new Identifier(SpyglassPlus.MOD_ID, id));
    }
}
