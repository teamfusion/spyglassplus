package com.github.teamfusion.spyglassplus.enchantment.target.fabric;

import com.github.teamfusion.spyglassplus.enchantment.target.SpyglassPlusEnchantmentTargetsEntrypoint;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;

import java.util.function.Predicate;

public final class SpyglassPlusEnchantmentTargetsImpl {
    public static EnchantmentTarget register(String id, Predicate<Item> predicate) {
        return EnchantmentTarget.valueOf(SpyglassPlusEnchantmentTargetsEntrypoint.SCOPING.getEnumName());
    }
}
