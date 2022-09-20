package com.github.teamfusion.spyglassplus.mixin.client.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor InputUtil.Key getBoundKey();

    @Accessor void setTimesPressed(int timesPressed);
    @Accessor int getTimesPressed();
}
