package com.github.teamfusion.spyglassplus.fabric;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.fabricmc.api.ModInitializer;

public final class SpyglassPlusFabric implements SpyglassPlus, ModInitializer {
    @Override
    public void onInitialize() {
        SpyglassPlus.commonInitialize();
    }
}
