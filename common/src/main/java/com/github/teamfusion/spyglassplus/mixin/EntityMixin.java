package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.github.teamfusion.spyglassplus.util.CommonPlayerLookup;
import com.github.teamfusion.spyglassplus.world.SpyglassRaycasting;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Collections;

import static com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking.COMMAND_UPDATE_PACKET_ID;
import static com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking.INDICATE_UPDATE_PACKET_ID;
import static com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking.isCommandAllyTo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;

    @Unique private int lastIndicateEntityId = -1;

    /**
     * Tracks and updates entities this is indicating.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void tickIndicate(CallbackInfo ci) {
        if (this.world.isClient) {
            return;
        }

        Entity that = (Entity) (Object) this;
        if (that instanceof ScopingEntity scoping && !(that instanceof SpyglassStandEntity)) {
            if (scoping.isScoping()) {
                ItemStack stack = scoping.getScopingStack();
                if (stack.hasEnchantments()) {
                    if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.INDICATE.get(), stack) > 0) {
                        Entity entity = SpyglassRaycasting.raycast(that);
                        this.sendIndicateUpdate(entity, CommonPlayerLookup.tracking(that));
                    }

                    if (that instanceof ServerPlayerEntity player) {
                        if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.COMMAND.get(), stack) > 0) {
                            Entity entity = SpyglassRaycasting.raycast(player, entityx -> entityx instanceof MobEntity mobEntity && !isCommandAllyTo(player, mobEntity));
                            this.sendCommandUpdate(entity, player);
                        }
                    }
                }
            } else {
                if (this.lastIndicateEntityId != -1) {
                    this.sendIndicateUpdate(CommonPlayerLookup.tracking(that), -1);
                }
            }
        }
    }

    @Unique
    private void sendCommandUpdate(@Nullable Entity entity, ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(entity == null ? -1 : entity.getId());
        NetworkManager.sendToPlayer(player, COMMAND_UPDATE_PACKET_ID, buf);
    }

    /**
     * Sends indicated entity to new trackers.
     */
    @Inject(method = "onStartedTrackingBy", at = @At("TAIL"))
    private void onOnStartedTrackingBy(ServerPlayerEntity player, CallbackInfo ci) {
        this.sendIndicateUpdate(Collections.singleton(player), this.lastIndicateEntityId);
    }

    /**
     * Removes indicated entity from old trackers.
     */
    @Inject(method = "onStoppedTrackingBy", at = @At("TAIL"))
    private void onOnStoppedTrackingBy(ServerPlayerEntity player, CallbackInfo ci) {
        this.sendIndicateUpdate(Collections.singleton(player), -1);
    }

    @Unique
    private int sendIndicateUpdate(@Nullable Entity entity, Collection<ServerPlayerEntity> tracking) {
        int id = entity == null ? -1 : entity.getId();
        if (this.lastIndicateEntityId != id) {
            this.sendIndicateUpdate(tracking, id);
        }
        return id;
    }

    @Unique
    private void sendIndicateUpdate(Collection<ServerPlayerEntity> tracking, int id) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(this.lastIndicateEntityId);
        buf.writeInt(id);
        NetworkManager.sendToPlayers(tracking, INDICATE_UPDATE_PACKET_ID, buf);

        this.lastIndicateEntityId = id;
    }
}
