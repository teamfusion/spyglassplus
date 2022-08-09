package com.github.teamfusion.spyglassplus.forge;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.minecraftforge.fml.common.Mod;

@Mod(SpyglassPlus.MOD_ID)
public final class SpyglassPlusForge implements SpyglassPlus {
    public SpyglassPlusForge() {
        this.commonInitialize();
    }
}
