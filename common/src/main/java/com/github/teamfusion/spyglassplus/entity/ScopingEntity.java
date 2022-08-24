package com.github.teamfusion.spyglassplus.entity;

import com.github.teamfusion.spyglassplus.mixin.PlayerEntityMixin;
import net.minecraft.item.ItemStack;

/**
 * An interface implemented into entities that can scope.
 *
 * @see ScopingPlayer ScopingPlayer, its player extension
 * @see PlayerEntityMixin
 * @see SpyglassStandEntity
 */
public interface ScopingEntity {
    /**
     * Retrieves the entity's active scoping stack. This includes
     * scoping stacks on the current active spyglass stand.
     */
    default ItemStack getScopingStack() {
        return ItemStack.EMPTY;
    }

    /**
     * Whether the entity is scoping in any way.
     */
    default boolean isScoping() {
        return false;
    }
}
