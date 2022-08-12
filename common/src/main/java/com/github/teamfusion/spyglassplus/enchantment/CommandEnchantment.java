package com.github.teamfusion.spyglassplus.enchantment;

/**
 * An enchantment that notifies allied entities to attack the targeted entity when scoping.
 * @see SpyglassPlusEnchantments#COMMAND
 */
public class CommandEnchantment extends ScopingEnchantment {
    public CommandEnchantment(Rarity weight) {
        super(weight);
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
}
