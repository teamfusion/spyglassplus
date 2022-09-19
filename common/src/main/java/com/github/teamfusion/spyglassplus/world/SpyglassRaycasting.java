package com.github.teamfusion.spyglassplus.world;

import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.github.teamfusion.spyglassplus.mixin.access.EntityInvoker;
import com.github.teamfusion.spyglassplus.tag.SpyglassPlusEntityTypeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Responsible for spyglass raycasting functionality.
 */
public interface SpyglassRaycasting {
    double MAX_RAYCAST_DISTANCE = 64.0D;

    /**
     * Retrieves the entity that the camera is looking at.
     */
    static Entity raycast(Entity camera, Vec2f rotation, float tickDelta, double distance, Predicate<Entity> predicate) {
        // calculate a position vector from the camera's rotation
        Vec3d vector = ((EntityInvoker) camera).invokeGetRotationVector(rotation.y, rotation.x);

        // calculate minimum and maximum points of raycast
        Vec3d min = camera.getCameraPosVec(tickDelta);
        Vec3d max = min.add(vector.x * distance, vector.y * distance, vector.z * distance);

        // grab default hit result
        HitResult hit = camera.world.raycast(new RaycastContext(min, max, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, camera));
        if (hit != null) {
            distance = hit.getPos().squaredDistanceTo(min);
        }

        // calculate entity hit result
        Box net = camera.getBoundingBox().stretch(vector.multiply(distance)).expand(1.0F);
        EntityHitResult entityHit = raycast(camera, min, max, net, entity -> isVisibleToRaycast(entity, camera instanceof PlayerEntity player ? player : null) && predicate.test(entity), distance);

        if (entityHit != null) {
            Entity entity = entityHit.getEntity();
            Vec3d pos = entityHit.getPos();
            double entityDistance = min.squaredDistanceTo(pos);
            if (entityDistance < distance || hit == null) {
                return entity;
            }
        }

        return null;
    }

    static Entity raycast(Entity camera, Vec2f rotation, float tickDelta) {
        return raycast(camera, rotation, tickDelta, MAX_RAYCAST_DISTANCE, entity -> true);
    }

    static Entity raycast(Entity camera, Predicate<Entity> predicate) {
        return raycast(camera, getRotation(camera), 1.0F, MAX_RAYCAST_DISTANCE, predicate);
    }

    static Entity raycast(Entity camera) {
        return raycast(camera, getRotation(camera), 1.0F);
    }

    static boolean isVisibleToRaycast(Entity entity, @Nullable PlayerEntity viewer) {
        return !entity.isSpectator() && !entity.getType().isIn(SpyglassPlusEntityTypeTags.IGNORE_DISCOVERY) && (viewer == null || !entity.isInvisibleTo(viewer));
    }

    /**
     * Modified and mapped version of {@link ProjectileUtil#raycast(Entity, Vec3d, Vec3d, Box, Predicate, double)}.
     * <p>Modifies the result of {@link Entity#getTargetingMargin()}.</p>
     */
    static EntityHitResult raycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double distance) {
        double runningDistance = distance;
        Entity resultEntity = null;
        Vec3d resultPos = null;

        for (Entity candidate : entity.world.getOtherEntities(entity, box, predicate)) {
            float margin = candidate.getTargetingMargin();
            Box candidateBoundingBox = candidate.getBoundingBox().expand(
                margin == 0.0F && !candidate.getType().isIn(SpyglassPlusEntityTypeTags.IGNORE_MARGIN_EXPANSION_DISCOVERY)
                    ? 0.175F : margin
            );

            Optional<Vec3d> optional = candidateBoundingBox.raycast(min, max);
            if (candidateBoundingBox.contains(min)) {
                if (!(runningDistance >= 0.0)) continue;
                resultEntity = candidate;
                resultPos = optional.orElse(min);
                runningDistance = 0.0;
                continue;
            }

            double squaredDistance;
            Vec3d runningPos;
            if (optional.isEmpty() || !((squaredDistance = min.squaredDistanceTo(runningPos = optional.get())) < runningDistance) && runningDistance != 0.0) {
                continue;
            }
            if (candidate.getRootVehicle() == entity.getRootVehicle()) {
                if (runningDistance != 0.0) continue;
                resultEntity = candidate;
                resultPos = runningPos;
                continue;
            }

            resultEntity = candidate;
            resultPos = runningPos;
            runningDistance = squaredDistance;
        }

        return resultEntity == null ? null : new EntityHitResult(resultEntity, resultPos);
    }

    static Vec2f getRotation(Entity camera) {
        return camera instanceof SpyglassStandEntity stand
            ? new Vec2f(stand.getSpyglassYaw(), stand.getSpyglassPitch())
            : new Vec2f(camera.getYaw(), camera.getPitch());
    }
}
