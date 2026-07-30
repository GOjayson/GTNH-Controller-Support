package dev.gtnhcontroller.client.gui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.GTNHController;
import dev.gtnhcontroller.mixins.CreativeContainerControllerAccessor;
import dev.gtnhcontroller.mixins.GuiContainerCreativeControllerAccessor;
import dev.gtnhcontroller.mixins.GuiSlotControllerAccessor;

/**
 * Bridges controller clicks into screens that bypass {@link GuiScreen}'s normal mouse callbacks.
 *
 * <p>
 * Vanilla's world list receives native mouse events through {@link GuiSlot}, while BetterQuesting dispatches them to
 * its own canvas API. JourneyMap polls the physical mouse button for map dragging, and Galacticraft leaves a custom
 * OpenGL/input state active in its celestial map. The same adapters provide scrolling because Minecraft 1.7.10 has no
 * common GUI scroll callback. They intentionally stay narrow so a normal screen never receives the same input twice.
 */
final class GuiInputCompatibility {

    private static final String BETTER_QUESTING_SCREEN = "betterquesting.api2.client.gui.GuiScreenCanvas";
    private static final String BETTER_QUESTING_CONTAINER = "betterquesting.api2.client.gui.GuiContainerCanvas";
    private static final long DOUBLE_CLICK_MILLIS = 250L;

    private final GalacticraftInputAdapter galacticraftInputAdapter = new GalacticraftInputAdapter();
    private final JourneyMapInputAdapter journeyMapInputAdapter = new JourneyMapInputAdapter();
    private final NeiFocusedTextDispatcher neiFocusedTextDispatcher = NeiFocusedTextDispatcher.create();
    private final NeiKeyboardDispatcher neiKeyboardDispatcher = NeiKeyboardDispatcher.create();

    private GuiSlot lastWorldList;
    private int lastWorldIndex = -1;
    private int lastWorldButton = -1;
    private long lastWorldClickMillis;

    boolean interceptMousePressed(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        if (!(screen instanceof GuiMultiplayer)) {
            return false;
        }

        GuiSlot serverList = findGuiSlot(screen);
        return serverList instanceof GuiListExtended
            && ((GuiListExtended) serverList).func_148179_a(mouseX, mouseY, mouseButton);
    }

    boolean interceptMouseReleased(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        if (!(screen instanceof GuiMultiplayer)) {
            return false;
        }

        GuiSlot serverList = findGuiSlot(screen);
        return serverList instanceof GuiListExtended
            && ((GuiListExtended) serverList).func_148181_b(mouseX, mouseY, mouseButton);
    }

    void mousePressed(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        if (screen instanceof GuiSelectWorld) {
            clickWorldList((GuiSelectWorld) screen, mouseX, mouseY, mouseButton);
        }
        invokeBetterQuesting(screen, "onMouseClick", mouseX, mouseY, mouseButton);
        journeyMapInputAdapter.mousePressed(screen, mouseX, mouseY, mouseButton);
    }

    void mouseDragged(GuiScreen screen, int mouseX, int mouseY, int mouseButton, long heldTimeMillis) {
        journeyMapInputAdapter.mouseDragged(screen, mouseX, mouseY, mouseButton);
    }

    void beforeMouseReleased(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        journeyMapInputAdapter.beforeMouseReleased(screen, mouseX, mouseY, mouseButton);
    }

    void mouseReleased(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        invokeBetterQuesting(screen, "onMouseRelease", mouseX, mouseY, mouseButton);
        journeyMapInputAdapter.mouseReleased(screen, mouseButton);
    }

    boolean scroll(GuiScreen screen, int mouseX, int mouseY, int direction) {
        if (invokeBetterQuesting(screen, "onMouseScroll", mouseX, mouseY, direction)) {
            return true;
        }
        if (screen instanceof GuiContainerCreative) {
            return scrollCreativeInventory((GuiContainerCreative) screen, direction);
        }
        if (galacticraftInputAdapter.scroll(screen, direction)) {
            return true;
        }

        GuiSlot guiSlot = findGuiSlot(screen);
        if (guiSlot == null) {
            return false;
        }

        guiSlot.scrollBy(direction * Math.max(guiSlot.slotHeight / 2, 1));
        return true;
    }

    void keyTyped(GuiScreen screen, char typedCharacter, int keyCode) {
        /*
         * NEI draws its search field over every GuiContainer, including GuiContainerCreative. Give an explicitly
         * focused NEI field first refusal or the Creative branch below will send the character to Minecraft's own
         * search field and return before NEI ever sees it.
         */
        if (neiFocusedTextDispatcher != null && neiFocusedTextDispatcher.dispatch(screen, typedCharacter, keyCode)) {
            return;
        }
        if (screen instanceof GuiContainerCreative) {
            dispatchCreativeText((GuiContainerCreative) screen, typedCharacter, keyCode);
            return;
        }

        GuiTextField focusedTextField = findFocusedTextField(screen);
        String previousText = focusedTextField == null ? null : focusedTextField.getText();
        if (neiKeyboardDispatcher != null && neiKeyboardDispatcher.dispatch(screen, typedCharacter, keyCode)) {
            insertIfScreenIgnoredCharacter(focusedTextField, previousText, typedCharacter);
            return;
        }

        dispatchToScreen(screen, typedCharacter, keyCode);
        insertIfScreenIgnoredCharacter(focusedTextField, previousText, typedCharacter);
    }

    private void dispatchToScreen(GuiScreen screen, char typedCharacter, int keyCode) {
        ((GuiScreenControllerKeyDispatcher) (Object) screen).gtnhcontroller$dispatchKeyTyped(typedCharacter, keyCode);
    }

    private void dispatchCreativeText(GuiContainerCreative screen, char typedCharacter, int keyCode) {
        GuiContainerCreativeControllerAccessor accessor = (GuiContainerCreativeControllerAccessor) (Object) screen;
        GuiTextField searchField = accessor.gtnhcontroller$getSearchField();
        String previousText = searchField == null ? null : searchField.getText();

        dispatchToScreen(screen, typedCharacter, keyCode);
        if (searchField != null && searchField.isFocused()
            && isPrintable(typedCharacter)
            && previousText.equals(searchField.getText())) {
            searchField.writeText(String.valueOf(typedCharacter));
            accessor.gtnhcontroller$updateCreativeSearch();
        }
    }

    private GuiTextField findFocusedTextField(GuiScreen screen) {
        for (Class<?> type = screen.getClass(); type != null
            && GuiScreen.class.isAssignableFrom(type); type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!GuiTextField.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(screen);
                    if (value instanceof GuiTextField && ((GuiTextField) value).isFocused()) {
                        return (GuiTextField) value;
                    }
                } catch (IllegalAccessException exception) {
                    GTNHController.LOG.warn("Could not access focused text field {}", field.getName(), exception);
                }
            }
        }
        return null;
    }

    private static boolean isPrintable(char typedCharacter) {
        return typedCharacter != '\0' && !Character.isISOControl(typedCharacter);
    }

    private static void insertIfScreenIgnoredCharacter(GuiTextField textField, String previousText,
        char typedCharacter) {
        if (textField != null && isPrintable(typedCharacter) && previousText.equals(textField.getText())) {
            textField.writeText(String.valueOf(typedCharacter));
        }
    }

    private boolean scrollCreativeInventory(GuiContainerCreative screen, int direction) {
        GuiContainerCreativeControllerAccessor accessor = (GuiContainerCreativeControllerAccessor) (Object) screen;
        if (!accessor.gtnhcontroller$needsScrollBars()
            || !(screen.inventorySlots instanceof CreativeContainerControllerAccessor)) {
            return true;
        }

        CreativeContainerControllerAccessor container = (CreativeContainerControllerAccessor) screen.inventorySlots;
        float scrollPosition = CreativeInventoryScroll.nextPosition(
            accessor.gtnhcontroller$getCurrentScroll(),
            container.gtnhcontroller$getItemList()
                .size(),
            direction);
        accessor.gtnhcontroller$setCurrentScroll(scrollPosition);
        container.gtnhcontroller$scrollTo(scrollPosition);
        return true;
    }

    private void clickWorldList(GuiSelectWorld screen, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        GuiSlot worldList = findGuiSlot(screen);
        if (worldList == null) {
            GTNHController.LOG.warn(
                "Could not locate the world list on {}",
                screen.getClass()
                    .getName());
            return;
        }

        int worldIndex = worldList.func_148124_c(mouseX, mouseY);
        if (worldIndex < 0) {
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        boolean doubleClick = worldList == lastWorldList && worldIndex == lastWorldIndex
            && mouseButton == lastWorldButton
            && currentTimeMillis - lastWorldClickMillis < DOUBLE_CLICK_MILLIS;
        ((GuiSlotControllerAccessor) (Object) worldList)
            .gtnhcontroller$elementClicked(worldIndex, doubleClick, mouseX, mouseY);

        lastWorldList = worldList;
        lastWorldIndex = worldIndex;
        lastWorldButton = mouseButton;
        lastWorldClickMillis = currentTimeMillis;
    }

    private GuiSlot findGuiSlot(GuiScreen screen) {
        for (Class<?> type = screen.getClass(); type != null
            && GuiScreen.class.isAssignableFrom(type); type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!GuiSlot.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(screen);
                    if (value instanceof GuiSlot) {
                        return (GuiSlot) value;
                    }
                } catch (IllegalAccessException exception) {
                    GTNHController.LOG.warn("Could not access world-list field {}", field.getName(), exception);
                }
            }
        }
        return null;
    }

    private boolean invokeBetterQuesting(GuiScreen screen, String methodName, int mouseX, int mouseY, int mouseButton) {
        if (!isBetterQuestingCanvas(screen.getClass())) {
            return false;
        }

        try {
            Method method = screen.getClass()
                .getMethod(methodName, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            method.invoke(screen, mouseX, mouseY, mouseButton);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            GTNHController.LOG.warn(
                "Could not dispatch {} to {}",
                methodName,
                screen.getClass()
                    .getName(),
                exception);
        } catch (InvocationTargetException exception) {
            GTNHController.LOG.warn("BetterQuesting rejected controller {}", methodName, exception.getCause());
        }
        return false;
    }

    private boolean isBetterQuestingCanvas(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            String className = current.getName();
            if (BETTER_QUESTING_SCREEN.equals(className) || BETTER_QUESTING_CONTAINER.equals(className)) {
                return true;
            }
        }
        return false;
    }

    private static final class NeiFocusedTextDispatcher {

        private static final String LAYOUT_MANAGER_CLASS = "codechicken.nei.LayoutManager";
        private static final String TEXT_FIELD_CLASS = "codechicken.nei.TextField";
        private static final String WIDGET_CLASS = "codechicken.nei.Widget";

        private final Method getInputFocusedMethod;
        private final Field searchField;
        private final Method focusedMethod;
        private final Method textMethod;
        private final Method handleKeyPressMethod;
        private final Field internalTextField;
        private final Method onTextChangeMethod;
        private boolean unavailable;

        private NeiFocusedTextDispatcher(Method getInputFocusedMethod, Field searchField, Method focusedMethod,
            Method textMethod, Method handleKeyPressMethod, Field internalTextField, Method onTextChangeMethod) {
            this.getInputFocusedMethod = getInputFocusedMethod;
            this.searchField = searchField;
            this.focusedMethod = focusedMethod;
            this.textMethod = textMethod;
            this.handleKeyPressMethod = handleKeyPressMethod;
            this.internalTextField = internalTextField;
            this.onTextChangeMethod = onTextChangeMethod;
        }

        static NeiFocusedTextDispatcher create() {
            try {
                ClassLoader classLoader = GuiInputCompatibility.class.getClassLoader();
                Class<?> layoutManagerClass = Class.forName(LAYOUT_MANAGER_CLASS, false, classLoader);
                Class<?> textFieldClass = Class.forName(TEXT_FIELD_CLASS, false, classLoader);
                Class<?> widgetClass = Class.forName(WIDGET_CLASS, false, classLoader);
                Method getInputFocusedMethod = findPublicMethod(layoutManagerClass, "getInputFocused");
                Field searchField = findPublicField(layoutManagerClass, "searchField");
                if (getInputFocusedMethod == null && searchField == null) {
                    GTNHController.LOG.warn("NEI is present but exposes no focused text widget");
                    return null;
                }
                Method focusedMethod = textFieldClass.getMethod("focused");
                Method textMethod = textFieldClass.getMethod("text");
                Method handleKeyPressMethod = widgetClass.getMethod("handleKeyPress", Integer.TYPE, Character.TYPE);
                Field internalTextField = textFieldClass.getDeclaredField("field");
                internalTextField.setAccessible(true);
                Method onTextChangeMethod = textFieldClass.getMethod("onTextChange", String.class);
                return new NeiFocusedTextDispatcher(
                    getInputFocusedMethod,
                    searchField,
                    focusedMethod,
                    textMethod,
                    handleKeyPressMethod,
                    internalTextField,
                    onTextChangeMethod);
            } catch (ClassNotFoundException exception) {
                return null;
            } catch (NoSuchFieldException | NoSuchMethodException | LinkageError exception) {
                GTNHController.LOG.warn("NEI is present but its focused text input bridge is incompatible", exception);
                return null;
            }
        }

        boolean dispatch(GuiScreen screen, char typedCharacter, int keyCode) {
            if (unavailable || !(screen instanceof GuiContainer)) {
                return false;
            }

            try {
                Object focusedWidget = null;
                if (searchField != null) {
                    Object candidate = searchField.get(null);
                    if (candidate != null && Boolean.TRUE.equals(focusedMethod.invoke(candidate))) {
                        focusedWidget = candidate;
                    }
                }
                if (focusedWidget == null && getInputFocusedMethod != null) {
                    focusedWidget = getInputFocusedMethod.invoke(null);
                }
                if (focusedWidget == null) {
                    return false;
                }

                String previousText = (String) textMethod.invoke(focusedWidget);
                boolean handled = Boolean.TRUE.equals(
                    handleKeyPressMethod
                        .invoke(focusedWidget, Integer.valueOf(keyCode), Character.valueOf(typedCharacter)));
                String currentText = (String) textMethod.invoke(focusedWidget);
                if (previousText.equals(currentText)) {
                    Object internalField = internalTextField.get(focusedWidget);
                    if (internalField instanceof GuiTextField) {
                        GuiTextField guiTextField = (GuiTextField) internalField;
                        if (isPrintable(typedCharacter)) {
                            guiTextField.writeText(String.valueOf(typedCharacter));
                        } else if (keyCode == Keyboard.KEY_BACK) {
                            guiTextField.deleteFromCursor(-1);
                        }
                        currentText = (String) textMethod.invoke(focusedWidget);
                        if (!previousText.equals(currentText)) {
                            onTextChangeMethod.invoke(focusedWidget, previousText);
                            handled = true;
                        }
                    }
                }
                return handled;
            } catch (IllegalAccessException exception) {
                unavailable = true;
                GTNHController.LOG.warn("Could not access NEI's focused text input bridge", exception);
            } catch (InvocationTargetException exception) {
                unavailable = true;
                GTNHController.LOG.warn("NEI rejected focused controller text input", exception.getCause());
            }
            return false;
        }

        private static Method findPublicMethod(Class<?> type, String methodName) {
            try {
                return type.getMethod(methodName);
            } catch (NoSuchMethodException exception) {
                return null;
            }
        }

        private static Field findPublicField(Class<?> type, String fieldName) {
            try {
                return type.getField(fieldName);
            } catch (NoSuchFieldException exception) {
                return null;
            }
        }
    }

    private static final class NeiKeyboardDispatcher {

        private static final String MANAGER_CLASS = "codechicken.nei.guihook.GuiContainerManager";

        private final Method getManagerMethod;
        private final Method keyTypedMethod;
        private boolean unavailable;

        private NeiKeyboardDispatcher(Method getManagerMethod, Method keyTypedMethod) {
            this.getManagerMethod = getManagerMethod;
            this.keyTypedMethod = keyTypedMethod;
        }

        static NeiKeyboardDispatcher create() {
            try {
                Class<?> managerClass = Class
                    .forName(MANAGER_CLASS, false, GuiInputCompatibility.class.getClassLoader());
                Method getManagerMethod = managerClass.getMethod("getManager", GuiContainer.class);
                Method keyTypedMethod = managerClass.getMethod("keyTyped", Character.TYPE, Integer.TYPE);
                return new NeiKeyboardDispatcher(getManagerMethod, keyTypedMethod);
            } catch (ClassNotFoundException exception) {
                return null;
            } catch (NoSuchMethodException | LinkageError exception) {
                GTNHController.LOG.warn("NEI is present but its keyboard input bridge is incompatible", exception);
                return null;
            }
        }

        boolean dispatch(GuiScreen screen, char typedCharacter, int keyCode) {
            if (unavailable || !(screen instanceof GuiContainer)) {
                return false;
            }

            try {
                Object manager = getManagerMethod.invoke(null, screen);
                if (manager == null) {
                    return false;
                }
                keyTypedMethod.invoke(manager, Character.valueOf(typedCharacter), Integer.valueOf(keyCode));
                return true;
            } catch (IllegalAccessException exception) {
                unavailable = true;
                GTNHController.LOG.warn("Could not access NEI's keyboard input bridge", exception);
            } catch (InvocationTargetException exception) {
                unavailable = true;
                GTNHController.LOG.warn("NEI rejected controller keyboard input", exception.getCause());
            }
            return false;
        }
    }
}
