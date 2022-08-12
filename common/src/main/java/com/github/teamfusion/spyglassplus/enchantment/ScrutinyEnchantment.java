package com.github.teamfusion.spyglassplus.enchantment;

/**
 * An enchantment that increases the zoom capability of scoping items.
 * <p>Can be adjusted through scrolling, dependent on level.</p>
 *
 * @see SpyglassPlusEnchantments#SCRUTINY
 */
public class ScrutinyEnchantment extends ScopingEnchantment {
    public ScrutinyEnchantment(Rarity weight) {
        super(weight);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinPower(int level) {
        return 1 + 10 * (level - 1);
    }

    @Override
    public int getMaxPower(int level) {
        return super.getMinPower(level) + 50;
    }
}
