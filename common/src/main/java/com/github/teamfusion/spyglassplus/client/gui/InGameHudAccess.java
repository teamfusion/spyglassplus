package com.github.teamfusion.spyglassplus.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface InGameHudAccess {
    DiscoveryHudRenderer getDiscoveryHud();
}
