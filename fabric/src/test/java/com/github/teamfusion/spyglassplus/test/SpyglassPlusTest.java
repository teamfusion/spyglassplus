package com.github.teamfusion.spyglassplus.test;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.fabricmc.api.ModInitializer;

public class SpyglassPlusTest implements ModInitializer, SpyglassPlus {
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}-TEST", MOD_NAME);
    }
}
