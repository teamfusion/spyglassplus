package com.github.teamfusion.spyglassplus.item;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface SpyglassPlusItems {
    DeferredRegister<Item> REGISTER = DeferredRegister.create(SpyglassPlus.MOD_ID, Registry.ITEM_KEY);

    RegistrySupplier<Item> BINOCULARS = unstackable("binoculars", noGroup(BinocularsItem::new));
    RegistrySupplier<Item> SPYGLASS_STAND = register("spyglass_stand", modify(SpyglassStandItem::new, settings -> settings.maxCount(16)));

    private static Function<Item.Settings, Item> noGroup(Function<Item.Settings, Item> item) {
        return settings -> item.apply(settings.group(null));
    }

    private static Function<Item.Settings, Item> modify(Function<Item.Settings, Item> item, UnaryOperator<Item.Settings> modifier) {
        return settings -> item.apply(modifier.apply(settings));
    }

    private static RegistrySupplier<Item> register(String id, Function<Item.Settings, Item> item) {
        return register(id, () -> item.apply(new Item.Settings().group(SpyglassPlusItemGroups.ALL)));
    }

    private static RegistrySupplier<Item> unstackable(String id, Function<Item.Settings, Item> item) {
        return register(id, settings -> item.apply(settings.maxCount(1)));
    }

    private static RegistrySupplier<Item> register(String id, Supplier<Item> item) {
        return REGISTER.register(id, item);
    }
}
