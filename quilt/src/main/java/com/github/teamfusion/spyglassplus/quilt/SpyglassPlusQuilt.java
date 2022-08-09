package com.github.teamfusion.spyglassplus.quilt;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class SpyglassPlusQuilt implements SpyglassPlus, ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        SpyglassPlus.commonInitialize();
    }
}
