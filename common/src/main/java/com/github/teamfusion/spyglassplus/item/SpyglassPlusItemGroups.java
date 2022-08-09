package com.github.teamfusion.spyglassplus.item;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.enchantment.target.SpyglassPlusEnchantmentTargets;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public interface SpyglassPlusItemGroups {
    ItemGroup ALL = CreativeTabRegistry.create(new Identifier(SpyglassPlus.MOD_ID, "item_group"), () -> new ItemStack(Items.SPYGLASS))
                                       .setEnchantments(SpyglassPlusEnchantmentTargets.SCOPING);
}
