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

    KeyBinding TRIGGER_COMMAND_ENCHANTMENT = register("trigger_command_enchantment", InputUtil.Type.MOUSE, 0);

    private static KeyBinding register(String id, InputUtil.Type type, int code) {
        KeyBinding binding = new KeyBinding("key.%s.%s".formatted(SpyglassPlus.MOD_ID, id), type, code, CATEGORY);
        KeyMappingRegistry.register(binding);
        return binding;
    }
}
