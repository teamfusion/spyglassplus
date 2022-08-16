package com.github.teamfusion.spyglassplus.client.event;

import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public interface DiscoveryHudRenderEvent {
    /**
     * @see Pre#render(DiscoveryHudRenderer, MatrixStack, float, Entity)
     */
    Event<Pre> PRE = EventFactory.createEventResult();

    /**
     * @see Post#render(DiscoveryHudRenderer, MatrixStack, float, Entity)
     */
    Event<Post> POST = EventFactory.createLoop();

    @FunctionalInterface
    interface Pre {
        /**
         * Invoked before the discovery HUD is rendered.
         * @return An {@link EventResult} determining the outcome of the event,
         *         the execution may be cancelled by the result.
         */
        EventResult render(DiscoveryHudRenderer discoveryHud, MatrixStack matrices, float tickDelta, Entity camera);
    }

    @FunctionalInterface
    interface Post {
        /**
         * Invoked after the discovery HUD is rendered.
         */
        void render(DiscoveryHudRenderer discoveryHud, MatrixStack matrices, float tickDelta, Entity camera);
    }
}
