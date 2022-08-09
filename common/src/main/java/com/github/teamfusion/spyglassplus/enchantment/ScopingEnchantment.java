package com.github.teamfusion.spyglassplus.enchantment;

import com.github.teamfusion.spyglassplus.enchantment.target.SpyglassPlusEnchantmentTargets;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;

public class ScopingEnchantment extends Enchantment {
    public ScopingEnchantment(Rarity weight) {
        super(weight, SpyglassPlusEnchantmentTargets.SCOPING, new EquipmentSlot[]{ EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }
}
