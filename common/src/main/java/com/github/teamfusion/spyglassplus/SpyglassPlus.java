package com.github.teamfusion.spyglassplus;

import com.github.teamfusion.spyglassplus.client.SpyglassPlusClient;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.enchantment.target.SpyglassPlusEnchantmentTargets;
import com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItemGroups;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking;
import com.github.teamfusion.spyglassplus.sound.SpyglassPlusSoundEvents;
import com.google.common.reflect.Reflection;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public interface SpyglassPlus {
    String MOD_ID = "spyglassplus";
    String MOD_NAME = "Spyglass+";
    Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    static void commonInitialize() {
        LOGGER.info("Initializing {}", MOD_NAME);

        Reflection.initialize(SpyglassPlusEnchantmentTargets.class);

        SpyglassPlusItems.REGISTER.register();
        SpyglassPlusEntityType.REGISTER.register();
        SpyglassPlusEnchantments.REGISTER.register();
        SpyglassPlusSoundEvents.REGISTER.register();

        SpyglassPlusEntityType.postRegister();

        SpyglassPlusNetworking.registerReceivers();

        EnvExecutor.runInEnv(EnvType.CLIENT, () -> SpyglassPlusClient::commonClientInitialize);

        CreativeTabRegistry.modify(SpyglassPlusItemGroups.ALL, (flags, output, operator) -> {
            output.add(new ItemStack(Items.SPYGLASS));
            output.add(new ItemStack(SpyglassPlusItems.SPYGLASS_STAND.get()));

            Registries.ENCHANTMENT.forEach(enchantment -> {
                if (enchantment.type == SpyglassPlusEnchantmentTargets.SCOPING) {
                    for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
                        output.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, level)), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                    }
                }
            });
        });
    }
}
