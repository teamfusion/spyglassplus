package com.github.teamfusion.spyglassplus.enchantment;

/**
 * An enchantment that applies a night vision effect on scoping items.
 * @see SpyglassPlusEnchantments#ILLUMINATE
 */
public class IlluminateEnchantment extends ScopingEnchantment {
    public IlluminateEnchantment(Rarity weight) {
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
