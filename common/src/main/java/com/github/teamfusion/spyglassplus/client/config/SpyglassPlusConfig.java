package com.github.teamfusion.spyglassplus.client.config;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.SpyglassPlusClient;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.Config.Gui.Background;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

/**
 * Config for {@link SpyglassPlus}.
 */
@Environment(EnvType.CLIENT)
@Background(Background.TRANSPARENT)
@Config(name = SpyglassPlus.MOD_ID)
public class SpyglassPlusConfig implements ConfigData {
    @CollapsibleObject(startExpanded = true)
    public DisplayConfig display = new DisplayConfig();

    public static class DisplayConfig {
        @CollapsibleObject(startExpanded = true)
        public DiscoveryHudConfig discoveryHud = new DiscoveryHudConfig();

        public static class DiscoveryHudConfig {
            @Comment("Whether or not the Discovery HUD opens with a zoom animation, matching the spyglass opening animation.")
            @Tooltip(count = 3)
            public boolean openWithZoom = true;

            @Comment("Whether or not the eye will display as open at any time. Accessibility option for possible triggers.")
            @Tooltip(count = 3)
            public boolean eyeOpens = true;

            @Comment("Whether or not the Discovery HUD has an extra 'step' before fully closing.")
            @Tooltip(count = 2)
            public boolean trailOff = true;
        }
    }

    /**
     * @return the instance of this config class stored in {@link SpyglassPlusClient#CONFIG_HOLDER}
     */
    public static SpyglassPlusConfig get() {
        return SpyglassPlusClient.CONFIG_HOLDER.getConfig();
    }

    public static Screen createScreen(Screen parent) {
        return AutoConfig.getConfigScreen(SpyglassPlusConfig.class, parent).get();
    }
}
