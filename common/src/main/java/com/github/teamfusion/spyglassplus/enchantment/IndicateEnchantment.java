package com.github.teamfusion.spyglassplus.enchantment;

/**
 * @see SpyglassPlusEnchantments#INDICATE
 */
public class IndicateEnchantment extends ScopingEnchantment {
    public IndicateEnchantment(Rarity weight) {
        super(weight);
    }

    @Override
    public int getMinPower(int level) {
        return 15;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
