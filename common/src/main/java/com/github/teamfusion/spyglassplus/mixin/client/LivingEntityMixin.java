package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.entity.LivingEntityClientAccess;
import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.entity.DiscoveryHudEntitySetup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.List;

/**
 * @see DiscoveryHudRenderer#drawEntity(int, int, float, float, int, Entity)
 */
@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityClientAccess, DiscoveryHudEntitySetup {
    @Shadow public float bodyYaw;
    @Shadow public float prevHeadYaw;
    @Shadow public float headYaw;

    @Unique private static final String
        BODY_YAW_KEY = "bodyYaw",
        PREV_HEAD_YAW_KEY = "prevHeadYaw",
        HEAD_YAW_KEY = "headYaw";

    @Unique private List<StatusEffectInstance> clientEffects = Collections.emptyList();

    private LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    @Override
    public List<StatusEffectInstance> getEffects() {
        return this.clientEffects;
    }

    @Unique
    @Override
    public void setEffects(List<StatusEffectInstance> effects) {
        this.clientEffects = effects;
    }

    @Unique
    @Override
    public void setupBeforeDiscoveryHud(NbtCompound nbt, float yaw, float pitch, float yawOffset, float pitchOffset) {
        nbt.putFloat(BODY_YAW_KEY, this.bodyYaw);
        nbt.putFloat(PREV_HEAD_YAW_KEY, this.prevHeadYaw);
        nbt.putFloat(HEAD_YAW_KEY, this.headYaw);

        this.bodyYaw = 180.0f + yawOffset * 20.0f;
        this.headYaw = yaw;
        this.prevHeadYaw = yaw;
    }

    @Unique
    @Override
    public void cleanupAfterDiscoveryHud(NbtCompound nbt) {
        this.bodyYaw = nbt.getFloat(BODY_YAW_KEY);
        this.prevHeadYaw = nbt.getFloat(PREV_HEAD_YAW_KEY);
        this.headYaw = nbt.getFloat(HEAD_YAW_KEY);
    }
}
