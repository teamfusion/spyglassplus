package com.github.teamfusion.spyglassplus.client;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.config.SpyglassPlusConfig;
import com.github.teamfusion.spyglassplus.client.entity.IndicateTargetManager;
import com.github.teamfusion.spyglassplus.client.model.entity.SpyglassPlusEntityModelLayers;
import com.github.teamfusion.spyglassplus.client.network.SpyglassPlusClientNetworking;
import com.github.teamfusion.spyglassplus.client.render.entity.SpyglassStandEntityRenderer;
import com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.mixin.client.ModelPredicateProviderRegistryMixin;
import com.google.common.reflect.Reflection;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public interface SpyglassPlusClient extends SpyglassPlus {
    ConfigHolder<SpyglassPlusConfig> CONFIG_HOLDER = AutoConfig.register(SpyglassPlusConfig.class, JanksonConfigSerializer::new);
    IndicateTargetManager INDICATE_TARGET_MANAGER = new IndicateTargetManager();

    /**
     * @see ModelPredicateProviderRegistryMixin
     */
    static void commonClientInitialize() {
        LOGGER.info("Initializing {}-CLIENT", MOD_NAME);

        Reflection.initialize(SpyglassPlusEntityModelLayers.class);

        EntityRendererRegistry.register(SpyglassPlusEntityType.SPYGLASS_STAND, SpyglassStandEntityRenderer::new);

        ClientTooltipEvent.ITEM.register(ISpyglass::appendLocalScrutinyLevelTooltip);
        SpyglassPlusClientNetworking.registerReceivers();

        initializeClothConfig();
    }

    /**
     * Explicitly initializes {@link SpyglassPlusConfig} and registers its config screen across platforms.
     */
    static void initializeClothConfig() {
        Reflection.initialize(SpyglassPlusConfig.class);
        Platform.getMod(MOD_ID).registerConfigurationScreen(SpyglassPlusConfig::createScreen);
    }
}
