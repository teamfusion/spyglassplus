package com.github.teamfusion.spyglassplus.client.network;

import com.github.teamfusion.spyglassplus.client.entity.LivingEntityClientAccess;
import com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.teamfusion.spyglassplus.item.ISpyglass.*;

@Environment(EnvType.CLIENT)
public interface SpyglassPlusClientNetworking extends SpyglassPlusNetworking {
    static void registerReceivers() {
        NetworkManager.registerReceiver(Side.S2C, EFFECTS_UPDATE_PACKET_ID, SpyglassPlusClientNetworking::onDiscoveryEffectsUpdate);
    }

    static void onDiscoveryEffectsUpdate(PacketByteBuf buf, NetworkManager.PacketContext context) {
        int id = buf.readInt();
        NbtCompound nbt = buf.readNbt();
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.world.getEntityById(id) instanceof LivingEntity livingEntity) {
                NbtList nbtEffects = nbt.getList(EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
                List<StatusEffectInstance> list = new ArrayList<>();
                nbtEffects.stream()
                          .filter(NbtCompound.class::isInstance)
                          .map(NbtCompound.class::cast)
                          .map(StatusEffectInstance::fromNbt)
                          .forEach(list::add);
                ((LivingEntityClientAccess) livingEntity).setEffects(Collections.unmodifiableList(list));
            }
        });
    }
}
