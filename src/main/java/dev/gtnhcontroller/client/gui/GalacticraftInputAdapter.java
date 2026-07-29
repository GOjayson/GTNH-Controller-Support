package dev.gtnhcontroller.client.gui;

import java.lang.reflect.Field;

import net.minecraft.client.gui.GuiScreen;

import dev.gtnhcontroller.GTNHController;

/**
 * Sends controller shoulder scrolling to Galacticraft's celestial-map zoom fields.
 */
final class GalacticraftInputAdapter {

    private static final String CELESTIAL_SELECTION_CLASS = "micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection";

    private Fields fields;
    private boolean unavailable;

    boolean scroll(GuiScreen screen, int direction) {
        if (unavailable || !isCelestialSelection(screen)) {
            return false;
        }

        Fields resolvedFields = fields(screen);
        if (resolvedFields == null) {
            return false;
        }

        try {
            int wheel = direction < 0 ? 1 : -1;
            Object selectedBody = resolvedFields.selectedBody.get(screen);
            Object selectionState = resolvedFields.selectionState.get(screen);
            int selectionCount = resolvedFields.selectionCount.getInt(screen);
            boolean preview = selectionState instanceof Enum<?> && "PREVIEW".equals(((Enum<?>) selectionState).name());

            if (selectedBody == null || preview && selectionCount < 2) {
                float zoom = resolvedFields.zoom.getFloat(screen);
                resolvedFields.zoom.setFloat(screen, nextOverviewZoom(zoom, wheel));
            } else {
                float planetZoom = resolvedFields.planetZoom.getFloat(screen);
                resolvedFields.planetZoom.setFloat(screen, nextPlanetZoom(planetZoom, wheel));
            }
            return true;
        } catch (IllegalAccessException exception) {
            unavailable = true;
            GTNHController.LOG
                .warn("Galacticraft celestial-map controller zoom is incompatible with this version", exception);
            return false;
        }
    }

    private Fields fields(GuiScreen screen) {
        if (fields != null) {
            return fields;
        }

        try {
            fields = new Fields(
                findField(screen.getClass(), "zoom"),
                findField(screen.getClass(), "planetZoom"),
                findField(screen.getClass(), "selectedBody"),
                findField(screen.getClass(), "selectionState"),
                findField(screen.getClass(), "selectionCount"));
            return fields;
        } catch (NoSuchFieldException exception) {
            unavailable = true;
            GTNHController.LOG
                .warn("Galacticraft celestial-map controller zoom is incompatible with this version", exception);
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
                // Keep searching through Galacticraft's GUI base classes.
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static boolean isCelestialSelection(GuiScreen screen) {
        for (Class<?> current = screen.getClass(); current != null; current = current.getSuperclass()) {
            if (CELESTIAL_SELECTION_CLASS.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }

    static float nextOverviewZoom(float zoom, int wheel) {
        return clamp(zoom + wheel * (zoom + 2.0F) / 10.0F, -1.0F, 3.0F);
    }

    static float nextPlanetZoom(float planetZoom, int wheel) {
        return clamp(planetZoom + wheel, -4.9F, 5.0F);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }

    private static final class Fields {

        final Field zoom;
        final Field planetZoom;
        final Field selectedBody;
        final Field selectionState;
        final Field selectionCount;

        Fields(Field zoom, Field planetZoom, Field selectedBody, Field selectionState, Field selectionCount) {
            this.zoom = zoom;
            this.planetZoom = planetZoom;
            this.selectedBody = selectedBody;
            this.selectionState = selectionState;
            this.selectionCount = selectionCount;
        }
    }
}
