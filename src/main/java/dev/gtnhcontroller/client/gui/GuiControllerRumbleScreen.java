package dev.gtnhcontroller.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.RumbleEffect;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

public final class GuiControllerRumbleScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int MASTER = 1;
    private static final int DAMAGE = 2;
    private static final int EXPLOSIONS = 3;
    private static final int MINING = 4;
    private static final int INTENSITY_DOWN = 5;
    private static final int INTENSITY_UP = 6;
    private static final int TEST = 7;
    private static final int FISHING = 8;
    private static final int LOW_HEALTH = 9;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private boolean lastConnected;
    private boolean lastRumbleSupport;

    public GuiControllerRumbleScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        buttonList.add(new GuiButton(MASTER, centerX - 100, 46, 200, 20, toggleLabel("Rumble", Config.rumbleEnabled)));
        buttonList.add(new GuiButton(DAMAGE, centerX - 155, 70, 150, 20, toggleLabel("Damage", Config.rumbleDamage)));
        buttonList.add(
            new GuiButton(EXPLOSIONS, centerX + 5, 70, 150, 20, toggleLabel("Explosions", Config.rumbleExplosions)));
        buttonList.add(new GuiButton(MINING, centerX - 155, 94, 150, 20, toggleLabel("Mining", Config.rumbleMining)));
        buttonList
            .add(new GuiButton(FISHING, centerX + 5, 94, 150, 20, toggleLabel("Fishing Bites", Config.rumbleFishing)));
        buttonList.add(
            new GuiButton(LOW_HEALTH, centerX - 155, 118, 150, 20, toggleLabel("Low Health", Config.rumbleLowHealth)));
        buttonList.add(new GuiButton(TEST, centerX + 5, 118, 150, 20, "Test Rumble"));
        buttonList.add(new GuiButton(INTENSITY_DOWN, centerX - 155, 142, 45, 20, "-"));
        buttonList.add(new GuiButton(INTENSITY_UP, centerX + 110, 142, 45, 20, "+"));
        buttonList.add(
            new GuiButton(
                -1,
                centerX - 105,
                142,
                210,
                20,
                "Intensity: " + Math.round(Config.rumbleIntensity * 100.0F) + "%"));
        buttonList.add(new GuiButton(DONE, centerX - 100, height - 28, 200, 20, "Done"));
        updateEnabledStates();
        lastConnected = gamepadManager.isConnected();
        lastRumbleSupport = gamepadManager.supportsRumble();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (lastConnected != gamepadManager.isConnected() || lastRumbleSupport != gamepadManager.supportsRumble()) {
            initGui();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case MASTER:
                Config.rumbleEnabled = !Config.rumbleEnabled;
                saveAndRefresh();
                break;
            case DAMAGE:
                Config.rumbleDamage = !Config.rumbleDamage;
                saveAndRefresh();
                break;
            case EXPLOSIONS:
                Config.rumbleExplosions = !Config.rumbleExplosions;
                saveAndRefresh();
                break;
            case MINING:
                Config.rumbleMining = !Config.rumbleMining;
                saveAndRefresh();
                break;
            case FISHING:
                Config.rumbleFishing = !Config.rumbleFishing;
                saveAndRefresh();
                break;
            case LOW_HEALTH:
                Config.rumbleLowHealth = !Config.rumbleLowHealth;
                saveAndRefresh();
                break;
            case INTENSITY_DOWN:
                Config.rumbleIntensity = Math.max(0.0F, Config.rumbleIntensity - 0.1F);
                saveAndRefresh();
                break;
            case INTENSITY_UP:
                Config.rumbleIntensity = Math.min(1.0F, Config.rumbleIntensity + 0.1F);
                saveAndRefresh();
                break;
            case TEST:
                gamepadManager
                    .playRumble(0.75F * Config.rumbleIntensity, 0.45F * Config.rumbleIntensity, 450, RumbleEffect.TEST);
                break;
            case DONE:
                mc.displayGuiScreen(parentScreen);
                break;
            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Rumble Feedback", width / 2, 14, 0xFFFFFF);
        String capability = !gamepadManager.isConnected() ? "Connect a controller to test rumble"
            : gamepadManager.supportsRumble() ? "Selected controller reports rumble support"
                : "Selected controller does not report rumble support";
        drawCenteredString(
            fontRendererObj,
            capability,
            width / 2,
            31,
            gamepadManager.supportsRumble() ? 0x80FF80 : 0xA0A0A0);
        drawCenteredString(
            fontRendererObj,
            "Effects are client-side and can be disabled independently.",
            width / 2,
            173,
            0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void saveAndRefresh() {
        Config.rumbleIntensity = Math.round(Config.rumbleIntensity * 10.0F) / 10.0F;
        Config.saveControllerSettings();
        if (!Config.rumbleEnabled || Config.rumbleIntensity <= 0.0F) {
            gamepadManager.stopRumble();
        }
        initGui();
    }

    private void updateEnabledStates() {
        for (Object entry : buttonList) {
            GuiButton button = (GuiButton) entry;
            if (button.id == DAMAGE || button.id == EXPLOSIONS
                || button.id == MINING
                || button.id == FISHING
                || button.id == LOW_HEALTH
                || button.id == INTENSITY_DOWN
                || button.id == INTENSITY_UP) {
                button.enabled = Config.rumbleEnabled;
            } else if (button.id == TEST) {
                button.enabled = Config.rumbleEnabled && Config.rumbleIntensity > 0.0F
                    && gamepadManager.supportsRumble();
            } else if (button.id == -1) {
                button.enabled = false;
            }
        }
    }

    private static String toggleLabel(String label, boolean enabled) {
        return label + ": " + (enabled ? "\u00A7aON" : "\u00A7cOFF");
    }
}
