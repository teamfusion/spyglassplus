package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.enchantment.DiscoveryEnchantment;
import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.util.CommonPlayerLookup;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static com.github.teamfusion.spyglassplus.item.ISpyglass.EFFECTS_KEY;
import static com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking.EFFECTS_UPDATE_PACKET_ID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    private LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Prevents movement input when scoping in spyglass stand.
     */
    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    private void onIsImmobile(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity that = (LivingEntity) (Object) this;
        if (that instanceof ScopingPlayer scopingPlayer && scopingPlayer.hasSpyglassStand()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Sends effects to the client player each tick.
     */
    @Inject(method = "tickStatusEffects", at = @At("TAIL"))
    private void onTickStatusEffects(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            NbtCompound nbt = new NbtCompound();

            NbtList nbtEffects = new NbtList();
            this.activeStatusEffects.values().forEach(effect -> nbtEffects.add(effect.writeNbt(new NbtCompound())));
            nbt.put(EFFECTS_KEY, nbtEffects);

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(this.getId());
            buf.writeNbt(nbt);

            NetworkManager.sendToPlayers(
                CommonPlayerLookup.tracking(this)
                                  .stream()
                                  .filter(player -> DiscoveryEnchantment.getLevel(player.getActiveItem()) > 0)
                                  .toList(),
                EFFECTS_UPDATE_PACKET_ID, buf
            );
        }
    }
}
