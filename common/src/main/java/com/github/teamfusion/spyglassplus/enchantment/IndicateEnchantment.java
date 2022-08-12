package com.github.teamfusion.spyglassplus.enchantment;

/**
 * An enchantment that applies the glowing effect to entities within view when scoping.
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
    public int getMaxPower(int level) {
        return super.getMinPower(level) + 50;
    }
}
