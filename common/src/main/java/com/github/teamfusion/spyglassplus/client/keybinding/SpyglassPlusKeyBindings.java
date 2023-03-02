package com.github.teamfusion.spyglassplus.client.keybinding;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public interface SpyglassPlusKeyBindings {
    String CATEGORY = "key.category." + SpyglassPlus.MOD_ID;

    KeyBinding COMMAND_TARGET = register("command_target", InputUtil.UNKNOWN_KEY.getCode());
    KeyBinding COMMAND_UNTARGET = register("command_untarget", InputUtil.UNKNOWN_KEY.getCode());

    private static KeyBinding register(String id, int code) {
        KeyBinding binding = new KeyBinding("key.%s.%s".formatted(SpyglassPlus.MOD_ID, id), code, CATEGORY);
        KeyMappingRegistry.register(binding);
        return binding;
    }
}
