package com.xdww.structureevents.config;

import java.util.ArrayList;
import java.util.List;

public final class DefaultBlockEvents {
    private DefaultBlockEvents() {
    }

    public static List<BlockEventDefinition> create() {
        List<BlockEventDefinition> list = new ArrayList<>();
        BlockEventDefinition crafting = new BlockEventDefinition();
        crafting.blockId = "minecraft:crafting_table";
        crafting.enabled = false;
        crafting.chance = 0.08;
        crafting.entityId = "minecraft:witch";
        crafting.count = 1;
        crafting.radius = 5;
        crafting.cooldownSeconds = 12;
        list.add(crafting);
        return list;
    }
}
