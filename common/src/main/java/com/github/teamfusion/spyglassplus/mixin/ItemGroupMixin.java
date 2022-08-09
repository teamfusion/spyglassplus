package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItemGroups;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ItemGroup.class)
public class ItemGroupMixin {
    /**
     * Platform-agnostic custom stack appending.
     */
    @Inject(method = "appendStacks", at = @At("HEAD"), cancellable = true)
    private void onAppendStacks(DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        ItemGroup that = (ItemGroup) (Object) this;
        if (that != SpyglassPlusItemGroups.ALL) return;

        Registry.ITEM.stream()
                     .filter(ISpyglass.class::isInstance)
                     .filter(item -> Objects.nonNull(item.getGroup()))
                     .map(ItemStack::new)
                     .forEach(stacks::add);

        stacks.add(new ItemStack(SpyglassPlusItems.SPYGLASS_STAND.get()));

        Registry.ITEM.stream()
                     .filter(EnchantedBookItem.class::isInstance)
                     .forEach(item -> item.appendStacks(that, stacks));

        ci.cancel();
    }
}
