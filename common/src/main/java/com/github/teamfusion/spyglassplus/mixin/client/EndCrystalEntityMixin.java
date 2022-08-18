package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.entity.DiscoveryHudEntitySetup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * @see DiscoveryHudRenderer#drawEntity(int, int, float, float, int, Entity)
 */
@Environment(EnvType.CLIENT)
@Mixin(EndCrystalEntity.class)
public abstract class EndCrystalEntityMixin implements DiscoveryHudEntitySetup {
    @Unique private static final String SHOW_BOTTOM_KEY = "ShowBottom";

    @Shadow public abstract boolean shouldShowBottom();
    @Shadow public abstract void setShowBottom(boolean showBottom);

    @Override
    public void setupBeforeDiscoveryHud(NbtCompound nbt, float yaw, float pitch, float yawOffset, float pitchOffset) {
        nbt.putBoolean(SHOW_BOTTOM_KEY, this.shouldShowBottom());
        this.setShowBottom(false);
    }

    @Override
    public void cleanupAfterDiscoveryHud(NbtCompound nbt) {
        this.setShowBottom(nbt.getBoolean(SHOW_BOTTOM_KEY));
    }
}
