package com.github.teamfusion.spyglassplus.enchantment.target;

import com.github.teamfusion.spyglassplus.enchantment.ScopingEnchantment;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;

import java.util.function.Predicate;

public final class SpyglassPlusEnchantmentTargets {
    public static final EnchantmentTarget SCOPING = register("scoping", ScopingEnchantment::isAcceptableItem);

    private SpyglassPlusEnchantmentTargets() {
    }

    @ExpectPlatform
    private static EnchantmentTarget register(String id, Predicate<Item> predicate) {
        throw new AssertionError();
    }
}
