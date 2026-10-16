package com.xdww.structureevents.util;

import com.xdww.structureevents.config.EventDefinitionLoader;
import com.xdww.structureevents.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StructureFinder {
    private StructureFinder() {
    }

    public record Found(ResourceLocation id, Structure structure, StructureStart start) {
        public long instanceKey() {
            return start.getChunkPos().toLong();
        }
    }

    public static Optional<Found> findFirst(ServerLevel level, BlockPos pos, List<String> watchedIds) {
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var manager = level.structureManager();
        for (String raw : watchedIds) {
            ResourceLocation id = ResourceLocation.tryParse(raw);
            if (id == null) {
                continue;
            }
            Structure structure = registry.get(id);
            if (structure == null) {
                continue;
            }
            StructureStart start = manager.getStructureWithPieceAt(pos, structure);
            if (start != null && start.isValid()) {
                return Optional.of(new Found(id, structure, start));
            }
        }
        return Optional.empty();
    }

    public static List<Found> findAllWatched(ServerLevel level, BlockPos pos, List<String> watchedIds) {
        List<Found> found = new ArrayList<>();
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var manager = level.structureManager();
        for (String raw : watchedIds) {
            ResourceLocation id = ResourceLocation.tryParse(raw);
            if (id == null) {
                continue;
            }
            Structure structure = registry.get(id);
            if (structure == null) {
                continue;
            }
            StructureStart start = manager.getStructureWithPieceAt(pos, structure);
            if (start != null && start.isValid()) {
                found.add(new Found(id, structure, start));
            }
        }
        return found;
    }

    public static List<String> listAllAt(ServerLevel level, BlockPos pos) {
        List<String> names = new ArrayList<>();
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var manager = level.structureManager();
        manager.getAllStructuresAt(pos).forEach((structure, refs) -> {
            StructureStart start = manager.getStructureWithPieceAt(pos, structure);
            if (start != null && start.isValid()) {
                ResourceLocation key = registry.getKey(structure);
                if (key != null) {
                    names.add(key.toString());
                }
            }
        });
        return names;
    }

    public static List<ResourceLocation> listRegistered(ServerLevel level, String namespaceOrNull) {
        List<ResourceLocation> ids = new ArrayList<>();
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (ResourceLocation id : registry.keySet()) {
            if (namespaceOrNull == null || namespaceOrNull.isBlank() || namespaceOrNull.equals(id.getNamespace())) {
                ids.add(id);
            }
        }
        ids.sort(java.util.Comparator.comparing(ResourceLocation::toString));
        return ids;
    }

    public static List<String> resolveWatched(ServerLevel level) {
        var patterns = ModConfig.DETECT_STRUCTURES.get();
        if (StructurePatterns.listEmpty(patterns)) {
            return EventDefinitionLoader.watchedIds();
        }
        List<String> resolved = new ArrayList<>();
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (ResourceLocation id : registry.keySet()) {
            if (StructurePatterns.matches(id.toString(), registry, patterns)) {
                resolved.add(id.toString());
            }
        }
        return resolved;
    }
}
