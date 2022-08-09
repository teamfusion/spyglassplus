package com.github.teamfusion.spyglassplus.entity;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface SpyglassPlusEntityType {
    DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(SpyglassPlus.MOD_ID, Registry.ENTITY_TYPE_KEY);

    RegistrySupplier<EntityType<SpyglassStandEntity>> SPYGLASS_STAND = register("spyglass_stand",
        Builder.<SpyglassStandEntity>create(SpyglassStandEntity::new, SpawnGroup.MISC)
               .setDimensions(0.6F, 1.9F)
               .maxTrackingRange(8 * 16)
    );

    static void postRegister() {
        EntityAttributeRegistry.register(SPYGLASS_STAND, SpyglassStandEntity::createLivingAttributes);
    }

    private static <T extends LivingEntity> RegistrySupplier<EntityType<T>> register(String id, Builder<T> builder) {
        return REGISTER.register(id, () -> builder.build(new Identifier(SpyglassPlus.MOD_ID, id).toString()));
    }
}
