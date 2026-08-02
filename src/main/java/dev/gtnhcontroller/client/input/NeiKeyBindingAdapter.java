package dev.gtnhcontroller.client.input;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;

import dev.gtnhcontroller.GTNHController;
import dev.gtnhcontroller.client.gui.GuiScreenControllerKeyDispatcher;

/**
 * Optional bridge for NEI key bindings. Older GTNH NEI builds keep them in an OptionKeyBind tree and KeyState map;
 * newer builds keep exact KeyBinding objects in KeyManager, but still poll raw keyboard state for GUI actions.
 */
final class NeiKeyBindingAdapter {

    private static final String KEY_MANAGER_CLASS = "codechicken.nei.KeyManager";
    private static final String CLIENT_CONFIG_CLASS = "codechicken.nei.NEIClientConfig";
    private static final String OPTION_LIST_CLASS = "codechicken.nei.config.OptionList";
    private static final String OPTION_KEY_BIND_CLASS = "codechicken.nei.config.OptionKeyBind";
    private static final Set<String> VIRTUAL_KEYS = Collections.synchronizedSet(new HashSet<String>());

    private final Set<String> pendingLegacyPulses = new HashSet<String>();

    private boolean initialized;
    private boolean unavailable;
    private boolean warned;
    private Class<?> keyManagerClass;
    private Field modernBindingsField;
    private Field legacyKeyStatesField;
    private Class<?> optionListClass;
    private Class<?> optionKeyBindClass;
    private Method getOptionListMethod;
    private Field optionChildrenField;
    private Field optionNameField;
    private Field optionUsesHashField;
    private Field keyStateDownField;
    private Field keyStateHeldField;
    private Field keyStateUpField;

    static boolean isVirtualKeyDown(String neiIdentifier) {
        return neiIdentifier != null && VIRTUAL_KEYS.contains(neiIdentifier);
    }

    List<NeiKeyBinding> discoverBindings() {
        initialize();
        if (unavailable) {
            return Collections.emptyList();
        }

        try {
            List<NeiKeyBinding> modernBindings = discoverModernBindings();
            if (modernBindings != null) {
                return modernBindings;
            }
            return discoverLegacyBindings();
        } catch (ReflectiveOperationException | LinkageError exception) {
            warnOnce("NEI controller binding discovery is incompatible with this NEI version", exception);
            return Collections.emptyList();
        } catch (RuntimeException exception) {
            // NEI plugins may add options from their loader thread. Retry on the next client tick.
            GTNHController.LOG.debug("NEI controller binding discovery will retry", exception);
            return Collections.emptyList();
        }
    }

    boolean update(NeiKeyBinding binding, boolean wasControllerDown, boolean controllerDown, Minecraft minecraft) {
        if (controllerDown) {
            VIRTUAL_KEYS.add(binding.neiIdentifier);
        } else {
            VIRTUAL_KEYS.remove(binding.neiIdentifier);
        }

        if (binding.legacy && !binding.modifierAware) {
            updateLegacyKeyState(binding.neiIdentifier, wasControllerDown, controllerDown);
        }
        return controllerDown && !wasControllerDown
            && minecraft != null
            && minecraft.currentScreen instanceof GuiContainer;
    }

    void pulse(NeiKeyBinding binding, Minecraft minecraft) {
        boolean alreadyHeld = VIRTUAL_KEYS.contains(binding.neiIdentifier);
        VIRTUAL_KEYS.add(binding.neiIdentifier);
        if (binding.legacy && !binding.modifierAware) {
            updateLegacyKeyState(binding.neiIdentifier, false, true);
            pendingLegacyPulses.add(binding.neiIdentifier);
        }
        if (minecraft.currentScreen instanceof GuiContainer) {
            dispatchToCurrentContainer(minecraft.currentScreen);
        }
        if (!alreadyHeld && (!binding.legacy || binding.modifierAware)) {
            VIRTUAL_KEYS.remove(binding.neiIdentifier);
        }
    }

    boolean releasePulses() {
        if (pendingLegacyPulses.isEmpty()) {
            return false;
        }
        for (String neiIdentifier : pendingLegacyPulses) {
            VIRTUAL_KEYS.remove(neiIdentifier);
            updateLegacyKeyState(neiIdentifier, true, false);
        }
        pendingLegacyPulses.clear();
        return true;
    }

    void dispatchCurrentContainer(Minecraft minecraft) {
        if (minecraft.currentScreen instanceof GuiContainer) {
            dispatchToCurrentContainer(minecraft.currentScreen);
        }
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            ClassLoader classLoader = NeiKeyBindingAdapter.class.getClassLoader();
            keyManagerClass = Class.forName(KEY_MANAGER_CLASS, false, classLoader);
            modernBindingsField = findOptionalField(keyManagerClass, "keyBindings");
            legacyKeyStatesField = findOptionalField(keyManagerClass, "keyStates");

            if (modernBindingsField == null) {
                optionListClass = Class.forName(OPTION_LIST_CLASS, false, classLoader);
                optionKeyBindClass = Class.forName(OPTION_KEY_BIND_CLASS, false, classLoader);
                Class<?> clientConfigClass = Class.forName(CLIENT_CONFIG_CLASS, false, classLoader);
                getOptionListMethod = clientConfigClass.getMethod("getOptionList");
                optionChildrenField = findRequiredField(optionListClass, "optionList");
                optionNameField = findRequiredField(optionKeyBindClass, "name");
                optionUsesHashField = findRequiredField(optionKeyBindClass, "useHash");
            }
        } catch (ClassNotFoundException exception) {
            unavailable = true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            unavailable = true;
            warnOnce("NEI controller binding support is incompatible with this NEI version", exception);
        }
    }

    private List<NeiKeyBinding> discoverModernBindings() throws IllegalAccessException {
        if (modernBindingsField == null) {
            return null;
        }

        Object value = modernBindingsField.get(null);
        if (!(value instanceof Map)) {
            return Collections.emptyList();
        }

        List<Map.Entry<?, ?>> entries = new ArrayList<Map.Entry<?, ?>>(((Map<?, ?>) value).entrySet());
        List<NeiKeyBinding> bindings = new ArrayList<NeiKeyBinding>();
        for (Map.Entry<?, ?> entry : entries) {
            if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof KeyBinding)) {
                continue;
            }
            String neiIdentifier = (String) entry.getKey();
            KeyBinding keyBinding = (KeyBinding) entry.getValue();
            bindings.add(
                new NeiKeyBinding(
                    neiIdentifier,
                    safe(keyBinding.getKeyCategory()),
                    safe(keyBinding.getKeyDescription()),
                    false,
                    false,
                    keyBinding));
        }
        sort(bindings);
        return bindings;
    }

    private List<NeiKeyBinding> discoverLegacyBindings() throws IllegalAccessException, InvocationTargetException {
        Map<String, Boolean> identifiers = new LinkedHashMap<String, Boolean>();
        Object rootOptionList = getOptionListMethod.invoke(null);
        Set<Object> visitedLists = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        collectLegacyOptions(rootOptionList, identifiers, visitedLists);

        Map<?, ?> keyStates = legacyKeyStates();
        if (keyStates != null) {
            for (Object identifier : new ArrayList<Object>(keyStates.keySet())) {
                if (identifier instanceof String && !identifiers.containsKey(identifier)) {
                    identifiers.put((String) identifier, Boolean.FALSE);
                }
            }
        }

        List<NeiKeyBinding> bindings = new ArrayList<NeiKeyBinding>();
        for (Map.Entry<String, Boolean> entry : identifiers.entrySet()) {
            String neiIdentifier = entry.getKey();
            bindings.add(
                new NeiKeyBinding(
                    neiIdentifier,
                    legacyCategory(neiIdentifier),
                    "nei.options.keys." + neiIdentifier,
                    true,
                    entry.getValue()
                        .booleanValue(),
                    null));
        }
        sort(bindings);
        return bindings;
    }

    private void collectLegacyOptions(Object optionList, Map<String, Boolean> identifiers, Set<Object> visitedLists)
        throws IllegalAccessException {
        if (optionList == null || !optionListClass.isInstance(optionList) || !visitedLists.add(optionList)) {
            return;
        }

        Object childrenValue = optionChildrenField.get(optionList);
        if (!(childrenValue instanceof Collection)) {
            return;
        }
        List<?> children = new ArrayList<Object>((Collection<?>) childrenValue);
        for (Object child : children) {
            if (optionKeyBindClass.isInstance(child)) {
                String optionName = (String) optionNameField.get(child);
                if (optionName != null && optionName.startsWith("keys.") && optionName.length() > 5) {
                    identifiers.put(optionName.substring(5), Boolean.valueOf(optionUsesHashField.getBoolean(child)));
                }
            } else if (optionListClass.isInstance(child)) {
                collectLegacyOptions(child, identifiers, visitedLists);
            }
        }
    }

    private void updateLegacyKeyState(String neiIdentifier, boolean wasControllerDown, boolean controllerDown) {
        try {
            Map<?, ?> keyStates = legacyKeyStates();
            Object keyState = keyStates == null ? null : keyStates.get(neiIdentifier);
            if (keyState == null) {
                return;
            }
            initializeKeyStateFields(keyState.getClass());
            keyStateDownField.setBoolean(keyState, controllerDown && !wasControllerDown);
            keyStateHeldField.setBoolean(keyState, controllerDown);
            keyStateUpField.setBoolean(keyState, !controllerDown && wasControllerDown);
        } catch (ReflectiveOperationException | LinkageError exception) {
            warnOnce("NEI controller key-state dispatch is incompatible with this NEI version", exception);
        }
    }

    private Map<?, ?> legacyKeyStates() throws IllegalAccessException {
        if (legacyKeyStatesField == null) {
            return null;
        }
        Object value = legacyKeyStatesField.get(null);
        return value instanceof Map ? (Map<?, ?>) value : null;
    }

    private void initializeKeyStateFields(Class<?> keyStateClass) throws NoSuchFieldException {
        if (keyStateDownField != null) {
            return;
        }
        keyStateDownField = findRequiredField(keyStateClass, "down");
        keyStateHeldField = findRequiredField(keyStateClass, "held");
        keyStateUpField = findRequiredField(keyStateClass, "up");
    }

    private static void dispatchToCurrentContainer(GuiScreen screen) {
        if (screen instanceof GuiScreenControllerKeyDispatcher) {
            ((GuiScreenControllerKeyDispatcher) screen).gtnhcontroller$dispatchKeyTyped('\0', 0);
        }
    }

    private static String legacyCategory(String neiIdentifier) {
        return neiIdentifier.startsWith("world.") ? "nei.options.keys.world" : "nei.options.keys.gui";
    }

    private static void sort(List<NeiKeyBinding> bindings) {
        Collections.sort(bindings, new Comparator<NeiKeyBinding>() {

            @Override
            public int compare(NeiKeyBinding first, NeiKeyBinding second) {
                return first.controllerIdentifier.compareTo(second.controllerIdentifier);
            }
        });
    }

    private static Field findOptionalField(Class<?> type, String fieldName) {
        try {
            return findRequiredField(type, fieldName);
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static Field findRequiredField(Class<?> type, String fieldName) throws NoSuchFieldException {
        for (Class<?> candidate = type; candidate != null; candidate = candidate.getSuperclass()) {
            try {
                Field field = candidate.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(type.getName() + "." + fieldName);
    }

    private void warnOnce(String message, Throwable exception) {
        if (!warned) {
            warned = true;
            GTNHController.LOG.warn(message, exception);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
