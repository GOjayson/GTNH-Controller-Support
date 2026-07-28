package dev.gtnhcontroller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = GTNHController.MODID,
    version = Tags.VERSION,
    name = GTNHController.NAME,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:lwjgl3ify@[3.0.0,)")
public final class GTNHController {

    public static final String MODID = "gtnhcontroller";
    public static final String NAME = "GTNH Controller Support";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "dev.gtnhcontroller.ClientProxy", serverSide = "dev.gtnhcontroller.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
