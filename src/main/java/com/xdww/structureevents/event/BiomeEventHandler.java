package com.xdww.structureevents.event;

import com.xdww.structureevents.config.BiomeEventDefinition;
import com.xdww.structureevents.config.BiomeEventLoader;
import com.xdww.structureevents.config.ModConfig;
import com.xdww.structureevents.util.SpawnHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.advancements.Advancement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BiomeEventHandler {
    private static final Map<UUID, Map<String, Long>> LAST_SPAWN = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Long>> LAST_EFFECT = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_SPAWN.remove(id);
        LAST_EFFECT.remove(id);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        if (!ModConfig.ENABLED.get() || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        Holder<Biome> holder = level.getBiome(player.blockPosition());
        ResourceLocation biomeId = holder.unwrapKey().map(ResourceKey::location).orElse(null);
        if (biomeId == null) {
            return;
        }

        for (BiomeEventDefinition def : BiomeEventLoader.all()) {
            if (def == null || !def.enabled || !matches(holder, biomeId, def.biomeId)) {
                continue;
            }
            boolean exempt = isExempt(player, def);
            if (ModConfig.APPLY_EFFECTS.get() && def.effects != null && !exempt) {
                for (BiomeEventDefinition.EffectSpec spec : def.effects) {
                    tryApplyEffect(player, def.biomeId, spec);
                }
            }
            if (ModConfig.SPAWN_MOBS.get() && def.spawns != null && !exempt) {
                for (BiomeEventDefinition.SpawnSpec spawn : def.spawns) {
                    trySpawn(player, level, def.biomeId, spawn);
                }
            }
        }
    }

    private static boolean isExempt(ServerPlayer player, BiomeEventDefinition def) {
        if (hasExemptItem(player, def.exemptItems)) {
            return true;
        }
        return hasExemptAdvancement(player, def.exemptAdvancements);
    }

    private static boolean hasExemptItem(ServerPlayer player, List<String> items) {
        if (items == null) {
            return false;
        }
        for (String raw : items) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(raw.trim());
            if (id == null) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && player.getInventory().contains(new ItemStack(item))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasExemptAdvancement(ServerPlayer player, List<String> advancements) {
        if (advancements == null) {
            return false;
        }
        for (String raw : advancements) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(raw.trim());
            if (id == null) {
                continue;
            }
            Advancement advancement = player.server.getAdvancements().getAdvancement(id);
            if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(Holder<Biome> holder, ResourceLocation biomeId, String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return false;
        }
        String raw = pattern.trim();
        if (raw.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(raw.substring(1));
            if (tagId == null) {
                return false;
            }
            return holder.is(TagKey.create(Registries.BIOME, tagId));
        }
        String id = biomeId.toString();
        return id.equals(raw) || id.startsWith(raw + "_");
    }

    private static void tryApplyEffect(ServerPlayer player, String biomeId, BiomeEventDefinition.EffectSpec spec) {
        if (spec == null || spec.effectId == null || spec.effectId.isBlank() || spec.seconds <= 0) {
            return;
        }
        String key = biomeId + "|" + spec.effectId;
        long now = System.currentTimeMillis();
        Map<String, Long> times = LAST_EFFECT.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>());
        long last = times.getOrDefault(key, 0L);
        int interval = Math.max(1, spec.intervalSeconds);
        if (now - last < interval * 1000L) {
            return;
        }
        times.put(key, now);
        applyEffect(player, spec);
    }

    private static void applyEffect(ServerPlayer player, BiomeEventDefinition.EffectSpec spec) {
        if (spec == null || spec.effectId == null || spec.effectId.isBlank() || spec.seconds <= 0) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(spec.effectId);
        if (id == null) {
            return;
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, spec.seconds * 20, Math.max(0, spec.amplifier), spec.ambient, spec.particles));
    }

    private static void trySpawn(ServerPlayer player, ServerLevel level, String biomeId, BiomeEventDefinition.SpawnSpec spawn) {
        if (spawn == null || spawn.entityId == null || spawn.count <= 0 || spawn.chance <= 0) {
            return;
        }
        String key = biomeId + "|" + spawn.entityId;
        long now = System.currentTimeMillis();
        Map<String, Long> playerTimes = LAST_SPAWN.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>());
        long last = playerTimes.getOrDefault(key, 0L);
        int interval = Math.max(1, spawn.intervalSeconds);
        if (now - last < interval * 1000L) {
            return;
        }
        playerTimes.put(key, now);
        if (level.random.nextDouble() > spawn.chance) {
            return;
        }
        int min = Math.max(4, spawn.minDistance);
        int max = Math.max(min, spawn.maxDistance);
        SpawnHelper.spawnAtDistance(level, player, spawn.entityId, spawn.count, min, max);
    }
}
