package com.github.teamfusion.spyglassplus.enchantment.target.fabric;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.moddingplayground.frame.api.enchantment.v0.target.EnchantmentTargetInfo;

import java.util.function.Predicate;

public final class SpyglassPlusEnchantmentTargetsImpl {
    public static EnchantmentTarget register(String id, Predicate<Item> predicate) {
        return new EnchantmentTargetInfo(new Identifier(SpyglassPlus.MOD_ID, id), "").getEnchantmentTarget();
    }
}
