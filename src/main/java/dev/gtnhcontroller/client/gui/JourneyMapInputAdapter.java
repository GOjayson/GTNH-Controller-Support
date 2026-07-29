package dev.gtnhcontroller.client.gui;

import java.lang.reflect.Field;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import dev.gtnhcontroller.GTNHController;
import dev.gtnhcontroller.mixins.GuiScreenControllerAccessor;

/**
 * Adapts controller drag state to JourneyMap 5's fullscreen map.
 *
 * <p>
 * JourneyMap polls {@code Mouse.isButtonDown(0)} instead of using {@link GuiScreen#mouseClickMove}. A synthetic
 * controller click therefore reaches its buttons but never starts a map pan. These fields are JourneyMap's own drag
 * state; its normal release callback still performs and persists the actual map movement.
 */
final class JourneyMapInputAdapter {

    private static final String FULLSCREEN_CLASS = "journeymap.client.ui.fullscreen.Fullscreen";

    private GuiScreen dragScreen;
    private Fields fields;
    private int dragStartX;
    private int dragStartY;
    private boolean unavailable;

    void mousePressed(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        if (unavailable || mouseButton != 0 || !isFullscreen(screen) || isOverButton(screen, mouseX, mouseY)) {
            return;
        }

        Fields resolvedFields = fields(screen);
        if (resolvedFields == null) {
            return;
        }

        try {
            resolvedFields.mx.setInt(screen, mouseX);
            resolvedFields.my.setInt(screen, mouseY);
            resolvedFields.msx.setInt(screen, mouseX);
            resolvedFields.msy.setInt(screen, mouseY);
            dragScreen = screen;
            dragStartX = mouseX;
            dragStartY = mouseY;
        } catch (IllegalAccessException exception) {
            disable(exception);
        }
    }

    void mouseDragged(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 || screen != dragScreen || fields == null) {
            return;
        }

        try {
            if (mouseX != dragStartX || mouseY != dragStartY) {
                fields.isScrolling.set(screen, Boolean.TRUE);
            }
            fields.mx.setInt(screen, mouseX);
            fields.my.setInt(screen, mouseY);
        } catch (IllegalAccessException exception) {
            disable(exception);
        }
    }

    void beforeMouseReleased(GuiScreen screen, int mouseX, int mouseY, int mouseButton) {
        mouseDragged(screen, mouseX, mouseY, mouseButton);
    }

    void mouseReleased(GuiScreen screen, int mouseButton) {
        if (mouseButton == 0 && screen == dragScreen) {
            finishReleaseIfJourneyMapSkippedIt(screen);
            dragScreen = null;
        }
    }

    private void finishReleaseIfJourneyMapSkippedIt(GuiScreen screen) {
        try {
            if (!Boolean.TRUE.equals(fields.isScrolling.get(screen))) {
                return;
            }

            int safeX = screen.width / 2;
            int safeY = screen.height / 2;
            if (isOverButton(screen, safeX, safeY)) {
                safeX = 0;
                safeY = Math.max(screen.height - 1, 0);
            }
            ((GuiScreenControllerAccessor) (Object) screen).gtnhcontroller$mouseMovedOrUp(safeX, safeY, 0);
            if (Boolean.TRUE.equals(fields.isScrolling.get(screen))) {
                fields.isScrolling.set(screen, Boolean.FALSE);
            }
        } catch (IllegalAccessException exception) {
            disable(exception);
        }
    }

    private Fields fields(GuiScreen screen) {
        if (fields != null) {
            return fields;
        }

        try {
            fields = new Fields(
                findField(screen.getClass(), "isScrolling"),
                findField(screen.getClass(), "msx"),
                findField(screen.getClass(), "msy"),
                findField(screen.getClass(), "mx"),
                findField(screen.getClass(), "my"));
            return fields;
        } catch (NoSuchFieldException exception) {
            disable(exception);
            return null;
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException exception) {
                // Keep searching through JourneyMap's GUI base classes.
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static boolean isFullscreen(GuiScreen screen) {
        for (Class<?> current = screen.getClass(); current != null; current = current.getSuperclass()) {
            if (FULLSCREEN_CLASS.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOverButton(GuiScreen screen, int mouseX, int mouseY) {
        GuiScreenControllerAccessor accessor = (GuiScreenControllerAccessor) (Object) screen;
        for (GuiButton button : accessor.gtnhcontroller$getButtonList()) {
            if (button.visible && button.enabled
                && mouseX >= button.xPosition
                && mouseX < button.xPosition + button.width
                && mouseY >= button.yPosition
                && mouseY < button.yPosition + button.height) {
                return true;
            }
        }
        return false;
    }

    private void disable(Exception exception) {
        unavailable = true;
        dragScreen = null;
        GTNHController.LOG
            .warn("JourneyMap fullscreen controller dragging is incompatible with this version", exception);
    }

    private static final class Fields {

        final Field isScrolling;
        final Field msx;
        final Field msy;
        final Field mx;
        final Field my;

        Fields(Field isScrolling, Field msx, Field msy, Field mx, Field my) {
            this.isScrolling = isScrolling;
            this.msx = msx;
            this.msy = msy;
            this.mx = mx;
            this.my = my;
        }
    }
}
