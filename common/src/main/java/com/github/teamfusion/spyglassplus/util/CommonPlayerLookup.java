package com.github.teamfusion.spyglassplus.util;

import com.github.teamfusion.spyglassplus.mixin.access.EntityTrackerAccessor;
import com.github.teamfusion.spyglassplus.mixin.access.ThreadedAnvilChunkStorageAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.ChunkManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * From Fabric API PlayerLookup.
 */
public interface CommonPlayerLookup {
    static Collection<ServerPlayerEntity> tracking(Entity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        ChunkManager manager = entity.getWorld().getChunkManager();

        if (manager instanceof ServerChunkManager) {
            ThreadedAnvilChunkStorage storage = ((ServerChunkManager) manager).threadedAnvilChunkStorage;
            EntityTrackerAccessor tracker = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(entity.getId());

            // return an immutable collection to guard against accidental removals.
            if (tracker != null) {
                return tracker.getListeners()
                              .stream()
                              .map(EntityTrackingListener::getPlayer)
                              .collect(Collectors.toUnmodifiableSet());
            }

            return Collections.emptySet();
        }

        throw new IllegalArgumentException("Only supported on server worlds!");
    }
}
