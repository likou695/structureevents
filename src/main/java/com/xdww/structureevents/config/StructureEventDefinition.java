package com.xdww.structureevents.config;

import java.util.ArrayList;
import java.util.List;

public class StructureEventDefinition {
    public String structureId = "";
    public boolean enabled = true;
    public String title = "";
    public String subtitle = "";
    public String actionbar = "";
    public String chat = "";
    public String sound = "";
    public float soundVolume = 1.0f;
    public float soundPitch = 1.0f;
    public int titleFadeIn = 10;
    public int titleStay = 70;
    public int titleFadeOut = 20;
    public int cooldownSeconds = -1;
    public List<MobSpawn> mobs = new ArrayList<>();
    public List<EffectSpec> effects = new ArrayList<>();

    public static class MobSpawn {
        public String entityId = "minecraft:zombie";
        public int count = 1;
        public int radius = 4;
        public boolean persist = false;
    }

    public static class EffectSpec {
        public String effectId = "minecraft:darkness";
        public int seconds = 8;
        public int amplifier = 0;
        public boolean ambient = true;
        public boolean particles = false;
    }
}
