package com.github.teamfusion.spyglassplus.enchantment;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;

import static net.minecraft.enchantment.Enchantment.Rarity.COMMON;
import static net.minecraft.enchantment.Enchantment.Rarity.UNCOMMON;

public interface SpyglassPlusEnchantments {
    DeferredRegister<Enchantment> REGISTER = DeferredRegister.create(SpyglassPlus.MOD_ID, Registry.ENCHANTMENT_KEY);

    RegistrySupplier<Enchantment> SCRUTINY = register("scrutiny", ScrutinyEnchantment::new, UNCOMMON);
    RegistrySupplier<Enchantment> ILLUMINATE = register("illuminate", IlluminateEnchantment::new, UNCOMMON);
    RegistrySupplier<Enchantment> INDICATE = register("indicate", IndicateEnchantment::new, UNCOMMON);
    RegistrySupplier<Enchantment> DISCOVERY = register("discovery", DiscoveryEnchantment::new, UNCOMMON);
    RegistrySupplier<Enchantment> COMMAND = register("command", CommandEnchantment::new, COMMON);

    private static RegistrySupplier<Enchantment> register(String id, Function<Enchantment.Rarity, Enchantment> factory, Enchantment.Rarity rarity) {
        Enchantment enchantment = factory.apply(rarity);
        return REGISTER.register(id, () -> enchantment);
    }
}
