package com.xdww.structureevents.config;

import java.util.ArrayList;
import java.util.List;

public class BlockEventDefinition {
    public String blockId = "";
    public boolean enabled = true;
    public double chance = 0.1;
    public String entityId = "minecraft:zombie";
    public int count = 1;
    public int radius = 6;
    public int cooldownSeconds = 8;
    public List<BiomeEventDefinition.EffectSpec> effects = new ArrayList<>();
}
