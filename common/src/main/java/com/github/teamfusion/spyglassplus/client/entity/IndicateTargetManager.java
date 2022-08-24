package com.github.teamfusion.spyglassplus.client.entity;

import com.github.teamfusion.spyglassplus.client.network.SpyglassPlusClientNetworking;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

/**
 * Manages what entities are being watched by any player for {@link SpyglassPlusEnchantments#INDICATE}.
 */
@Environment(EnvType.CLIENT)
public class IndicateTargetManager {
    private final Int2ObjectMap<Integer> watchers;

    public IndicateTargetManager() {
        this.watchers = new Int2ObjectArrayMap<>();
    }

    public void reset() {
        this.watchers.clear();
    }

    /**
     * @return whether an entity has any {@link SpyglassPlusEnchantments#INDICATE} watchers.
     */
    public boolean isIndicated(Entity entity) {
        return this.watchers.getOrDefault(entity.getId(), (Integer) 0) > 0;
    }

    /**
     * Increments watchers.
     *
     * @see SpyglassPlusClientNetworking#onIndicateUpdate(PacketByteBuf, NetworkManager.PacketContext)
     */
    public void increment(int id) {
        if (id != -1) {
            this.watchers.compute(id, (key, value) -> (value == null ? 0 : value) + 1);
        }
    }

    /**
     * Decrements watchers.
     *
     * @see SpyglassPlusClientNetworking#onIndicateUpdate(PacketByteBuf, NetworkManager.PacketContext)
     */
    public void decrement(int id) {
        if (id != -1) {
            this.watchers.compute(id, (key, value) -> Math.max(0, (value == null ? 0 : value) - 1));
        }
    }
}
