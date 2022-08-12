package com.github.teamfusion.spyglassplus.client;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.model.entity.SpyglassPlusEntityModelLayers;
import com.github.teamfusion.spyglassplus.client.render.entity.SpyglassStandEntityRenderer;
import com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import com.github.teamfusion.spyglassplus.item.SpyglassStandItem;
import com.google.common.reflect.Reflection;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public interface SpyglassPlusClient extends SpyglassPlus {
    static void commonClientInitialize() {
        LOGGER.info("Initializing {}-CLIENT", MOD_NAME);

        Reflection.initialize(SpyglassPlusEntityModelLayers.class);
        EntityRendererRegistry.register(SpyglassPlusEntityType.SPYGLASS_STAND, SpyglassStandEntityRenderer::new);

        ClientTooltipEvent.ITEM.register(ISpyglass::appendLocalScrutinyLevelTooltip);

        ModelPredicateProviderRegistry.register(SpyglassPlusItems.SPYGLASS_STAND.get(), new Identifier(MOD_ID, "small"),
            (stack, world, entity, seed) -> SpyglassStandItem.isSmall(stack) ? 1 : 0
        );
    }
}
