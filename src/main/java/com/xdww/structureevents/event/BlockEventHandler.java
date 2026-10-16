package com.xdww.structureevents.event;

import com.xdww.structureevents.config.BlockEventDefinition;
import com.xdww.structureevents.config.BlockEventLoader;
import com.xdww.structureevents.config.ModConfig;
import com.xdww.structureevents.util.SpawnHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockEventHandler {
    private static final Map<UUID, Map<String, Long>> LAST_USE = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_USE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || !ModConfig.ENABLED.get() || !ModConfig.SPAWN_MOBS.get()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        var block = event.getLevel().getBlockState(event.getPos()).getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) {
            return;
        }
        BlockEventDefinition def = BlockEventLoader.find(blockId.toString());
        if (def == null || def.chance <= 0 || def.entityId == null || def.count <= 0) {
            return;
        }

        String key = blockId.toString();
        long now = System.currentTimeMillis();
        Map<String, Long> times = LAST_USE.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>());
        long last = times.getOrDefault(key, 0L);
        int cooldown = Math.max(0, def.cooldownSeconds);
        if (now - last < cooldown * 1000L) {
            return;
        }
        times.put(key, now);
        if (level.random.nextDouble() > def.chance) {
            return;
        }
        SpawnHelper.spawnAround(level, player, def.entityId, def.count, Math.max(2, def.radius));
    }
}
