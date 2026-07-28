package dev.gtnhcontroller.client.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ScreenShotHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.GTNHController;
import dev.gtnhcontroller.mixins.KeyBindingControllerAccessor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Applies controller buttons directly to the exact KeyBinding objects registered by Minecraft and installed mods.
 * Analog movement and the other special built-in controller actions remain owned by {@link GameplayController}.
 */
public final class ModKeyBindingController {

    private final SdlGamepadManager gamepadManager;
    private final List<RegisteredKeyBinding> registeredBindings = new ArrayList<RegisteredKeyBinding>();
    private final Map<String, BindingState> states = new LinkedHashMap<String, BindingState>();
    private final Set<KeyBinding> pendingPulseReleases =
        Collections.newSetFromMap(new IdentityHashMap<KeyBinding, Boolean>());

    private KeyBinding[] observedKeyBindings = new KeyBinding[0];

    public ModKeyBindingController(SdlGamepadManager gamepadManager) {
        this.gamepadManager = gamepadManager;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        refreshIfRegistryChanged();
        boolean controllerStateChanged = releasePulsedBindings();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!canControlGameplay(minecraft)) {
            controllerStateChanged |= releaseControlledBindings();
            if (controllerStateChanged) {
                FMLCommonHandler.instance()
                    .fireKeyInput();
            }
            return;
        }

        for (BindingState state : states.values()) {
            boolean controllerDown = !state.controllerBinding.isEmpty()
                && state.controllerBinding.isDown(gamepadManager, Config.triggerThreshold);
            controllerStateChanged |= state.controllerDown != controllerDown;
            if (controllerDown && !state.controllerDown) {
                dispatchEventDrivenVanillaBinding(minecraft, state.entry);
            }
            updateExactBinding(state.entry.getKeyBinding(), state.controllerDown, controllerDown);
            state.controllerDown = controllerDown;
        }
        if (controllerStateChanged) {
            FMLCommonHandler.instance()
                .fireKeyInput();
        }
    }

    public List<RegisteredKeyBinding> getRegisteredBindings() {
        refreshIfRegistryChanged();
        return Collections.unmodifiableList(new ArrayList<RegisteredKeyBinding>(registeredBindings));
    }

    public void refreshBindings() {
        rebuildRegistry(Minecraft.getMinecraft());
    }

    public RegisteredKeyBinding findRegisteredBinding(String identifier) {
        refreshIfRegistryChanged();
        BindingState state = states.get(identifier);
        return state == null ? null : state.entry;
    }

    public boolean pulseBinding(String identifier) {
        refreshIfRegistryChanged();
        BindingState state = states.get(identifier);
        if (state == null) {
            return false;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        dispatchEventDrivenVanillaBinding(minecraft, state.entry);
        KeyBinding binding = state.entry.getKeyBinding();
        KeyBindingControllerAccessor accessor = (KeyBindingControllerAccessor) (Object) binding;
        accessor.gtnhcontroller$setPressTime(accessor.gtnhcontroller$getPressTime() + 1);
        accessor.gtnhcontroller$setPressed(true);
        pendingPulseReleases.add(binding);
        FMLCommonHandler.instance()
            .fireKeyInput();
        return true;
    }

    private static void dispatchEventDrivenVanillaBinding(Minecraft minecraft, RegisteredKeyBinding entry) {
        GameSettings settings = minecraft.gameSettings;
        KeyBinding binding = entry.getKeyBinding();
        if (binding == settings.keyBindScreenshot) {
            minecraft.ingameGUI
                .getChatGUI()
                .printChatMessage(
                    ScreenShotHelper.saveScreenshot(
                        minecraft.mcDataDir,
                        minecraft.displayWidth,
                        minecraft.displayHeight,
                        minecraft.getFramebuffer()));
        } else if (binding == settings.field_152395_am) {
            minecraft.toggleFullscreen();
        }
    }

    public void setBinding(String identifier, String bindingSpecification) {
        ControllerBinding parsedBinding = ControllerBinding.parse(bindingSpecification);
        Config.setModKeyBinding(identifier, bindingSpecification);
        Config.saveControllerSettings();

        BindingState state = states.get(identifier);
        if (state != null) {
            if (state.controllerDown) {
                updateExactBinding(state.entry.getKeyBinding(), true, false);
            }
            state.controllerBinding = parsedBinding;
            state.controllerDown = false;
        }
    }

    public boolean hasConflict(String identifier) {
        BindingState subject = states.get(identifier);
        if (subject == null || subject.controllerBinding.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, BindingState> candidate : states.entrySet()) {
            if (!identifier.equals(candidate.getKey())
                && subject.controllerBinding.conflictsWith(candidate.getValue().controllerBinding)) {
                return true;
            }
        }
        for (ControllerAction action : ControllerAction.values()) {
            if (!action.guiAction
                && subject.controllerBinding.conflictsWith(safeParse(Config.getBinding(action), action.displayName))) {
                return true;
            }
        }
        return false;
    }

    private void refreshIfRegistryChanged() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.gameSettings == null) {
            return;
        }

        KeyBinding[] currentBindings = minecraft.gameSettings.keyBindings;
        if (!sameRegistry(currentBindings, observedKeyBindings)) {
            rebuildRegistry(minecraft);
        }
    }

    private void rebuildRegistry(Minecraft minecraft) {
        if (minecraft.gameSettings == null) {
            return;
        }

        releaseControlledBindings();
        registeredBindings.clear();
        states.clear();

        KeyBinding[] currentBindings = minecraft.gameSettings.keyBindings;
        observedKeyBindings = currentBindings.clone();
        Set<KeyBinding> excludedBindings = builtInControllerBindings(minecraft.gameSettings);
        Set<KeyBinding> seenBindings = Collections.newSetFromMap(new IdentityHashMap<KeyBinding, Boolean>());
        Map<String, Integer> occurrences = new HashMap<String, Integer>();

        for (KeyBinding keyBinding : currentBindings) {
            if (keyBinding == null || excludedBindings.contains(keyBinding) || !seenBindings.add(keyBinding)) {
                continue;
            }

            String categoryKey = safeTranslationKey(keyBinding.getKeyCategory());
            String descriptionKey = safeTranslationKey(keyBinding.getKeyDescription());
            String identifierBase = ModKeyBindingIdentifier.base(categoryKey, descriptionKey);
            int occurrence = occurrences.containsKey(identifierBase) ? occurrences.get(identifierBase).intValue() + 1
                : 1;
            occurrences.put(identifierBase, Integer.valueOf(occurrence));

            String identifier = ModKeyBindingIdentifier.create(categoryKey, descriptionKey, occurrence);
            RegisteredKeyBinding entry = new RegisteredKeyBinding(
                keyBinding,
                identifier,
                categoryKey,
                descriptionKey,
                localize(categoryKey),
                localize(descriptionKey),
                occurrence);
            ControllerBinding controllerBinding = safeParse(
                Config.getModKeyBinding(identifier),
                entry.getDisplayName());
            registeredBindings.add(entry);
            states.put(identifier, new BindingState(entry, controllerBinding));
        }
    }

    private boolean canControlGameplay(Minecraft minecraft) {
        return Config.enableGameplayControls && gamepadManager.isConnected()
            && minecraft.thePlayer != null
            && minecraft.currentScreen == null
            && minecraft.inGameHasFocus;
    }

    private boolean releaseControlledBindings() {
        boolean releasedAnyBinding = false;
        for (BindingState state : states.values()) {
            if (state.controllerDown) {
                updateExactBinding(state.entry.getKeyBinding(), true, false);
                state.controllerDown = false;
                releasedAnyBinding = true;
            }
        }
        return releasedAnyBinding;
    }

    private boolean releasePulsedBindings() {
        if (pendingPulseReleases.isEmpty()) {
            return false;
        }

        for (KeyBinding binding : pendingPulseReleases) {
            KeyBindingControllerAccessor accessor = (KeyBindingControllerAccessor) (Object) binding;
            accessor.gtnhcontroller$setPressed(isPhysicalInputDown(binding));
        }
        pendingPulseReleases.clear();
        return true;
    }

    private static void updateExactBinding(KeyBinding binding, boolean wasControllerDown, boolean controllerDown) {
        KeyBindingControllerAccessor accessor = (KeyBindingControllerAccessor) (Object) binding;
        if (controllerDown && !wasControllerDown) {
            accessor.gtnhcontroller$setPressTime(accessor.gtnhcontroller$getPressTime() + 1);
        }
        if (wasControllerDown || controllerDown) {
            accessor.gtnhcontroller$setPressed(controllerDown || isPhysicalInputDown(binding));
        }
    }

    private static boolean isPhysicalInputDown(KeyBinding binding) {
        int keyCode = binding.getKeyCode();
        if (keyCode < 0) {
            int mouseButton = keyCode + 100;
            return mouseButton >= 0 && Mouse.isButtonDown(mouseButton);
        }
        return keyCode > 0 && Keyboard.isKeyDown(keyCode);
    }

    private static Set<KeyBinding> builtInControllerBindings(GameSettings settings) {
        Set<KeyBinding> excluded = Collections.newSetFromMap(new IdentityHashMap<KeyBinding, Boolean>());
        excluded.add(settings.keyBindForward);
        excluded.add(settings.keyBindLeft);
        excluded.add(settings.keyBindBack);
        excluded.add(settings.keyBindRight);
        excluded.add(settings.keyBindJump);
        excluded.add(settings.keyBindSneak);
        excluded.add(settings.keyBindSprint);
        excluded.add(settings.keyBindAttack);
        excluded.add(settings.keyBindUseItem);
        excluded.add(settings.keyBindInventory);
        return excluded;
    }

    private static boolean sameRegistry(KeyBinding[] first, KeyBinding[] second) {
        if (first.length != second.length) {
            return false;
        }
        for (int index = 0; index < first.length; index++) {
            if (first[index] != second[index]) {
                return false;
            }
        }
        return true;
    }

    private static String localize(String translationKey) {
        if (translationKey.isEmpty()) {
            return "Unknown";
        }
        return I18n.format(translationKey);
    }

    private static String safeTranslationKey(String translationKey) {
        return translationKey == null ? "" : translationKey;
    }

    private static ControllerBinding safeParse(String specification, String actionName) {
        try {
            return ControllerBinding.parse(specification);
        } catch (IllegalArgumentException exception) {
            GTNHController.LOG.error(
                "Invalid controller binding '{}' for registered action '{}'; treating it as unbound.",
                specification,
                actionName,
                exception);
            return ControllerBinding.parse("NONE");
        }
    }

    private static final class BindingState {

        private final RegisteredKeyBinding entry;
        private ControllerBinding controllerBinding;
        private boolean controllerDown;

        private BindingState(RegisteredKeyBinding entry, ControllerBinding controllerBinding) {
            this.entry = entry;
            this.controllerBinding = controllerBinding;
        }
    }
}
