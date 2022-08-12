package com.github.teamfusion.spyglassplus.enchantment.target.fabric;

import com.github.teamfusion.spyglassplus.enchantment.ScopingEnchantment;
import com.github.teamfusion.spyglassplus.fabric.SpyglassPlusASM;
import com.github.teamfusion.spyglassplus.mixin.fabric.EnchantmentTargetMixin;
import net.minecraft.item.Item;

/**
 * @implNote Implemented through {@link SpyglassPlusASM}
 */
@SuppressWarnings("unused")
public class ScopingEnchantmentTarget extends EnchantmentTargetMixin {
    @Override
    public boolean isAcceptableItem(Item item) {
        return ScopingEnchantment.isAcceptableItem(item);
    }
}
