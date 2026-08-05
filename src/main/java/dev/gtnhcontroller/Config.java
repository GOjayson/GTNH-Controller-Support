package dev.gtnhcontroller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

import dev.gtnhcontroller.client.input.ActivationMode;
import dev.gtnhcontroller.client.input.ChatMacro;
import dev.gtnhcontroller.client.input.ChatMacroConfigCodec;
import dev.gtnhcontroller.client.input.ControllerAction;
import dev.gtnhcontroller.client.input.ControllerBindingLayer;
import dev.gtnhcontroller.client.input.ControllerSelection;
import dev.gtnhcontroller.client.input.ModKeyBindingConfigCodec;
import dev.gtnhcontroller.client.input.ModKeyBindingIdentifier;
import dev.gtnhcontroller.client.input.RadialMenuActivationMode;
import dev.gtnhcontroller.client.input.RadialMenuConfigCodec;
import dev.gtnhcontroller.client.input.RadialMenuPage;

public final class Config {

    private static final String VANILLA_DROP_IDENTIFIER = ModKeyBindingIdentifier
        .create("key.categories.gameplay", "key.drop", 1);
    private static final String LEGACY_GUI_KEYBOARD_DEFAULT = "BUTTON:NORTH";

    public static boolean showDebugOverlay = true;
    public static int rescanIntervalTicks = 40;
    public static String controllerSelection = ControllerSelection.AUTOMATIC;
    public static boolean enableGameplayControls = true;
    public static boolean autoJump = false;
    @Deprecated
    public static boolean autoSwim = false;
    public static ActivationMode swimActivationMode = ActivationMode.HOLD;
    public static ActivationMode sneakActivationMode = ActivationMode.HOLD;
    public static ActivationMode sprintActivationMode = ActivationMode.HOLD;
    public static ActivationMode attackActivationMode = ActivationMode.HOLD;
    public static ActivationMode useActivationMode = ActivationMode.HOLD;
    public static float moveSensitivity = 1.0F;
    public static float moveDeadZone = 0.18F;
    public static float moveCurveExponent = 1.0F;
    public static float lookSensitivity = 1.0F;
    public static float lookDeadZone = 0.15F;
    public static float lookCurveExponent = 1.6F;
    public static float lookSpeed = 120.0F;
    public static float triggerThreshold = 0.50F;
    public static boolean invertLookX = false;
    public static boolean invertLookY = false;
    public static boolean enableGuiControls = true;
    public static float cursorSensitivity = 1.0F;
    public static String cursorStick = "RIGHT";
    public static float cursorDeadZone = 0.15F;
    public static float cursorCurveExponent = 1.8F;
    public static float cursorSpeed = 420.0F;
    public static float cursorAcceleration = 1400.0F;
    public static float cursorDeceleration = 2400.0F;
    public static boolean invertCursorX = false;
    public static boolean invertCursorY = false;
    public static boolean enableButtonNavigation = true;
    public static boolean enableSlotNavigation = true;
    public static float precisionCursorScale = 0.30F;
    public static int navigationInitialDelayMillis = 350;
    public static int navigationRepeatIntervalMillis = 100;
    public static boolean scrollAccelerationEnabled = true;
    public static float scrollAccelerationMultiplier = 3.0F;
    public static boolean showControllerPrompts = true;
    public static boolean showActiveModeHud = true;
    public static boolean largeCursor = false;
    public static boolean cursorTrail = false;
    public static boolean rumbleEnabled = true;
    public static boolean rumbleDamage = true;
    public static boolean rumbleExplosions = true;
    public static boolean rumbleMining = true;
    public static boolean rumbleFishing = true;
    public static boolean rumbleLowHealth = true;
    public static float rumbleIntensity = 1.0F;
    public static RadialMenuActivationMode radialMenuActivationMode = RadialMenuActivationMode.HOLD;

    public static String jumpBinding = "BUTTON:SOUTH";
    public static String sneakBinding = "BUTTON:EAST";
    public static String sprintBinding = "BUTTON:LEFT_STICK";
    public static String attackBinding = "TRIGGER:RIGHT_TRIGGER";
    public static String useBinding = "TRIGGER:LEFT_TRIGGER";
    public static String dropItemBinding = "NONE";
    public static String hotbarPreviousBinding = "BUTTON:LEFT_SHOULDER|BUTTON:DPAD_LEFT";
    public static String hotbarNextBinding = "BUTTON:RIGHT_SHOULDER|BUTTON:DPAD_RIGHT";
    public static String openInventoryBinding = "BUTTON:NORTH";
    public static String pauseBinding = "BUTTON:START";
    public static String radialMenuBinding = "BUTTON:BACK";
    public static String modifierLayerBinding = "NONE";
    public static String guiConfirmBinding = "BUTTON:SOUTH";
    public static String guiQuickMoveBinding = "BUTTON:NORTH";
    public static String guiAlternateBinding = "BUTTON:WEST";
    public static String guiBackBinding = "BUTTON:EAST";
    public static String guiKeyboardBinding = "BUTTON:BACK";
    public static String guiScrollUpBinding = "BUTTON:LEFT_SHOULDER";
    public static String guiScrollDownBinding = "BUTTON:RIGHT_SHOULDER";
    public static String guiNavigateUpBinding = "BUTTON:DPAD_UP";
    public static String guiNavigateDownBinding = "BUTTON:DPAD_DOWN";
    public static String guiNavigateLeftBinding = "BUTTON:DPAD_LEFT";
    public static String guiNavigateRightBinding = "BUTTON:DPAD_RIGHT";
    public static String guiPrecisionBinding = "BUTTON:RIGHT_STICK";

    private static final Map<String, String> modKeyBindings = new LinkedHashMap<String, String>();
    private static final Map<String, String> modifierModKeyBindings = new LinkedHashMap<String, String>();
    private static final Map<ControllerAction, String> modifierBindings = new EnumMap<ControllerAction, String>(
        ControllerAction.class);
    private static final Map<RadialMenuPage, String[]> radialMenuEntries = new EnumMap<RadialMenuPage, String[]>(
        RadialMenuPage.class);
    private static final Map<String, ChatMacro> chatMacros = new LinkedHashMap<String, ChatMacro>();
    private static File configFile;

    static {
        for (RadialMenuPage page : RadialMenuPage.values()) {
            radialMenuEntries.put(page, new String[RadialMenuConfigCodec.SLOT_COUNT]);
        }
    }

    private Config() {}

    public static void synchronize(File suggestedConfigFile) {
        configFile = suggestedConfigFile;
        Configuration configuration = new Configuration(suggestedConfigFile);
        boolean hadQuickMoveBinding = configuration.hasKey("bindings", ControllerAction.GUI_QUICK_MOVE.configKey);

        showDebugOverlay = configuration.getBoolean(
            "showDebugOverlay",
            "debug",
            showDebugOverlay,
            "Append live controller name, axes, and pressed buttons to Minecraft's F3 debug screen.");
        rescanIntervalTicks = configuration.getInt(
            "rescanIntervalTicks",
            "controller",
            rescanIntervalTicks,
            20,
            1200,
            "How often to scan for a controller while none is connected. 20 ticks is approximately one second.");
        controllerSelection = configuration.getString(
            "selected",
            "controller",
            controllerSelection,
            "Controller selected in-game. AUTO uses the first available SDL gamepad.");
        if (ControllerSelection.isAutomatic(controllerSelection)) {
            controllerSelection = ControllerSelection.AUTOMATIC;
        } else if (!ControllerSelection.isValid(controllerSelection)) {
            GTNHController.LOG.warn("Invalid controller selection '{}'; falling back to AUTO.", controllerSelection);
            controllerSelection = ControllerSelection.AUTOMATIC;
            configuration.get("controller", "selected", ControllerSelection.AUTOMATIC)
                .set(ControllerSelection.AUTOMATIC);
        }
        enableGameplayControls = configuration.getBoolean(
            "enableGameplayControls",
            "controls",
            enableGameplayControls,
            "Enable in-world controller mappings.");
        autoJump = configuration.getBoolean(
            "autoJump",
            "controls",
            autoJump,
            "Jump automatically when controller movement encounters a one-block rise with enough clearance.");
        autoSwim = configuration.getBoolean(
            "autoSwim",
            "controls",
            autoSwim,
            "Legacy Auto Swim switch. Migrated to activationModes.swim; true becomes TOGGLE and false becomes HOLD.");
        ActivationMode legacySwimMode = autoSwim ? ActivationMode.TOGGLE : ActivationMode.HOLD;
        swimActivationMode = getActivationMode(
            configuration,
            "swim",
            legacySwimMode,
            "Jump behavior in water. HOLD rises while Jump is held; TOGGLE presses once to rise and again to sink.");
        sneakActivationMode = getActivationMode(
            configuration,
            "sneak",
            sneakActivationMode,
            "Activation behavior for the controller Sneak action.");
        sprintActivationMode = getActivationMode(
            configuration,
            "sprint",
            sprintActivationMode,
            "Activation behavior for the controller Sprint action.");
        attackActivationMode = getActivationMode(
            configuration,
            "attack",
            attackActivationMode,
            "Activation behavior for the controller Attack / Mine action.");
        useActivationMode = getActivationMode(
            configuration,
            "use",
            useActivationMode,
            "Activation behavior for the controller Use / Place action.");
        autoSwim = swimActivationMode == ActivationMode.TOGGLE;
        moveSensitivity = configuration.getFloat(
            "moveSensitivity",
            "controls",
            moveSensitivity,
            0.25F,
            5.0F,
            "Changes partial left-stick response while preserving full movement at full stick deflection.");
        moveDeadZone = configuration.getFloat(
            "moveDeadZone",
            "controls",
            moveDeadZone,
            0.0F,
            0.90F,
            "Radial dead zone applied to the left stick.");
        moveCurveExponent = configuration.getFloat(
            "moveCurveExponent",
            "controls",
            moveCurveExponent,
            0.25F,
            4.0F,
            "Left-stick response exponent. 1.0 is linear; larger values provide more precision near the center.");
        lookSensitivity = configuration.getFloat(
            "lookSensitivity",
            "controls",
            lookSensitivity,
            0.25F,
            5.0F,
            "Scales controller camera rotation speed.");
        lookDeadZone = configuration.getFloat(
            "lookDeadZone",
            "controls",
            lookDeadZone,
            0.0F,
            0.90F,
            "Radial dead zone applied to the right stick.");
        lookCurveExponent = configuration.getFloat(
            "lookCurveExponent",
            "controls",
            lookCurveExponent,
            0.25F,
            4.0F,
            "Right-stick response exponent. Larger values provide finer aiming near the center.");
        lookSpeed = configuration.getFloat(
            "lookSpeed",
            "controls",
            lookSpeed,
            10.0F,
            720.0F,
            "Maximum camera rotation in degrees per second.");
        triggerThreshold = configuration.getFloat(
            "triggerThreshold",
            "controls",
            triggerThreshold,
            0.05F,
            0.95F,
            "Trigger dead zone: position at which a trigger binding becomes pressed.");
        invertLookX = configuration
            .getBoolean("invertLookX", "controls", invertLookX, "Invert the horizontal camera-stick direction.");
        invertLookY = configuration
            .getBoolean("invertLookY", "controls", invertLookY, "Invert the vertical right-stick camera direction.");
        enableGuiControls = configuration.getBoolean(
            "enableGuiControls",
            "gui",
            enableGuiControls,
            "Enable controller cursor and button input while a GUI is open.");
        cursorSensitivity = configuration
            .getFloat("cursorSensitivity", "gui", cursorSensitivity, 0.25F, 5.0F, "Scales controller cursor speed.");
        cursorStick = configuration.getString(
            "cursorStick",
            "gui",
            cursorStick,
            "Stick used for the GUI cursor. Valid values: LEFT or RIGHT.");
        cursorDeadZone = configuration.getFloat(
            "cursorDeadZone",
            "gui",
            cursorDeadZone,
            0.0F,
            0.90F,
            "Radial dead zone applied to the GUI cursor stick.");
        cursorCurveExponent = configuration.getFloat(
            "cursorCurveExponent",
            "gui",
            cursorCurveExponent,
            0.25F,
            4.0F,
            "GUI cursor response exponent. Larger values provide finer movement near the center.");
        cursorCurveExponent = migrateLegacyGuiValue(
            configuration,
            "cursorCurveExponent",
            cursorCurveExponent,
            1.4F,
            1.8F);
        cursorSpeed = configuration.getFloat(
            "cursorSpeed",
            "gui",
            cursorSpeed,
            100.0F,
            4000.0F,
            "Maximum GUI cursor speed in display pixels per second.");
        cursorSpeed = migrateLegacyGuiValue(configuration, "cursorSpeed", cursorSpeed, 900.0F, 420.0F);
        cursorAcceleration = configuration.getFloat(
            "cursorAcceleration",
            "gui",
            cursorAcceleration,
            100.0F,
            10000.0F,
            "How quickly the GUI cursor accelerates toward the requested speed, in display pixels per second squared.");
        cursorDeceleration = configuration.getFloat(
            "cursorDeceleration",
            "gui",
            cursorDeceleration,
            100.0F,
            10000.0F,
            "How quickly the GUI cursor stops or changes direction, in display pixels per second squared.");
        invertCursorX = configuration
            .getBoolean("invertCursorX", "gui", invertCursorX, "Invert the horizontal GUI cursor-stick direction.");
        invertCursorY = configuration
            .getBoolean("invertCursorY", "gui", invertCursorY, "Invert the vertical GUI cursor-stick direction.");
        enableButtonNavigation = configuration.getBoolean(
            "enableButtonNavigation",
            "guiNavigation",
            enableButtonNavigation,
            "Allow configured GUI navigation actions to move the cursor between visible buttons.");
        enableSlotNavigation = configuration.getBoolean(
            "enableSlotNavigation",
            "guiNavigation",
            enableSlotNavigation,
            "Include visible inventory slots as targets for configured GUI navigation actions.");
        precisionCursorScale = configuration.getFloat(
            "precisionCursorScale",
            "guiNavigation",
            precisionCursorScale,
            0.10F,
            1.0F,
            "Cursor speed multiplier while the GUI precision action is held.");
        navigationInitialDelayMillis = configuration.getInt(
            "navigationInitialDelayMillis",
            "guiNavigation",
            navigationInitialDelayMillis,
            100,
            1000,
            "Delay before a held GUI navigation or scroll action starts repeating.");
        navigationRepeatIntervalMillis = configuration.getInt(
            "navigationRepeatIntervalMillis",
            "guiNavigation",
            navigationRepeatIntervalMillis,
            50,
            500,
            "Delay between repeated GUI navigation or scroll actions.");
        scrollAccelerationEnabled = configuration.getBoolean(
            "scrollAccelerationEnabled",
            "guiNavigation",
            scrollAccelerationEnabled,
            "Accelerate GUI scrolling while its configured controller input remains held.");
        scrollAccelerationMultiplier = configuration.getFloat(
            "scrollAccelerationMultiplier",
            "guiNavigation",
            scrollAccelerationMultiplier,
            1.0F,
            5.0F,
            "Maximum held-scroll speed relative to normal GUI scrolling.");
        showControllerPrompts = configuration.getBoolean(
            "showControllerPrompts",
            "accessibility",
            showControllerPrompts,
            "Show context-sensitive controller button prompts along the bottom of GUI screens.");
        showActiveModeHud = configuration.getBoolean(
            "showActiveModeHud",
            "accessibility",
            showActiveModeHud,
            "Show latched accessibility modes, the modifier layer, and radial page on the HUD.");
        largeCursor = configuration.getBoolean(
            "largeCursor",
            "accessibility",
            largeCursor,
            "Use a larger high-contrast controller-owned GUI cursor.");
        cursorTrail = configuration.getBoolean(
            "cursorTrail",
            "accessibility",
            cursorTrail,
            "Draw a short high-contrast trail behind the controller-owned GUI cursor.");
        rumbleEnabled = configuration.getBoolean(
            "enabled",
            "rumble",
            rumbleEnabled,
            "Enable controller rumble when the selected controller reports rumble support.");
        rumbleDamage = configuration
            .getBoolean("damage", "rumble", rumbleDamage, "Rumble when the player takes damage.");
        rumbleExplosions = configuration
            .getBoolean("explosions", "rumble", rumbleExplosions, "Rumble for nearby explosion sounds.");
        rumbleMining = configuration
            .getBoolean("mining", "rumble", rumbleMining, "Provide light periodic feedback while mining a block.");
        rumbleFishing = configuration
            .getBoolean("fishing", "rumble", rumbleFishing, "Rumble when a fishing bite splashes near the bobber.");
        rumbleLowHealth = configuration.getBoolean(
            "lowHealth",
            "rumble",
            rumbleLowHealth,
            "Pulse periodically while the player has three hearts or less.");
        rumbleIntensity = configuration.getFloat(
            "intensity",
            "rumble",
            rumbleIntensity,
            0.0F,
            1.0F,
            "Global multiplier applied to all controller rumble effects.");
        String configuredRadialMode = configuration.getString(
            "activationMode",
            "radialMenu",
            radialMenuActivationMode.name(),
            "HOLD activates when the radial input is released. TOGGLE stays open until Confirm or Back is pressed.");
        radialMenuActivationMode = RadialMenuActivationMode.parse(configuredRadialMode, RadialMenuActivationMode.HOLD);
        if (!radialMenuActivationMode.name()
            .equalsIgnoreCase(configuredRadialMode.trim())) {
            GTNHController.LOG
                .warn("Invalid radial menu activation mode '{}'; falling back to HOLD.", configuredRadialMode);
            configuration.get("radialMenu", "activationMode", RadialMenuActivationMode.HOLD.name())
                .set(RadialMenuActivationMode.HOLD.name());
        }

        jumpBinding = getBinding(configuration, "jump", jumpBinding, "Jump while playing.");
        sneakBinding = getBinding(configuration, "sneak", sneakBinding, "Sneak while playing.");
        sprintBinding = getBinding(configuration, "sprint", sprintBinding, "Sprint while playing.");
        attackBinding = getBinding(configuration, "attack", attackBinding, "Attack or mine.");
        useBinding = getBinding(configuration, "use", useBinding, "Use an item or place a block.");
        dropItemBinding = getBinding(configuration, "dropItem", dropItemBinding, "Drop the selected item.");
        hotbarPreviousBinding = getBinding(
            configuration,
            "hotbarPrevious",
            hotbarPreviousBinding,
            "Select the previous hotbar slot.");
        hotbarNextBinding = getBinding(configuration, "hotbarNext", hotbarNextBinding, "Select the next hotbar slot.");
        openInventoryBinding = getBinding(
            configuration,
            "openInventory",
            openInventoryBinding,
            "Open the player inventory.");
        pauseBinding = getBinding(configuration, "pause", pauseBinding, "Open the pause menu.");
        radialMenuBinding = getBinding(
            configuration,
            "radialMenu",
            radialMenuBinding,
            "Open the radial action menu. Its HOLD or TOGGLE behavior is configured under radialMenu.activationMode.");
        modifierLayerBinding = getBinding(
            configuration,
            "modifierLayer",
            modifierLayerBinding,
            "Hold to use the alternate gameplay and Minecraft/mod binding layer.");
        guiConfirmBinding = getBinding(
            configuration,
            "guiConfirm",
            guiConfirmBinding,
            "Left-click while a GUI is open.");
        guiQuickMoveBinding = getBinding(
            configuration,
            "guiQuickMove",
            guiQuickMoveBinding,
            "Immediately transfer the hovered inventory stack using the container's normal shift-click operation.");
        guiAlternateBinding = getBinding(
            configuration,
            "guiAlternate",
            guiAlternateBinding,
            "Right-click while a GUI is open.");
        guiBackBinding = getBinding(configuration, "guiBack", guiBackBinding, "Send Escape while a GUI is open.");
        guiKeyboardBinding = getBinding(
            configuration,
            "guiKeyboard",
            guiKeyboardBinding,
            "Open or close the on-screen keyboard while a GUI is open.");
        migrateLegacyGuiKeyboardBinding(configuration, hadQuickMoveBinding);
        guiScrollUpBinding = getBinding(
            configuration,
            "guiScrollUp",
            guiScrollUpBinding,
            "Scroll upward while a supported GUI is open.");
        guiScrollDownBinding = getBinding(
            configuration,
            "guiScrollDown",
            guiScrollDownBinding,
            "Scroll downward while a supported GUI is open.");
        guiNavigateUpBinding = getBinding(
            configuration,
            "guiNavigateUp",
            guiNavigateUpBinding,
            "Move the GUI cursor to the nearest target above it.");
        guiNavigateDownBinding = getBinding(
            configuration,
            "guiNavigateDown",
            guiNavigateDownBinding,
            "Move the GUI cursor to the nearest target below it.");
        guiNavigateLeftBinding = getBinding(
            configuration,
            "guiNavigateLeft",
            guiNavigateLeftBinding,
            "Move the GUI cursor to the nearest target to its left.");
        guiNavigateRightBinding = getBinding(
            configuration,
            "guiNavigateRight",
            guiNavigateRightBinding,
            "Move the GUI cursor to the nearest target to its right.");
        guiPrecisionBinding = getBinding(
            configuration,
            "guiPrecision",
            guiPrecisionBinding,
            "Reduce GUI cursor speed while held.");
        modKeyBindings.clear();
        modKeyBindings.putAll(
            ModKeyBindingConfigCodec.decode(
                configuration.getStringList(
                    "entries",
                    "modBindings",
                    new String[0],
                    "Dynamic controller bindings for registered Minecraft and mod key actions.")));
        modifierModKeyBindings.clear();
        modifierModKeyBindings.putAll(
            ModKeyBindingConfigCodec.decode(
                configuration.getStringList(
                    "entries",
                    "modifierModBindings",
                    new String[0],
                    "Alternate-layer controller bindings for registered Minecraft and mod key actions.")));
        modifierBindings.clear();
        for (ControllerAction action : ControllerAction.values()) {
            if (!action.guiAction && action != ControllerAction.MODIFIER_LAYER) {
                modifierBindings.put(
                    action,
                    getBinding(
                        configuration,
                        "modifierBindings",
                        action.configKey,
                        "NONE",
                        "Alternate-layer binding for " + action.displayName + "."));
            }
        }
        migrateDropBinding(configuration);
        chatMacros.clear();
        chatMacros.putAll(
            ChatMacroConfigCodec.decode(
                configuration.getStringList(
                    "entries",
                    "chatMacros",
                    new String[0],
                    "User-created single-message chat or command macros available to the radial menu.")));
        radialMenuEntries.clear();
        for (RadialMenuPage page : RadialMenuPage.values()) {
            radialMenuEntries.put(
                page,
                RadialMenuConfigCodec.decode(
                    configuration.getStringList(
                        page.configKey,
                        "radialMenu",
                        new String[0],
                        "Registered actions assigned to the eight " + page.displayName + " radial-menu slots.")));
        }

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static String getBinding(ControllerAction action) {
        switch (action) {
            case JUMP:
                return jumpBinding;
            case SNEAK:
                return sneakBinding;
            case SPRINT:
                return sprintBinding;
            case ATTACK:
                return attackBinding;
            case USE:
                return useBinding;
            case DROP_ITEM:
                return dropItemBinding;
            case HOTBAR_PREVIOUS:
                return hotbarPreviousBinding;
            case HOTBAR_NEXT:
                return hotbarNextBinding;
            case OPEN_INVENTORY:
                return openInventoryBinding;
            case PAUSE:
                return pauseBinding;
            case RADIAL_MENU:
                return radialMenuBinding;
            case MODIFIER_LAYER:
                return modifierLayerBinding;
            case GUI_CONFIRM:
                return guiConfirmBinding;
            case GUI_QUICK_MOVE:
                return guiQuickMoveBinding;
            case GUI_ALTERNATE:
                return guiAlternateBinding;
            case GUI_BACK:
                return guiBackBinding;
            case GUI_KEYBOARD:
                return guiKeyboardBinding;
            case GUI_SCROLL_UP:
                return guiScrollUpBinding;
            case GUI_SCROLL_DOWN:
                return guiScrollDownBinding;
            case GUI_NAV_UP:
                return guiNavigateUpBinding;
            case GUI_NAV_DOWN:
                return guiNavigateDownBinding;
            case GUI_NAV_LEFT:
                return guiNavigateLeftBinding;
            case GUI_NAV_RIGHT:
                return guiNavigateRightBinding;
            case GUI_PRECISION:
                return guiPrecisionBinding;
            default:
                throw new IllegalArgumentException("Unknown controller action: " + action);
        }
    }

    public static void setBinding(ControllerAction action, String binding) {
        switch (action) {
            case JUMP:
                jumpBinding = binding;
                break;
            case SNEAK:
                sneakBinding = binding;
                break;
            case SPRINT:
                sprintBinding = binding;
                break;
            case ATTACK:
                attackBinding = binding;
                break;
            case USE:
                useBinding = binding;
                break;
            case DROP_ITEM:
                dropItemBinding = binding;
                break;
            case HOTBAR_PREVIOUS:
                hotbarPreviousBinding = binding;
                break;
            case HOTBAR_NEXT:
                hotbarNextBinding = binding;
                break;
            case OPEN_INVENTORY:
                openInventoryBinding = binding;
                break;
            case PAUSE:
                pauseBinding = binding;
                break;
            case RADIAL_MENU:
                radialMenuBinding = binding;
                break;
            case MODIFIER_LAYER:
                modifierLayerBinding = binding;
                break;
            case GUI_CONFIRM:
                guiConfirmBinding = binding;
                break;
            case GUI_QUICK_MOVE:
                guiQuickMoveBinding = binding;
                break;
            case GUI_ALTERNATE:
                guiAlternateBinding = binding;
                break;
            case GUI_BACK:
                guiBackBinding = binding;
                break;
            case GUI_KEYBOARD:
                guiKeyboardBinding = binding;
                break;
            case GUI_SCROLL_UP:
                guiScrollUpBinding = binding;
                break;
            case GUI_SCROLL_DOWN:
                guiScrollDownBinding = binding;
                break;
            case GUI_NAV_UP:
                guiNavigateUpBinding = binding;
                break;
            case GUI_NAV_DOWN:
                guiNavigateDownBinding = binding;
                break;
            case GUI_NAV_LEFT:
                guiNavigateLeftBinding = binding;
                break;
            case GUI_NAV_RIGHT:
                guiNavigateRightBinding = binding;
                break;
            case GUI_PRECISION:
                guiPrecisionBinding = binding;
                break;
            default:
                throw new IllegalArgumentException("Unknown controller action: " + action);
        }
    }

    public static String getModKeyBinding(String identifier) {
        return getModKeyBinding(identifier, ControllerBindingLayer.PRIMARY);
    }

    public static String getModKeyBinding(String identifier, ControllerBindingLayer layer) {
        Map<String, String> bindings = layer == ControllerBindingLayer.MODIFIER ? modifierModKeyBindings
            : modKeyBindings;
        String binding = bindings.get(identifier);
        return binding == null ? "NONE" : binding;
    }

    public static void setModKeyBinding(String identifier, String binding) {
        setModKeyBinding(identifier, binding, ControllerBindingLayer.PRIMARY);
    }

    public static void setModKeyBinding(String identifier, String binding, ControllerBindingLayer layer) {
        Map<String, String> bindings = layer == ControllerBindingLayer.MODIFIER ? modifierModKeyBindings
            : modKeyBindings;
        if (binding == null || "NONE".equalsIgnoreCase(binding)) {
            bindings.remove(identifier);
        } else {
            bindings.put(identifier, binding);
        }
    }

    public static String getRadialMenuEntry(int slot) {
        return getRadialMenuEntry(RadialMenuPage.BASE, slot);
    }

    public static String getRadialMenuEntry(RadialMenuPage page, int slot) {
        requireRadialMenuPage(page);
        requireRadialMenuSlot(slot);
        String identifier = radialMenuEntries.get(page)[slot];
        return identifier == null ? "" : identifier;
    }

    public static void setRadialMenuEntry(int slot, String identifier) {
        setRadialMenuEntry(RadialMenuPage.BASE, slot, identifier);
    }

    public static void setRadialMenuEntry(RadialMenuPage page, int slot, String identifier) {
        requireRadialMenuPage(page);
        requireRadialMenuSlot(slot);
        radialMenuEntries.get(page)[slot] = identifier == null ? "" : identifier;
    }

    public static List<ChatMacro> getChatMacros() {
        return Collections.unmodifiableList(new ArrayList<ChatMacro>(chatMacros.values()));
    }

    public static ChatMacro getChatMacro(String id) {
        return id == null ? null : chatMacros.get(id);
    }

    public static ChatMacro findChatMacro(String radialIdentifier) {
        return getChatMacro(ChatMacro.idFromRadialIdentifier(radialIdentifier));
    }

    public static void putChatMacro(ChatMacro macro) {
        if (macro == null) {
            throw new IllegalArgumentException("Chat macro cannot be null");
        }
        chatMacros.put(macro.getId(), macro);
    }

    public static void removeChatMacro(String id) {
        ChatMacro removed = chatMacros.remove(id);
        if (removed == null) {
            return;
        }
        String identifier = removed.getRadialIdentifier();
        for (String[] entries : radialMenuEntries.values()) {
            for (int slot = 0; slot < entries.length; slot++) {
                if (identifier.equals(entries[slot])) {
                    entries[slot] = "";
                }
            }
        }
    }

    public static File getConfigFile() {
        return configFile;
    }

    public static String getBinding(ControllerAction action, ControllerBindingLayer layer) {
        if (layer == ControllerBindingLayer.PRIMARY || action.guiAction || action == ControllerAction.MODIFIER_LAYER) {
            return getBinding(action);
        }
        String binding = modifierBindings.get(action);
        return binding == null ? "NONE" : binding;
    }

    public static void setBinding(ControllerAction action, String binding, ControllerBindingLayer layer) {
        if (layer == ControllerBindingLayer.PRIMARY || action.guiAction || action == ControllerAction.MODIFIER_LAYER) {
            setBinding(action, binding);
        } else {
            modifierBindings.put(action, binding);
        }
    }

    public static void resetActivationModes() {
        swimActivationMode = ActivationMode.HOLD;
        sneakActivationMode = ActivationMode.HOLD;
        sprintActivationMode = ActivationMode.HOLD;
        attackActivationMode = ActivationMode.HOLD;
        useActivationMode = ActivationMode.HOLD;
        showControllerPrompts = true;
        showActiveModeHud = true;
        largeCursor = false;
        cursorTrail = false;
        saveControllerSettings();
    }

    public static void saveControllerSettings() {
        if (configFile == null) {
            GTNHController.LOG.error("Cannot save controller settings before the configuration file is initialized");
            return;
        }

        Configuration configuration = new Configuration(configFile);
        configuration.get("controls", "enableGameplayControls", enableGameplayControls)
            .set(enableGameplayControls);
        configuration.get("controls", "autoJump", autoJump)
            .set(autoJump);
        autoSwim = swimActivationMode == ActivationMode.TOGGLE;
        configuration.get("controls", "autoSwim", autoSwim)
            .set(autoSwim);
        configuration.get("activationModes", "swim", swimActivationMode.name())
            .set(swimActivationMode.name());
        configuration.get("activationModes", "sneak", sneakActivationMode.name())
            .set(sneakActivationMode.name());
        configuration.get("activationModes", "sprint", sprintActivationMode.name())
            .set(sprintActivationMode.name());
        configuration.get("activationModes", "attack", attackActivationMode.name())
            .set(attackActivationMode.name());
        configuration.get("activationModes", "use", useActivationMode.name())
            .set(useActivationMode.name());
        configuration.get("controls", "moveSensitivity", moveSensitivity)
            .set(moveSensitivity);
        configuration.get("controls", "lookSensitivity", lookSensitivity)
            .set(lookSensitivity);
        configuration.get("controls", "moveDeadZone", moveDeadZone)
            .set(moveDeadZone);
        configuration.get("controls", "lookDeadZone", lookDeadZone)
            .set(lookDeadZone);
        configuration.get("controls", "triggerThreshold", triggerThreshold)
            .set(triggerThreshold);
        configuration.get("controls", "invertLookX", invertLookX)
            .set(invertLookX);
        configuration.get("controls", "invertLookY", invertLookY)
            .set(invertLookY);
        configuration.get("controller", "selected", controllerSelection)
            .set(controllerSelection);
        configuration.get("gui", "enableGuiControls", enableGuiControls)
            .set(enableGuiControls);
        configuration.get("gui", "cursorSensitivity", cursorSensitivity)
            .set(cursorSensitivity);
        configuration.get("gui", "cursorDeadZone", cursorDeadZone)
            .set(cursorDeadZone);
        configuration.get("gui", "invertCursorX", invertCursorX)
            .set(invertCursorX);
        configuration.get("gui", "invertCursorY", invertCursorY)
            .set(invertCursorY);
        configuration.get("guiNavigation", "enableButtonNavigation", enableButtonNavigation)
            .set(enableButtonNavigation);
        configuration.get("guiNavigation", "enableSlotNavigation", enableSlotNavigation)
            .set(enableSlotNavigation);
        configuration.get("guiNavigation", "precisionCursorScale", precisionCursorScale)
            .set(precisionCursorScale);
        configuration.get("guiNavigation", "navigationInitialDelayMillis", navigationInitialDelayMillis)
            .set(navigationInitialDelayMillis);
        configuration.get("guiNavigation", "navigationRepeatIntervalMillis", navigationRepeatIntervalMillis)
            .set(navigationRepeatIntervalMillis);
        configuration.get("guiNavigation", "scrollAccelerationEnabled", scrollAccelerationEnabled)
            .set(scrollAccelerationEnabled);
        configuration.get("guiNavigation", "scrollAccelerationMultiplier", scrollAccelerationMultiplier)
            .set(scrollAccelerationMultiplier);
        configuration.get("accessibility", "showControllerPrompts", showControllerPrompts)
            .set(showControllerPrompts);
        configuration.get("accessibility", "showActiveModeHud", showActiveModeHud)
            .set(showActiveModeHud);
        configuration.get("accessibility", "largeCursor", largeCursor)
            .set(largeCursor);
        configuration.get("accessibility", "cursorTrail", cursorTrail)
            .set(cursorTrail);
        configuration.get("rumble", "enabled", rumbleEnabled)
            .set(rumbleEnabled);
        configuration.get("rumble", "damage", rumbleDamage)
            .set(rumbleDamage);
        configuration.get("rumble", "explosions", rumbleExplosions)
            .set(rumbleExplosions);
        configuration.get("rumble", "mining", rumbleMining)
            .set(rumbleMining);
        configuration.get("rumble", "fishing", rumbleFishing)
            .set(rumbleFishing);
        configuration.get("rumble", "lowHealth", rumbleLowHealth)
            .set(rumbleLowHealth);
        configuration.get("rumble", "intensity", rumbleIntensity)
            .set(rumbleIntensity);
        configuration.get("radialMenu", "activationMode", radialMenuActivationMode.name())
            .set(radialMenuActivationMode.name());
        for (ControllerAction action : ControllerAction.values()) {
            String binding = getBinding(action);
            configuration.get("bindings", action.configKey, binding)
                .set(binding);
            if (!action.guiAction && action != ControllerAction.MODIFIER_LAYER) {
                String modifierBinding = getBinding(action, ControllerBindingLayer.MODIFIER);
                configuration.get("modifierBindings", action.configKey, modifierBinding)
                    .set(modifierBinding);
            }
        }
        configuration.get("modBindings", "entries", new String[0])
            .set(ModKeyBindingConfigCodec.encode(modKeyBindings));
        configuration.get("modifierModBindings", "entries", new String[0])
            .set(ModKeyBindingConfigCodec.encode(modifierModKeyBindings));
        configuration.get("chatMacros", "entries", new String[0])
            .set(ChatMacroConfigCodec.encode(chatMacros));
        for (RadialMenuPage page : RadialMenuPage.values()) {
            configuration.get("radialMenu", page.configKey, new String[0])
                .set(RadialMenuConfigCodec.encode(radialMenuEntries.get(page)));
        }

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private static String getBinding(Configuration configuration, String name, String defaultValue,
        String actionDescription) {
        return getBinding(configuration, "bindings", name, defaultValue, actionDescription);
    }

    private static String getBinding(Configuration configuration, String category, String name, String defaultValue,
        String actionDescription) {
        return configuration.getString(
            name,
            category,
            defaultValue,
            actionDescription
                + " Use BUTTON:<name> or TRIGGER:<name>; + joins a chord, | separates alternatives, and NONE unbinds.");
    }

    private static ActivationMode getActivationMode(Configuration configuration, String name,
        ActivationMode defaultValue, String description) {
        String configuredValue = configuration.getString(
            name,
            "activationModes",
            defaultValue.name(),
            description + " Valid values: HOLD, TOGGLE, PRESS.");
        ActivationMode parsedValue = ActivationMode.parse(configuredValue, defaultValue);
        if (!parsedValue.name()
            .equalsIgnoreCase(configuredValue.trim())) {
            GTNHController.LOG
                .warn("Invalid activation mode '{}' for {}. Falling back to {}.", configuredValue, name, defaultValue);
            configuration.get("activationModes", name, defaultValue.name())
                .set(defaultValue.name());
        }
        return parsedValue;
    }

    private static void migrateDropBinding(Configuration configuration) {
        String previousPrimary = modKeyBindings.remove(VANILLA_DROP_IDENTIFIER);
        if ("NONE".equalsIgnoreCase(dropItemBinding) && previousPrimary != null) {
            dropItemBinding = previousPrimary;
            configuration.get("bindings", ControllerAction.DROP_ITEM.configKey, dropItemBinding)
                .set(dropItemBinding);
        }
        configuration.get("modBindings", "entries", new String[0])
            .set(ModKeyBindingConfigCodec.encode(modKeyBindings));

        String previousModifier = modifierModKeyBindings.remove(VANILLA_DROP_IDENTIFIER);
        String modifierDrop = modifierBindings.get(ControllerAction.DROP_ITEM);
        if ((modifierDrop == null || "NONE".equalsIgnoreCase(modifierDrop)) && previousModifier != null) {
            modifierBindings.put(ControllerAction.DROP_ITEM, previousModifier);
            configuration.get("modifierBindings", ControllerAction.DROP_ITEM.configKey, previousModifier)
                .set(previousModifier);
        }
        configuration.get("modifierModBindings", "entries", new String[0])
            .set(ModKeyBindingConfigCodec.encode(modifierModKeyBindings));
    }

    private static void migrateLegacyGuiKeyboardBinding(Configuration configuration, boolean hadQuickMoveBinding) {
        String migratedBinding = migratedGuiKeyboardBinding(hadQuickMoveBinding, guiKeyboardBinding);
        if (migratedBinding.equals(guiKeyboardBinding)) {
            return;
        }

        guiKeyboardBinding = migratedBinding;
        configuration.get("bindings", ControllerAction.GUI_KEYBOARD.configKey, guiKeyboardBinding)
            .set(guiKeyboardBinding);
    }

    static String migratedGuiKeyboardBinding(boolean hadQuickMoveBinding, String configuredKeyboardBinding) {
        if (hadQuickMoveBinding || !LEGACY_GUI_KEYBOARD_DEFAULT.equalsIgnoreCase(configuredKeyboardBinding)) {
            return configuredKeyboardBinding;
        }
        return ControllerAction.GUI_KEYBOARD.defaultBinding;
    }

    private static float migrateLegacyGuiValue(Configuration configuration, String name, float configuredValue,
        float legacyDefault, float replacementDefault) {
        if (Math.abs(configuredValue - legacyDefault) > 0.0001F) {
            return configuredValue;
        }

        configuration.get("gui", name, legacyDefault)
            .set(replacementDefault);
        return replacementDefault;
    }

    private static void requireRadialMenuSlot(int slot) {
        if (slot < 0 || slot >= RadialMenuConfigCodec.SLOT_COUNT) {
            throw new IllegalArgumentException("Radial-menu slot is out of range: " + slot);
        }
    }

    private static void requireRadialMenuPage(RadialMenuPage page) {
        if (page == null || !radialMenuEntries.containsKey(page)) {
            throw new IllegalArgumentException("Radial-menu page is unavailable: " + page);
        }
    }
}
