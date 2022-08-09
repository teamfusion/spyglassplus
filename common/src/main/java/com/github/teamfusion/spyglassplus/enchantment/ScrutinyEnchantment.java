package com.github.teamfusion.spyglassplus.enchantment;

/**
 * @see SpyglassPlusEnchantments#SCRUTINY
 */
public class ScrutinyEnchantment extends ScopingEnchantment {
    public ScrutinyEnchantment(Rarity weight) {
        super(weight);
    }

    @Override
    public int getMinPower(int level) {
        return 15;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
