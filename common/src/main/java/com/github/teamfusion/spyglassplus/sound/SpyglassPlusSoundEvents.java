package com.github.teamfusion.spyglassplus.sound;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface SpyglassPlusSoundEvents {
    DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(SpyglassPlus.MOD_ID, Registry.SOUND_EVENT_KEY);

    RegistrySupplier<SoundEvent> ITEM_BINOCULARS_USE = binoculars("use");
    RegistrySupplier<SoundEvent> ITEM_BINOCULARS_STOP_USING = binoculars("stop_using");
    RegistrySupplier<SoundEvent> ITEM_BINOCULARS_ADJUST = binoculars("adjust");
    RegistrySupplier<SoundEvent> ITEM_BINOCULARS_RESET_ADJUST = binoculars("reset_adjust");
    RegistrySupplier<SoundEvent> ITEM_SPYGLASS_ADJUST = spyglass("adjust");
    RegistrySupplier<SoundEvent> ITEM_SPYGLASS_RESET_ADJUST = spyglass("reset_adjust");
    RegistrySupplier<SoundEvent> ENTITY_SPYGLASS_STAND_PLACE = spyglassStand("place");
    RegistrySupplier<SoundEvent> ENTITY_SPYGLASS_STAND_BREAK = spyglassStand("break");
    RegistrySupplier<SoundEvent> ENTITY_SPYGLASS_STAND_HIT = spyglassStand("hit");
    RegistrySupplier<SoundEvent> ENTITY_SPYGLASS_STAND_FALL = spyglassStand("fall");
    RegistrySupplier<SoundEvent> ENTITY_SPYGLASS_STAND_SHRINK = spyglassStand("shrink");
    RegistrySupplier<SoundEvent> ENTITY_SPYGLASS_STAND_ENLARGE = spyglassStand("enlarge");

    private static RegistrySupplier<SoundEvent> binoculars(String id) {
        return item("binoculars", id);
    }

    static RegistrySupplier<SoundEvent> spyglass(String id) {
        return item("spyglass", id);
    }

    static RegistrySupplier<SoundEvent> item(String item, String id) {
        return register("item.%s.%s".formatted(item, id));
    }

    static RegistrySupplier<SoundEvent> spyglassStand(String id) {
        return entity("spyglass_stand", id);
    }

    static RegistrySupplier<SoundEvent> entity(String entity, String id) {
        return register("entity.%s.%s".formatted(entity, id));
    }

    static RegistrySupplier<SoundEvent> register(String id) {
        return REGISTER.register(id, () -> new SoundEvent(new Identifier(SpyglassPlus.MOD_ID, id)));
    }
}
