package com.github.teamfusion.spyglassplus.enchantment.target;

import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.minecraft.item.Item;
import net.moddingplayground.frame.api.enchantment.v0.target.CustomEnchantmentTarget;

/**
 * @implNote Implemented through {@link SpyglassPlusEnchantmentTargetsEntrypoint}
 */
@SuppressWarnings("unused")
public class ScopingEnchantmentTarget extends CustomEnchantmentTarget {
    public boolean isAcceptableItem(Item item) {
        return item instanceof ISpyglass;
    }
}
