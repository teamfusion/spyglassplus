package com.github.teamfusion.spyglassplus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface SpyglassPlus {
    String MOD_ID = "spyglassplus";
    String MOD_NAME = "Spyglass+";
    Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    default void commonInitialize() {
        LOGGER.info("Initializing {}", MOD_NAME);
    }
}
