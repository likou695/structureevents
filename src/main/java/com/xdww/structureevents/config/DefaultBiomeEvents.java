package com.xdww.structureevents.config;

import java.util.ArrayList;
import java.util.List;

public final class DefaultBiomeEvents {
    private DefaultBiomeEvents() {
    }

    public static List<BiomeEventDefinition> create() {
        List<BiomeEventDefinition> list = new ArrayList<>();

        BiomeEventDefinition darkForest = new BiomeEventDefinition();
        darkForest.biomeId = "minecraft:dark_forest";
        darkForest.enabled = true;
        BiomeEventDefinition.EffectSpec darkness = new BiomeEventDefinition.EffectSpec();
        darkness.effectId = "minecraft:darkness";
        darkness.seconds = 12;
        darkness.intervalSeconds = 8;
        darkForest.effects.add(darkness);
        BiomeEventDefinition.SpawnSpec zombie = new BiomeEventDefinition.SpawnSpec();
        zombie.entityId = "minecraft:zombie";
        zombie.count = 1;
        zombie.chance = 0.2;
        zombie.intervalSeconds = 10;
        zombie.minDistance = 12;
        zombie.maxDistance = 22;
        darkForest.spawns.add(zombie);
        darkForest.exemptItems.add("minecraft:torch");
        darkForest.exemptAdvancements.add("minecraft:adventure/kill_a_mob");
        list.add(darkForest);

        BiomeEventDefinition deepDark = new BiomeEventDefinition();
        deepDark.biomeId = "minecraft:deep_dark";
        deepDark.enabled = false;
        BiomeEventDefinition.EffectSpec deepDarkness = new BiomeEventDefinition.EffectSpec();
        deepDarkness.effectId = "minecraft:darkness";
        deepDarkness.seconds = 16;
        deepDark.effects.add(deepDarkness);
        list.add(deepDark);

        return list;
    }
}
