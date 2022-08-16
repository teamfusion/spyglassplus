package com.github.teamfusion.spyglassplus.entity;

import net.minecraft.nbt.NbtCompound;

public interface DiscoveryHudEntitySetup {
    void setupBeforeDiscoveryHud(NbtCompound nbt, float yaw, float pitch, float yawOffset, float pitchOffset);
    void cleanupAfterDiscoveryHud(NbtCompound nbt);
}
