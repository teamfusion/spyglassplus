package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.minecraft.item.SpyglassItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SpyglassItem.class)
public class SpyglassItemMixin implements ISpyglass {}
