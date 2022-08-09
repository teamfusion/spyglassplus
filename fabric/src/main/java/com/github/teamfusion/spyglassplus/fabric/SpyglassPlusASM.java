package com.github.teamfusion.spyglassplus.fabric;

import com.chocohead.mm.api.ClassTinkerers;
import com.chocohead.mm.api.EnumAdder;
import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.util.Identifier;
import net.moddingplayground.frame.api.enchantment.v0.target.EnchantmentTargetInfo;

import java.util.List;
import java.util.stream.Collectors;

public final class SpyglassPlusASM implements Runnable {
    public static final EnchantmentTargetInfo SCOPING = new EnchantmentTargetInfo(
        new Identifier(SpyglassPlus.MOD_ID, "scoping"),
        "com.github.teamfusion.spyglassplus.enchantment.target.fabric.ScopingEnchantmentTarget"
    );

    public static final List<EnchantmentTargetInfo> TARGETS = List.of(
        SCOPING
    );

    @Override
    public void run() {
        FabricLoader loader = FabricLoader.getInstance();
        MappingResolver mappings = loader.getMappingResolver();
        EnumAdder adder = ClassTinkerers.enumBuilder(mappings.mapClassName("intermediary", "net.minecraft.class_1886"));
        TARGETS.stream().collect(Collectors.toMap(EnchantmentTargetInfo::getEnumName, EnchantmentTargetInfo::className)).forEach(adder::addEnumSubclass);
        adder.build();
    }
}
