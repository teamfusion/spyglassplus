package com.github.teamfusion.spyglassplus.enchantment;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

import static net.minecraft.enchantment.Enchantment.Rarity.COMMON;
import static net.minecraft.enchantment.Enchantment.Rarity.UNCOMMON;

public interface SpyglassPlusEnchantments {
    DeferredRegister<Enchantment> REGISTER = DeferredRegister.create(SpyglassPlus.MOD_ID, Registry.ENCHANTMENT_KEY);

    RegistrySupplier<Enchantment> SCRUTINY = register("scrutiny", new ScrutinyEnchantment(UNCOMMON));
    RegistrySupplier<Enchantment> ILLUMINATE = register("illuminate", new IlluminateEnchantment(UNCOMMON));
    RegistrySupplier<Enchantment> INDICATE = register("indicate", new IndicateEnchantment(UNCOMMON));
    RegistrySupplier<Enchantment> DISCOVERY = register("discovery", new DiscoveryEnchantment(UNCOMMON));
    RegistrySupplier<Enchantment> COMMAND = register("command", new CommandEnchantment(COMMON));

    private static RegistrySupplier<Enchantment> register(String id, Enchantment enchantment) {
        return REGISTER.register(id, () -> enchantment);
    }
}
