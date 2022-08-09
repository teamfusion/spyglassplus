package com.github.teamfusion.spyglassplus.enchantment.target;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.minecraft.util.Identifier;
import net.moddingplayground.frame.api.enchantment.v0.FrameEnchantmentTargetsEntrypoint;
import net.moddingplayground.frame.api.enchantment.v0.target.EnchantmentTargetInfo;
import net.moddingplayground.frame.impl.enchantment.EnchantmentTargetManager;

public class SpyglassPlusEnchantmentTargetsEntrypoint implements FrameEnchantmentTargetsEntrypoint {
    public static EnchantmentTargetInfo SCOPING;

    @Override
    public void registerEnchantmentTargets(EnchantmentTargetManager manager) {
        SCOPING = manager.register(new Identifier(SpyglassPlus.MOD_ID, "scoping"), "com.github.teamfusion.spyglassplus.enchantment.target.ScopingEnchantmentTarget");
    }
}
