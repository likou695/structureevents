package com.xdww.structureevents.config;

import java.util.ArrayList;
import java.util.List;

public class BiomeEventDefinition {
    public String biomeId = "";
    public boolean enabled = true;
    public List<EffectSpec> effects = new ArrayList<>();
    public List<SpawnSpec> spawns = new ArrayList<>();
    public List<String> exemptItems = new ArrayList<>();
    public List<String> exemptAdvancements = new ArrayList<>();

    public static class EffectSpec {
        public String effectId = "minecraft:darkness";
        public int seconds = 12;
        public int amplifier = 0;
        public boolean ambient = true;
        public boolean particles = false;
        public int intervalSeconds = 1;
    }

    public static class SpawnSpec {
        public String entityId = "minecraft:zombie";
        public int count = 1;
        public double chance = 0.15;
        public int intervalSeconds = 8;
        public int minDistance = 12;
        public int maxDistance = 24;
    }
}
