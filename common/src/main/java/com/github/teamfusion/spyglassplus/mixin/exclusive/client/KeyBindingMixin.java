package com.github.teamfusion.spyglassplus.mixin.exclusive.client;

import com.github.teamfusion.spyglassplus.client.keybinding.SpyglassPlusKeyBindings;
import com.github.teamfusion.spyglassplus.mixin.client.access.KeyBindingAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * An incredible mixin that prevents {@link SpyglassPlusKeyBindings#TRIGGER_COMMAND_ENCHANTMENT} from
 * being added to relevant maps that the game loops through to set key states, and runs its own
 * logic instead.
 */
@SuppressWarnings("unchecked")
@Environment(EnvType.CLIENT)
@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    @Shadow @Final private static Map<InputUtil.Key, KeyBinding> KEY_TO_BINDINGS;
    @Shadow private InputUtil.Key boundKey;

    /* Vanilla Prevention */

    @ModifyArg(
        method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 1
        ),
        index = 1
    )
    private <V> V onInitAddToKeyBindings(V value) {
        KeyBinding that = (KeyBinding) (Object) this;
        if (that == SpyglassPlusKeyBindings.TRIGGER_COMMAND_ENCHANTMENT) {
            return (V) KEY_TO_BINDINGS.get(this.boundKey);
        }

        return value;
    }

    @ModifyArg(
        method = "updateKeysByCode",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 0
        ),
        index = 1
    )
    private static <V> V onUpdateKeysByCodeAddToKeyBindings(V value) {
        KeyBinding keyBinding = (KeyBinding) value;
        if (keyBinding == SpyglassPlusKeyBindings.TRIGGER_COMMAND_ENCHANTMENT) {
            KeyBindingAccessor access = (KeyBindingAccessor) keyBinding;
            return (V) KEY_TO_BINDINGS.get(access.getBoundKey());
        }

        return value;
    }

    /* Custom Logic */

    @Inject(method = "onKeyPressed", at = @At("HEAD"))
    private static void onOnKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        KeyBinding keyBinding = SpyglassPlusKeyBindings.TRIGGER_COMMAND_ENCHANTMENT;
        KeyBindingAccessor access = (KeyBindingAccessor) keyBinding;
        if (access.getBoundKey() == key) {
            access.setTimesPressed(access.getTimesPressed() + 1);
        }
    }

    @Inject(method = "setKeyPressed", at = @At("HEAD"))
    private static void onSetKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        KeyBinding keyBinding = SpyglassPlusKeyBindings.TRIGGER_COMMAND_ENCHANTMENT;
        KeyBindingAccessor access = (KeyBindingAccessor) keyBinding;
        if (access.getBoundKey() == key) {
            keyBinding.setPressed(pressed);
        }
    }
}
