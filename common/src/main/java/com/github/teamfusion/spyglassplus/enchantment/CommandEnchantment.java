package com.github.teamfusion.spyglassplus.enchantment;

/**
 * @see SpyglassPlusEnchantments#COMMAND
 */
public class CommandEnchantment extends ScopingEnchantment {
    public CommandEnchantment(Rarity weight) {
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
