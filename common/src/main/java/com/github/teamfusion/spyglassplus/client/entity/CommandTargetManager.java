package com.github.teamfusion.spyglassplus.client.entity;

import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the actively targeted entity for {@link SpyglassPlusEnchantments#COMMAND}.
 */
@Environment(EnvType.CLIENT)
public class CommandTargetManager {
    private final MinecraftClient client;

    private Entity entity;
    private Entity lastTargetedEntity;

    public CommandTargetManager() {
        this.client = MinecraftClient.getInstance();
    }

    public void setEntity(int id) {
        this.entity = id == -1 ? null : this.client.world.getEntityById(id);
    }

    public Entity setLastTargetedEntity(int id) {
        this.lastTargetedEntity = id == -1 ? null : this.client.world.getEntityById(id);
        return this.lastTargetedEntity;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    @Nullable
    public Entity getLastTargetedEntity() {
        return lastTargetedEntity;
    }
}
