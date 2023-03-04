package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import com.github.teamfusion.spyglassplus.item.SpyglassStandItem;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@SuppressWarnings("deprecation")
@Environment(EnvType.CLIENT)
@Mixin(ModelPredicateProviderRegistry.class)
public abstract class ModelPredicateProviderRegistryMixin {
    @Shadow @Final private static Map<Item, Map<Identifier, ModelPredicateProvider>> ITEM_SPECIFIC;

    /**
     * Registers custom model predicates.
     */
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onStaticInit(CallbackInfo ci) {
        register(SpyglassPlusItems.SPYGLASS_STAND.get(), new Identifier(SpyglassPlus.MOD_ID, "small"), (stack, world, entity, seed) -> SpyglassStandItem.isSmall(stack) ? 1 : 0);
    }

    @Unique
    private static void register(Item item, Identifier id, ModelPredicateProvider provider) {
        ITEM_SPECIFIC.computeIfAbsent(item, key -> Maps.newHashMap())
                     .put(id, provider);
    }
}
