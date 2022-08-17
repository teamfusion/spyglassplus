package com.github.teamfusion.spyglassplus.test.client;

import com.github.teamfusion.spyglassplus.client.SpyglassPlusClient;
import com.github.teamfusion.spyglassplus.client.event.BinocularsHudOverlayRenderEvent;
import com.github.teamfusion.spyglassplus.client.event.DiscoveryHudRenderEvent;
import dev.architectury.event.EventResult;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SpyglassPlusClientTest implements ClientModInitializer, SpyglassPlusClient {
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {}-TEST-CLIENT", MOD_NAME);

        DiscoveryHudRenderEvent.PRE.register((discoveryHud, matrices, tickDelta, camera) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            return client.player.hasStatusEffect(StatusEffects.UNLUCK) ? EventResult.interruptFalse() : EventResult.pass();
        });

        DiscoveryHudRenderEvent.POST.register((discoveryHud, matrices, tickDelta, camera) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.player.sendMessage(Text.literal(String.format(
                "Closing: %s | %.2f", discoveryHud.isEyeClosing() ? "Y" : "N", discoveryHud.getEyePhase()
            )), true);
        });

        BinocularsHudOverlayRenderEvent.PRE.register((binocularsOverlay, scale, scaledWidth, scaledHeight) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            return client.player.hasStatusEffect(StatusEffects.UNLUCK) ? EventResult.interruptFalse() : EventResult.pass();
        });
    }
}
