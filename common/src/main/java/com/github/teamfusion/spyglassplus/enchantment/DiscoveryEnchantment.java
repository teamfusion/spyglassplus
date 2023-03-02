package com.github.teamfusion.spyglassplus.enchantment;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

/**
 * An enchantment that displays statistics about the targeted entity when scoping.
 * <p>This information includes health, strength, behavior, name, etc. This depends on the level.</p>
 *
 * @see SpyglassPlusEnchantments#DISCOVERY
 */
public class DiscoveryEnchantment extends ScopingEnchantment {
    public DiscoveryEnchantment(Rarity rarity) {
        super(rarity);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    public static int getLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(SpyglassPlusEnchantments.DISCOVERY.get(), stack);
    }
}
