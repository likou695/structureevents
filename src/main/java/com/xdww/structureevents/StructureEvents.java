package com.xdww.structureevents;

import com.mojang.logging.LogUtils;
import com.xdww.structureevents.command.StructureEventsCommands;
import com.xdww.structureevents.config.BiomeEventLoader;
import com.xdww.structureevents.config.BlockEventLoader;
import com.xdww.structureevents.config.EventDefinitionLoader;
import com.xdww.structureevents.config.ModConfig;
import com.xdww.structureevents.event.BiomeEventHandler;
import com.xdww.structureevents.event.BlockEventHandler;
import com.xdww.structureevents.event.StructureEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(StructureEvents.MODID)
public class StructureEvents {
    public static final String MODID = "structureevents";
    public static final Logger LOGGER = LogUtils.getLogger();

    public StructureEvents() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
        modBus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(new StructureEventHandler());
        MinecraftForge.EVENT_BUS.register(new BiomeEventHandler());
        MinecraftForge.EVENT_BUS.register(new BlockEventHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Structure Events initialized for Forge 1.20.1");
    }

    private void onServerStarting(ServerStartingEvent event) {
        EventDefinitionLoader.load();
        BiomeEventLoader.load();
        BlockEventLoader.load();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        StructureEventsCommands.register(event.getDispatcher());
    }
}
