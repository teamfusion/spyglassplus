package com.github.teamfusion.spyglassplus.client.event;

import com.github.teamfusion.spyglassplus.client.gui.BinocularsOverlayRenderer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface BinocularsHudOverlayRenderEvent {
    /**
     * @see Pre#render(BinocularsOverlayRenderer, float, int, int)
     */
    Event<Pre> PRE = EventFactory.createEventResult();

    /**
     * @see Post#render(BinocularsOverlayRenderer, float, int, int)
     */
    Event<Post> POST = EventFactory.createLoop();

    @FunctionalInterface
    interface Pre {
        /**
         * Invoked before the binoculars overlay is rendered.
         *
         * @return An {@link EventResult} determining the outcome of the event,
         *         the execution may be cancelled by the result.
         */
        EventResult render(BinocularsOverlayRenderer binocularsOverlay, float scale, int scaledWidth, int scaledHeight);
    }

    @FunctionalInterface
    interface Post {
        /**
         * Invoked after the binoculars overlay is rendered.
         */
        void render(BinocularsOverlayRenderer binocularsOverlay, float scale, int scaledWidth, int scaledHeight);
    }
}
