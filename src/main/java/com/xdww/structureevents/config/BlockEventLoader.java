package com.xdww.structureevents.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xdww.structureevents.StructureEvents;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BlockEventLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Type LIST_TYPE = new TypeToken<List<BlockEventDefinition>>() {}.getType();
    private static final List<BlockEventDefinition> ENTRIES = new ArrayList<>();

    private BlockEventLoader() {
    }

    public static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve("structureevents-blocks.json");
    }

    public static void load() {
        Path file = configFile();
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    GSON.toJson(DefaultBlockEvents.create(), writer);
                }
            }
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                List<BlockEventDefinition> list = GSON.fromJson(reader, LIST_TYPE);
                ENTRIES.clear();
                if (list != null) {
                    for (BlockEventDefinition def : list) {
                        if (def != null && def.blockId != null && !def.blockId.isBlank()) {
                            ENTRIES.add(def);
                        }
                    }
                }
            }
            StructureEvents.LOGGER.info("Loaded {} block event definitions from {}", ENTRIES.size(), file);
        } catch (Exception e) {
            StructureEvents.LOGGER.error("Failed to load block event definitions", e);
        }
    }

    public static List<BlockEventDefinition> all() {
        return ENTRIES;
    }

    public static BlockEventDefinition find(String blockId) {
        for (BlockEventDefinition def : ENTRIES) {
            if (def.enabled && def.blockId.equals(blockId)) {
                return def;
            }
        }
        return null;
    }
}
