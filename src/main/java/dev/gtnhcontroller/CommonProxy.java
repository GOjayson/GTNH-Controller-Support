package dev.gtnhcontroller;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronize(event.getSuggestedConfigurationFile());
        GTNHController.LOG.info("{} {} loading", GTNHController.NAME, Tags.VERSION);
    }

    public void init(FMLInitializationEvent event) {}
}
