package com.xdww.structureevents.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class SpawnHelper {
    private SpawnHelper() {
    }

    public static void spawnAtDistance(ServerLevel level, ServerPlayer player, String entityId, int count, int minDistance, int maxDistance) {
        EntityType<?> type = type(entityId);
        if (type == null) {
            return;
        }
        int spawned = 0;
        for (int attempt = 0; attempt < count * 8 && spawned < count; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            int dist = minDistance + level.random.nextInt(maxDistance - minDistance + 1);
            int dx = (int) Math.round(Math.cos(angle) * dist);
            int dz = (int) Math.round(Math.sin(angle) * dist);
            if (dx == 0 && dz == 0) {
                continue;
            }
            BlockPos pos = findStandable(level, player.blockPosition().offset(dx, 0, dz));
            if (pos == null) {
                continue;
            }
            if (spawnOne(level, player, type, pos)) {
                spawned++;
            }
        }
    }

    public static void spawnAround(ServerLevel level, ServerPlayer player, String entityId, int count, int radius) {
        EntityType<?> type = type(entityId);
        if (type == null) {
            return;
        }
        int spawned = 0;
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < count * 8 && spawned < count; attempt++) {
            int dx = level.random.nextInt(radius * 2 + 1) - radius;
            int dz = level.random.nextInt(radius * 2 + 1) - radius;
            if (dx == 0 && dz == 0) {
                continue;
            }
            BlockPos pos = findStandable(level, origin.offset(dx, 0, dz));
            if (pos == null) {
                continue;
            }
            if (spawnOne(level, player, type, pos)) {
                spawned++;
            }
        }
    }

    private static boolean spawnOne(ServerLevel level, ServerPlayer player, EntityType<?> type, BlockPos pos) {
        var entity = type.spawn(level, pos, MobSpawnType.EVENT);
        if (entity == null) {
            return false;
        }
        if (entity instanceof Mob mob) {
            mob.setTarget(player);
        }
        return true;
    }

    private static EntityType<?> type(String entityId) {
        ResourceLocation id = ResourceLocation.tryParse(entityId);
        return id == null ? null : ForgeRegistries.ENTITY_TYPES.getValue(id);
    }

    private static BlockPos findStandable(Level level, BlockPos around) {
        for (int y = 3; y >= -4; y--) {
            BlockPos pos = around.offset(0, y, 0);
            if (!level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.above(2)).isAir()) {
                return pos.above();
            }
        }
        return level.getBlockState(around).isAir() ? around : null;
    }
}
