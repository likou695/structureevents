package com.xdww.structureevents.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue CHECK_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue DEFAULT_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue TRIGGER_ON_REENTER;
    public static final ForgeConfigSpec.BooleanValue SHOW_CHAT_MESSAGE;
    public static final ForgeConfigSpec.BooleanValue SHOW_ACTIONBAR;
    public static final ForgeConfigSpec.BooleanValue SHOW_TITLE;
    public static final ForgeConfigSpec.BooleanValue SPAWN_MOBS;
    public static final ForgeConfigSpec.BooleanValue PLAY_SOUND;
    public static final ForgeConfigSpec.BooleanValue APPLY_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DETECT_STRUCTURES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTER_EFFECT_STRUCTURES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LEAVE_CLEAR_STRUCTURES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        ENABLED = builder.comment("Master switch for structure enter events.")
                .define("enabled", true);
        CHECK_INTERVAL_TICKS = builder.comment("How often to scan player position for structures. 10 = twice per second.")
                .defineInRange("checkIntervalTicks", 10, 1, 200);
        DEFAULT_COOLDOWN_SECONDS = builder.comment("Default cooldown before the same structure instance can fire again for a player.")
                .defineInRange("defaultCooldownSeconds", 180, 0, 86400);
        TRIGGER_ON_REENTER = builder.comment("If true, leaving and walking back in can trigger again after cooldown. If false, each structure instance only fires once per player session.")
                .define("triggerOnReenter", true);
        DEBUG_LOG = builder.comment("Log structure detections to the server console.")
                .define("debugLog", false);
        builder.pop();

        builder.push("structures");
        DETECT_STRUCTURES = builder.comment(
                        "Structures this mod should detect. Shown in the in-game Common config screen.",
                        "Empty = use enabled entries from structureevents-events.json (old behaviour).",
                        "Write one id per line, for example:",
                        "  minecraft:desert_pyramid",
                        "  minecraft:village          (matches village_plains, village_desert, ...)",
                        "  #minecraft:village         (structure tag)")
                .defineListAllowEmpty(List.of("detectStructures"), ArrayList::new, o -> o instanceof String);
        ENTER_EFFECT_STRUCTURES = builder.comment(
                        "Structures that may apply potion effects on enter.",
                        "Empty = no extra filter; JSON \"effects\" still apply as before.",
                        "Same id / prefix / tag format as detectStructures.")
                .defineListAllowEmpty(List.of("enterEffectStructures"), ArrayList::new, o -> o instanceof String);
        LEAVE_CLEAR_STRUCTURES = builder.comment(
                        "Structures that remove the potion effects this mod applied when the player leaves.",
                        "Empty = never clear on leave (old behaviour).",
                        "Same id / prefix / tag format as detectStructures.")
                .defineListAllowEmpty(List.of("leaveClearStructures"), ArrayList::new, o -> o instanceof String);
        builder.pop();

        builder.push("presentation");
        SHOW_TITLE = builder.comment("Show title + subtitle on screen when entering a structure.")
                .define("showTitle", true);
        SHOW_ACTIONBAR = builder.comment("Show actionbar text when entering a structure.")
                .define("showActionbar", true);
        SHOW_CHAT_MESSAGE = builder.comment("Send a chat message when entering a structure.")
                .define("showChatMessage", false);
        PLAY_SOUND = builder.comment("Play configured sounds.")
                .define("playSound", true);
        builder.pop();

        builder.push("gameplay");
        SPAWN_MOBS = builder.comment("Spawn configured mobs around the player.")
                .define("spawnMobs", true);
        APPLY_EFFECTS = builder.comment("Master switch for potion effects. Per-structure lists above still apply.")
                .define("applyEffects", true);
        builder.pop();

        SPEC = builder.build();
    }
}
