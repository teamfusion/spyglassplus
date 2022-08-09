package com.github.teamfusion.spyglassplus.enchantment.target;

import com.github.teamfusion.spyglassplus.item.ISpyglass;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;

import java.util.function.Predicate;

public final class SpyglassPlusEnchantmentTargets {
    public static EnchantmentTarget SCOPING = register("scoping", item -> item instanceof ISpyglass);

    private SpyglassPlusEnchantmentTargets() {
    }

    @ExpectPlatform
    private static EnchantmentTarget register(String id, Predicate<Item> predicate) {
        throw new AssertionError();
    }
}
