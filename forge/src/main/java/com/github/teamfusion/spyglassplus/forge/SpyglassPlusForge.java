package com.github.teamfusion.spyglassplus.forge;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SpyglassPlus.MOD_ID)
public final class SpyglassPlusForge implements SpyglassPlus {
    public SpyglassPlusForge() {
        EventBuses.registerModEventBus(MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SpyglassPlus.commonInitialize();
    }
}
