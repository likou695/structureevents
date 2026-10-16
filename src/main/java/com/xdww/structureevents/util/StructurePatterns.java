package com.xdww.structureevents.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public final class StructurePatterns {
    private StructurePatterns() {
    }

    public static boolean listEmpty(List<? extends String> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        for (String item : list) {
            if (item != null && !item.isBlank()) {
                return false;
            }
        }
        return true;
    }

    public static boolean matches(String structureId, Registry<Structure> registry, List<? extends String> patterns) {
        if (listEmpty(patterns)) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(structureId);
        if (id == null) {
            return false;
        }
        Structure structure = registry.get(id);
        for (String raw : patterns) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String pattern = raw.trim();
            if (pattern.startsWith("#")) {
                if (structure != null && matchesTag(registry, structure, pattern.substring(1))) {
                    return true;
                }
                continue;
            }
            if (id.toString().equals(pattern) || id.toString().startsWith(pattern + "_")) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesTag(Registry<Structure> registry, Structure structure, String tagPath) {
        ResourceLocation tagId = ResourceLocation.tryParse(tagPath);
        if (tagId == null) {
            return false;
        }
        TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, tagId);
        Holder<Structure> holder = registry.wrapAsHolder(structure);
        return holder.is(tag);
    }
}
