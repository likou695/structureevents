package com.xdww.structureevents.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.xdww.structureevents.config.BiomeEventLoader;
import com.xdww.structureevents.config.BlockEventLoader;
import com.xdww.structureevents.config.EventDefinitionLoader;
import com.xdww.structureevents.config.StructureEventDefinition;
import com.xdww.structureevents.event.StructureEventHandler;
import com.xdww.structureevents.util.StructureFinder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class StructureEventsCommands {
    private StructureEventsCommands() {
    }

    private static final SuggestionProvider<CommandSourceStack> STRUCTURE_IDS = (ctx, builder) -> {
        var level = ctx.getSource().getLevel();
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return SharedSuggestionProvider.suggestResource(registry.keySet(), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("structureevents")
                .then(Commands.literal("check")
                        .executes(ctx -> check(ctx.getSource())))
                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx.getSource(), null))
                        .then(Commands.argument("namespace", StringArgumentType.word())
                                .executes(ctx -> list(ctx.getSource(), StringArgumentType.getString(ctx, "namespace")))))
                .then(Commands.literal("dump")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> dump(ctx.getSource())))
                .then(Commands.literal("reload")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> reload(ctx.getSource())))
                .then(Commands.literal("test")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("structureId", StringArgumentType.greedyString())
                                .suggests(STRUCTURE_IDS)
                                .executes(ctx -> test(ctx.getSource(), StringArgumentType.getString(ctx, "structureId")))))
        );
    }

    private static int check(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("该命令需要由玩家执行。"));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        var all = StructureFinder.listAllAt(level, player.blockPosition());
        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("当前位置没有检测到结构（或尚未生成结构件）。"), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("当前位置结构： " + String.join(", ", all)), false);
        return 1;
    }

    private static int list(CommandSourceStack source, String namespace) {
        ServerLevel level = source.getLevel();
        List<ResourceLocation> ids = StructureFinder.listRegistered(level, namespace);
        if (ids.isEmpty()) {
            source.sendFailure(Component.literal(namespace == null
                    ? "结构注册表是空的。"
                    : "命名空间 " + namespace + " 下没有结构。"));
            return 0;
        }
        int shown = 0;
        source.sendSuccess(() -> Component.literal("已注册结构 " + ids.size() + " 个"
                + (namespace == null ? "" : "（" + namespace + "）") + "："), false);
        for (ResourceLocation id : ids) {
            if (shown >= 80) {
                int remaining = ids.size() - shown;
                source.sendSuccess(() -> Component.literal("…还有 " + remaining + " 个，请用 /structureevents dump 导出全部，或加上命名空间过滤。"), false);
                break;
            }
            source.sendSuccess(() -> Component.literal("  " + id), false);
            shown++;
        }
        return ids.size();
    }

    private static int dump(CommandSourceStack source) {
        List<ResourceLocation> ids = StructureFinder.listRegistered(source.getLevel(), null);
        Path file = FMLPaths.GAMEDIR.get().resolve("structureevents-structure-ids.txt");
        try {
            String body = ids.stream().map(ResourceLocation::toString).collect(Collectors.joining("\n"));
            Files.writeString(file, body + "\n", StandardCharsets.UTF_8);
            source.sendSuccess(() -> Component.literal("已写出 " + ids.size() + " 个结构 ID 到 " + file.toAbsolutePath()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("写出失败：" + e.getMessage()));
            return 0;
        }
    }

    private static int reload(CommandSourceStack source) {
        EventDefinitionLoader.load();
        BiomeEventLoader.load();
        BlockEventLoader.load();
        source.sendSuccess(() -> Component.literal("已重新加载三份配置：结构 "
                + EventDefinitionLoader.all().size()
                + " 条，群系 " + BiomeEventLoader.all().size()
                + " 条，方块 " + BlockEventLoader.all().size() + " 条。"), true);
        return 1;
    }

    private static int test(CommandSourceStack source, String structureId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("该命令需要由玩家执行。"));
            return 0;
        }
        StructureEventDefinition def = EventDefinitionLoader.get(structureId);
        if (def == null) {
            source.sendFailure(Component.literal("找不到事件定义：" + structureId + "（先在 JSON 里加一条，或 /structureevents reload）"));
            return 0;
        }
        StructureEventHandler.fire(player, player.serverLevel(), def, structureId);
        source.sendSuccess(() -> Component.literal("已触发事件：" + structureId), true);
        return 1;
    }
}
