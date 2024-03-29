package com.github.teamfusion.spyglassplus.client.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface LivingEntityClientAccess {
    List<StatusEffectInstance> getEffects();
    void setEffects(List<StatusEffectInstance> effects);
}
