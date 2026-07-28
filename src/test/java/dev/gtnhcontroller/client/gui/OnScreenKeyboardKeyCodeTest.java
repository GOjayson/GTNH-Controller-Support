package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.lwjgl.input.Keyboard;

import org.junit.Test;

public class OnScreenKeyboardKeyCodeTest {

    @Test
    public void everyPrintableKeyboardCharacterHasARealKeyCode() {
        for (boolean symbols : new boolean[] { false, true }) {
            for (OnScreenKeyboardLayout.Key[] row : OnScreenKeyboardLayout.getRows(symbols)) {
                for (OnScreenKeyboardLayout.Key key : row) {
                    if (key.getCommand() != OnScreenKeyboardLayout.Command.CHARACTER) {
                        continue;
                    }

                    assertNotEquals(Keyboard.KEY_NONE, OnScreenKeyboardKeyCode.forCharacter(key.getCharacter(false)));
                    assertNotEquals(Keyboard.KEY_NONE, OnScreenKeyboardKeyCode.forCharacter(key.getCharacter(true)));
                }
            }
        }
    }

    @Test
    public void shiftedCharactersUseTheirPhysicalBaseKey() {
        assertEquals(Keyboard.KEY_A, OnScreenKeyboardKeyCode.forCharacter('a'));
        assertEquals(Keyboard.KEY_A, OnScreenKeyboardKeyCode.forCharacter('A'));
        assertEquals(Keyboard.KEY_1, OnScreenKeyboardKeyCode.forCharacter('!'));
        assertEquals(Keyboard.KEY_MINUS, OnScreenKeyboardKeyCode.forCharacter('_'));
        assertEquals(Keyboard.KEY_APOSTROPHE, OnScreenKeyboardKeyCode.forCharacter('"'));
    }
}
