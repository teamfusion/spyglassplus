package com.github.teamfusion.spyglassplus.client;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.config.SpyglassPlusConfig;
import com.github.teamfusion.spyglassplus.client.entity.CommandTargetManager;
import com.github.teamfusion.spyglassplus.client.entity.IndicateTargetManager;
import com.github.teamfusion.spyglassplus.client.keybinding.SpyglassPlusKeyBindings;
import com.github.teamfusion.spyglassplus.client.model.entity.SpyglassPlusEntityModelLayers;
import com.github.teamfusion.spyglassplus.client.network.SpyglassPlusClientNetworking;
import com.github.teamfusion.spyglassplus.client.render.entity.SpyglassStandEntityRenderer;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.mixin.client.ModelPredicateProviderRegistryMixin;
import com.github.teamfusion.spyglassplus.mixin.client.access.KeyBindingAccessor;
import com.google.common.reflect.Reflection;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import static com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking.COMMAND_TRIGGERED_PACKET_ID;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public interface SpyglassPlusClient extends SpyglassPlus {
    ConfigHolder<SpyglassPlusConfig> CONFIG_HOLDER = AutoConfig.register(SpyglassPlusConfig.class, JanksonConfigSerializer::new);

    IndicateTargetManager INDICATE_TARGET_MANAGER = new IndicateTargetManager();
    CommandTargetManager COMMAND_TARGET_MANAGER = new CommandTargetManager();

    /**
     * @see ModelPredicateProviderRegistryMixin
     */
    static void commonClientInitialize() {
        LOGGER.info("Initializing {}-CLIENT", MOD_NAME);

        Reflection.initialize(SpyglassPlusEntityModelLayers.class, SpyglassPlusKeyBindings.class);

        EntityRendererRegistry.register(SpyglassPlusEntityType.SPYGLASS_STAND, SpyglassStandEntityRenderer::new);

        ClientTooltipEvent.ITEM.register(ISpyglass::appendLocalScrutinyLevelTooltip);
        SpyglassPlusClientNetworking.registerReceivers();

        ClientTickEvent.CLIENT_LEVEL_POST.register(SpyglassPlusClient::clientWorldTick);

        initializeConfig();
    }

    /**
     * Explicitly initializes {@link SpyglassPlusConfig} and registers its config screen across platforms.
     */
    static void initializeConfig() {
        Reflection.initialize(SpyglassPlusConfig.class);
        Platform.getMod(MOD_ID).registerConfigurationScreen(SpyglassPlusConfig::createScreen);
    }

    static void clientWorldTick(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player instanceof ScopingPlayer scopingPlayer && scopingPlayer.isScoping()) {
            ItemStack stack = scopingPlayer.getScopingStack();
            if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.COMMAND.get(), stack) > 0) {
                Window window = client.getWindow();
                int code = ((KeyBindingAccessor) SpyglassPlusKeyBindings.TRIGGER_COMMAND_ENCHANTMENT).getBoundKey().getCode();
                if (!(code >= 0 && code <= 7) && InputUtil.isKeyPressed(window.getHandle(), code)) {
                    NetworkManager.sendToServer(COMMAND_TRIGGERED_PACKET_ID, new PacketByteBuf(Unpooled.EMPTY_BUFFER));
                }
            }
        }
    }
}
