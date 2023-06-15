package com.github.teamfusion.spyglassplus.item;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface SpyglassPlusItemGroups {
    DeferredRegister<ItemGroup> REGISTER = DeferredRegister.create(SpyglassPlus.MOD_ID, RegistryKeys.ITEM_GROUP);

    RegistrySupplier<ItemGroup> ALL = REGISTER.register(new Identifier(SpyglassPlus.MOD_ID, "item_group"), () -> CreativeTabRegistry.create(Text.translatable("itemGroup." + SpyglassPlus.MOD_ID + ".item_group"), () -> new ItemStack(Items.SPYGLASS)));
}
