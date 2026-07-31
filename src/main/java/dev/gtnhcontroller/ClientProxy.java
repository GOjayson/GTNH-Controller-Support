package dev.gtnhcontroller;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import dev.gtnhcontroller.client.ControllerDebugOverlay;
import dev.gtnhcontroller.client.gui.ControllerControlsEntry;
import dev.gtnhcontroller.client.gui.GuiController;
import dev.gtnhcontroller.client.gui.RadialMenuController;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.GameplayController;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.RumbleController;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class ClientProxy extends CommonProxy {

    private SdlGamepadManager gamepadManager;
    private ControllerProfile controllerProfile;
    private GameplayController gameplayController;
    private ModKeyBindingController modKeyBindingController;
    private RadialMenuController radialMenuController;
    private RumbleController rumbleController;
    private GuiController guiController;

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        gamepadManager = new SdlGamepadManager(Config.rescanIntervalTicks);
        controllerProfile = new ControllerProfile(gamepadManager);
        modKeyBindingController = new ModKeyBindingController(gamepadManager, controllerProfile);
        radialMenuController = new RadialMenuController(gamepadManager, controllerProfile, modKeyBindingController);
        gameplayController = new GameplayController(gamepadManager, controllerProfile);
        rumbleController = new RumbleController(gamepadManager);
        guiController = new GuiController(gamepadManager, controllerProfile);
        FMLCommonHandler.instance()
            .bus()
            .register(gamepadManager);
        FMLCommonHandler.instance()
            .bus()
            .register(controllerProfile);
        FMLCommonHandler.instance()
            .bus()
            .register(radialMenuController);
        FMLCommonHandler.instance()
            .bus()
            .register(gameplayController);
        FMLCommonHandler.instance()
            .bus()
            .register(modKeyBindingController);
        FMLCommonHandler.instance()
            .bus()
            .register(guiController);
        FMLCommonHandler.instance()
            .bus()
            .register(rumbleController);
        MinecraftForge.EVENT_BUS.register(new ControllerDebugOverlay(gamepadManager));
        MinecraftForge.EVENT_BUS.register(guiController);
        MinecraftForge.EVENT_BUS.register(rumbleController);
        MinecraftForge.EVENT_BUS
            .register(new ControllerControlsEntry(gamepadManager, controllerProfile, modKeyBindingController));

        GTNHController.LOG.info("SDL3 controller input registered");
    }
}
