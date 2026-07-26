package dev.gtnhcontroller.client.gui;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import dev.gtnhcontroller.GTNHController;

/**
 * Hides the operating-system cursor while the controller owns GUI input, without grabbing the mouse. Keeping it
 * ungrabbed allows the native position to mirror the virtual cursor and still lets physical mouse movement take over.
 */
final class NativeCursorManager {

    private Cursor transparentCursor;
    private Cursor previousCursor;
    private boolean hidden;
    private boolean unavailable;

    void hide() {
        if (hidden || unavailable || !Mouse.isCreated()) {
            return;
        }

        try {
            if (transparentCursor == null) {
                transparentCursor = createTransparentCursor();
            }
            previousCursor = Mouse.setNativeCursor(transparentCursor);
            hidden = true;
        } catch (LWJGLException | RuntimeException exception) {
            unavailable = true;
            GTNHController.LOG.warn(
                "Could not hide the native cursor; controller hover coordinates will still be synchronized.",
                exception);
        }
    }

    void restore() {
        if (!hidden) {
            return;
        }

        try {
            if (Mouse.isCreated()) {
                Mouse.setNativeCursor(previousCursor);
            }
        } catch (LWJGLException | RuntimeException exception) {
            GTNHController.LOG.warn("Could not restore the native cursor.", exception);
        } finally {
            previousCursor = null;
            hidden = false;
        }
    }

    private Cursor createTransparentCursor() throws LWJGLException {
        int capabilities = Cursor.getCapabilities();
        if ((capabilities & Cursor.CURSOR_ONE_BIT_TRANSPARENCY) == 0) {
            throw new LWJGLException("The platform does not support transparent native cursors");
        }

        int cursorSize = Math.max(Cursor.getMinCursorSize(), 1);
        IntBuffer pixels = BufferUtils.createIntBuffer(cursorSize * cursorSize);
        for (int pixel = 0; pixel < cursorSize * cursorSize; pixel++) {
            pixels.put(0x00000000);
        }
        pixels.flip();
        return new Cursor(cursorSize, cursorSize, 0, 0, 1, pixels, null);
    }
}
