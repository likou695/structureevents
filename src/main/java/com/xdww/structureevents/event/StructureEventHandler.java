package com.xdww.structureevents.event;

import com.xdww.structureevents.StructureEvents;
import com.xdww.structureevents.config.EventDefinitionLoader;
import com.xdww.structureevents.config.ModConfig;
import com.xdww.structureevents.config.StructureEventDefinition;
import com.xdww.structureevents.util.StructureFinder;
import com.xdww.structureevents.util.StructurePatterns;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StructureEventHandler {
    private static final Map<UUID, VisitState> LAST = new ConcurrentHashMap<>();
    private static final Map<UUID, List<ResourceLocation>> APPLIED_EFFECTS = new ConcurrentHashMap<>();

    private record VisitState(String insideId, long insideInstance, String lastId, long lastInstance, long lastTriggerMs) {
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST.remove(id);
        APPLIED_EFFECTS.remove(id);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player.level().isClientSide) {
            return;
        }
        if (!ModConfig.ENABLED.get()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % Math.max(1, ModConfig.CHECK_INTERVAL_TICKS.get()) != 0) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        var watched = StructureFinder.resolveWatched(level);
        if (watched.isEmpty()) {
            return;
        }

        var found = StructureFinder.findFirst(level, player.blockPosition(), watched);
        VisitState prev = LAST.get(player.getUUID());

        if (found.isEmpty()) {
            if (prev != null && !prev.insideId.isEmpty()) {
                onLeave(player, level, prev.insideId);
                LAST.put(player.getUUID(), new VisitState("", 0L, prev.lastId, prev.lastInstance, prev.lastTriggerMs));
            }
            return;
        }

        StructureFinder.Found current = found.get();
        String currentId = current.id().toString();
        long currentInstance = current.instanceKey();

        boolean alreadyInsideSame = prev != null
                && currentId.equals(prev.insideId)
                && currentInstance == prev.insideInstance;
        if (alreadyInsideSame) {
            return;
        }

        if (prev != null && !prev.insideId.isEmpty()) {
            onLeave(player, level, prev.insideId);
        }

        StructureEventDefinition def = EventDefinitionLoader.getOrFallback(currentId);
        if (def != null && !def.enabled && StructurePatterns.listEmpty(ModConfig.DETECT_STRUCTURES.get())) {
            LAST.put(player.getUUID(), new VisitState(currentId, currentInstance,
                    prev == null ? "" : prev.lastId,
                    prev == null ? 0L : prev.lastInstance,
                    prev == null ? 0L : prev.lastTriggerMs));
            return;
        }

        boolean sameInstanceAsLastTrigger = prev != null
                && currentId.equals(prev.lastId)
                && currentInstance == prev.lastInstance;

        if (!ModConfig.TRIGGER_ON_REENTER.get() && sameInstanceAsLastTrigger && prev.lastTriggerMs > 0) {
            LAST.put(player.getUUID(), new VisitState(currentId, currentInstance, prev.lastId, prev.lastInstance, prev.lastTriggerMs));
            return;
        }

        int cooldownSec = def.cooldownSeconds >= 0 ? def.cooldownSeconds : ModConfig.DEFAULT_COOLDOWN_SECONDS.get();
        if (sameInstanceAsLastTrigger && System.currentTimeMillis() - prev.lastTriggerMs < cooldownSec * 1000L) {
            LAST.put(player.getUUID(), new VisitState(currentId, currentInstance, prev.lastId, prev.lastInstance, prev.lastTriggerMs));
            return;
        }

        if (ModConfig.DEBUG_LOG.get()) {
            StructureEvents.LOGGER.info("{} entered {} at {}", player.getGameProfile().getName(), currentId, player.blockPosition());
        }

        fire(player, level, def, currentId);
        LAST.put(player.getUUID(), new VisitState(currentId, currentInstance, currentId, currentInstance, System.currentTimeMillis()));
    }

    private static void onLeave(ServerPlayer player, ServerLevel level, String structureId) {
        if (!ModConfig.APPLY_EFFECTS.get()) {
            APPLIED_EFFECTS.remove(player.getUUID());
            return;
        }
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        if (!StructurePatterns.matches(structureId, registry, ModConfig.LEAVE_CLEAR_STRUCTURES.get())) {
            return;
        }
        List<ResourceLocation> applied = APPLIED_EFFECTS.remove(player.getUUID());
        if (applied == null) {
            return;
        }
        for (ResourceLocation effectId : applied) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
            if (effect != null) {
                player.removeEffect(effect);
            }
        }
        if (ModConfig.DEBUG_LOG.get()) {
            StructureEvents.LOGGER.info("{} left {}, cleared {} effects", player.getGameProfile().getName(), structureId, applied.size());
        }
    }

    public static void fire(ServerPlayer player, ServerLevel level, StructureEventDefinition def, String structureId) {
        if (ModConfig.SHOW_TITLE.get() && (!def.title.isBlank() || !def.subtitle.isBlank())) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(def.titleFadeIn, def.titleStay, def.titleFadeOut));
            if (!def.title.isBlank()) {
                player.connection.send(new ClientboundSetTitleTextPacket(text(def.title)));
            }
            if (!def.subtitle.isBlank()) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(text(def.subtitle)));
            }
        }

        if (ModConfig.SHOW_ACTIONBAR.get() && !def.actionbar.isBlank()) {
            player.displayClientMessage(text(def.actionbar), true);
        }

        if (ModConfig.SHOW_CHAT_MESSAGE.get() && !def.chat.isBlank()) {
            player.sendSystemMessage(text(def.chat));
        }

        if (ModConfig.PLAY_SOUND.get() && def.sound != null && !def.sound.isBlank()) {
            ResourceLocation soundId = ResourceLocation.tryParse(def.sound);
            SoundEvent sound = soundId == null ? null : ForgeRegistries.SOUND_EVENTS.getValue(soundId);
            if (sound != null) {
                level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, def.soundVolume, def.soundPitch);
            }
        }

        if (shouldApplyEnterEffects(level, structureId) && def.effects != null) {
            List<ResourceLocation> applied = new ArrayList<>();
            for (StructureEventDefinition.EffectSpec spec : def.effects) {
                ResourceLocation effectId = applyEffect(player, spec);
                if (effectId != null) {
                    applied.add(effectId);
                }
            }
            if (!applied.isEmpty()) {
                APPLIED_EFFECTS.put(player.getUUID(), applied);
            } else {
                APPLIED_EFFECTS.remove(player.getUUID());
            }
        }

        if (ModConfig.SPAWN_MOBS.get() && def.mobs != null) {
            for (StructureEventDefinition.MobSpawn spawn : def.mobs) {
                spawnMobs(level, player, spawn);
            }
        }
    }

    private static boolean shouldApplyEnterEffects(ServerLevel level, String structureId) {
        if (!ModConfig.APPLY_EFFECTS.get()) {
            return false;
        }
        var patterns = ModConfig.ENTER_EFFECT_STRUCTURES.get();
        if (StructurePatterns.listEmpty(patterns)) {
            return true;
        }
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return StructurePatterns.matches(structureId, registry, patterns);
    }

    private static ResourceLocation applyEffect(ServerPlayer player, StructureEventDefinition.EffectSpec spec) {
        if (spec == null || spec.effectId == null || spec.effectId.isBlank() || spec.seconds <= 0) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(spec.effectId);
        if (id == null) {
            return null;
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) {
            return null;
        }
        player.addEffect(new MobEffectInstance(effect, spec.seconds * 20, Math.max(0, spec.amplifier), spec.ambient, spec.particles));
        return id;
    }

    private static void spawnMobs(ServerLevel level, ServerPlayer player, StructureEventDefinition.MobSpawn spawn) {
        if (spawn == null || spawn.count <= 0 || spawn.entityId == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(spawn.entityId);
        if (id == null) {
            return;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null) {
            return;
        }
        BlockPos origin = player.blockPosition();
        int spawned = 0;
        for (int i = 0; i < spawn.count * 6 && spawned < spawn.count; i++) {
            int dx = level.random.nextInt(spawn.radius * 2 + 1) - spawn.radius;
            int dz = level.random.nextInt(spawn.radius * 2 + 1) - spawn.radius;
            if (dx == 0 && dz == 0) {
                continue;
            }
            BlockPos pos = findStandable(level, origin.offset(dx, 0, dz));
            if (pos == null) {
                continue;
            }
            var entity = type.spawn(level, pos, MobSpawnType.EVENT);
            if (entity != null) {
                if (entity instanceof Mob mob) {
                    if (spawn.persist) {
                        mob.setPersistenceRequired();
                    }
                    mob.setTarget(player);
                }
                spawned++;
            }
        }
    }

    private static BlockPos findStandable(Level level, BlockPos around) {
        for (int y = 2; y >= -3; y--) {
            BlockPos pos = around.offset(0, y, 0);
            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above(2)).isAir()) {
                return pos.above();
            }
        }
        return level.getBlockState(around).isAir() ? around : null;
    }

    private static Component text(String raw) {
        return Component.literal(raw == null ? "" : raw);
    }
}
