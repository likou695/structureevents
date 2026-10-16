package com.xdww.structureevents.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xdww.structureevents.StructureEvents;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EventDefinitionLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Type LIST_TYPE = new TypeToken<List<StructureEventDefinition>>() {}.getType();
    private static final Map<String, StructureEventDefinition> BY_ID = new LinkedHashMap<>();

    private EventDefinitionLoader() {
    }

    public static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve("structureevents-events.json");
    }

    public static void load() {
        Path file = configFile();
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                writeDefaults(file);
            }
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                List<StructureEventDefinition> list = GSON.fromJson(reader, LIST_TYPE);
                BY_ID.clear();
                if (list != null) {
                    for (StructureEventDefinition def : list) {
                        if (def != null && def.structureId != null && !def.structureId.isBlank()) {
                            BY_ID.put(def.structureId, def);
                        }
                    }
                }
            }
            StructureEvents.LOGGER.info("Loaded {} structure event definitions from {}", BY_ID.size(), file);
        } catch (Exception e) {
            StructureEvents.LOGGER.error("Failed to load structure event definitions", e);
        }
    }

    public static StructureEventDefinition get(String structureId) {
        return BY_ID.get(structureId);
    }

    public static StructureEventDefinition getOrFallback(String structureId) {
        StructureEventDefinition existing = BY_ID.get(structureId);
        if (existing != null) {
            return existing;
        }
        StructureEventDefinition fallback = new StructureEventDefinition();
        fallback.structureId = structureId;
        fallback.enabled = true;
        fallback.title = structureId;
        fallback.subtitle = "";
        fallback.actionbar = "进入结构：" + structureId;
        return fallback;
    }

    public static Map<String, StructureEventDefinition> all() {
        return BY_ID;
    }

    public static List<String> watchedIds() {
        List<String> ids = new ArrayList<>();
        for (StructureEventDefinition def : BY_ID.values()) {
            if (def.enabled) {
                ids.add(def.structureId);
            }
        }
        return ids;
    }

    private static void writeDefaults(Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(DefaultEvents.create(), writer);
        }
    }
}
