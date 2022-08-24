package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.tag.SpyglassPlusItemTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements ScopingPlayer {
    @Shadow public abstract boolean isUsingSpyglass();

    @Unique private Optional<Integer> spyglassStand = Optional.empty();

    private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    @Override
    public void setSpyglassStand(Integer id) {
        this.spyglassStand = Optional.ofNullable(id);
    }

    @Unique
    @Override
    public void setSpyglassStandEntity(SpyglassStandEntity entity) {
        this.spyglassStand = Optional.of(entity).map(SpyglassStandEntity::getId);
    }

    @Unique
    @Override
    public boolean hasSpyglassStand() {
        return this.spyglassStand.isPresent();
    }

    @Unique
    @Override
    public Optional<Integer> getSpyglassStand() {
        return this.spyglassStand;
    }

    @Unique
    @Override
    public Optional<SpyglassStandEntity> getSpyglassStandEntity() {
        return this.getSpyglassStand()
                   .map(this.world::getEntityById)
                   .filter(SpyglassStandEntity.class::isInstance)
                   .map(SpyglassStandEntity.class::cast);
    }

    @Unique
    @Override
    public ItemStack getScopingStack() {
        return this.getSpyglassStandEntity()
                   .map(SpyglassStandEntity::getScopingStack)
                   .or(() -> Optional.ofNullable(this.isUsingItem() ? this.getActiveItem() : null))
                   .filter(stack -> stack.getItem() instanceof ISpyglass)
                   .orElse(ItemStack.EMPTY);
    }

    @Unique
    @Override
    public boolean isScoping() {
        return this.isUsingSpyglass();
    }

    /**
     * Adds spyglass stands and  the tag {@link SpyglassPlusItemTags#SCOPING_ITEMS} as valid spyglass states.
     */
    @Inject(method = "isUsingSpyglass", at = @At("RETURN"), cancellable = true)
    private void onIsUsingSpyglass(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && !this.getScopingStack().isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}
