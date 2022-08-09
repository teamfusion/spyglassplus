package com.github.teamfusion.spyglassplus.enchantment;

/**
 * @see SpyglassPlusEnchantments#DISCOVERY
 */
public class DiscoveryEnchantment extends ScopingEnchantment {
    public DiscoveryEnchantment(Rarity rarity) {
        super(rarity);
    }

    @Override
    public int getMinPower(int level) {
        return level * 25;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
