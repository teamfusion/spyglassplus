package com.github.teamfusion.spyglassplus.entity;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;

/**
 * An interface implemented into all {@link PlayerEntity} instances.
 */
public interface ScopingPlayer extends ScopingEntity {
    static ScopingPlayer cast(PlayerEntity player) {
        return (ScopingPlayer) player;
    }

    default void setSpyglassStand(Integer id) {
    }

    default void setSpyglassStandEntity(SpyglassStandEntity entity) {
    }

    default boolean hasSpyglassStand() {
        return false;
    }

    default Optional<Integer> getSpyglassStand() {
        return Optional.empty();
    }

    /**
     * Retrieves the {@link SpyglassStandEntity} instance attached to the result of {@link #getSpyglassStand()}.
     */
    default Optional<SpyglassStandEntity> getSpyglassStandEntity() {
        return Optional.empty();
    }
}
