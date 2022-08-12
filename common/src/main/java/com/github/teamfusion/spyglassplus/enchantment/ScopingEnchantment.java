package com.github.teamfusion.spyglassplus.enchantment;

import com.github.teamfusion.spyglassplus.enchantment.target.SpyglassPlusEnchantmentTargets;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;

public class ScopingEnchantment extends Enchantment {
    public ScopingEnchantment(Rarity weight) {
        super(weight, SpyglassPlusEnchantmentTargets.SCOPING, new EquipmentSlot[]{ EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }

    public static boolean isAcceptableItem(Item item) {
        return item instanceof ISpyglass;
    }
}
