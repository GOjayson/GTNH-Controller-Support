package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

public class OnScreenKeyboardLayoutTest {

    @Test
    public void containsCharactersNeededForSearchChatNamesAndAddresses() {
        String requiredCharacters =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                + "!@#$%^&*()-_=+[]{}\\|;:'\",.<>?/`~";

        for (int index = 0; index < requiredCharacters.length(); index++) {
            char character = requiredCharacters.charAt(index);
            assertTrue("Missing keyboard character: " + character, OnScreenKeyboardLayout.containsCharacter(character));
        }
    }

    @Test
    public void capsChangesLettersWithoutChangingDigits() {
        OnScreenKeyboardLayout.Key[][] rows = OnScreenKeyboardLayout.getRows(false);

        assertEquals('q', rows[1][0].getCharacter(false));
        assertEquals('Q', rows[1][0].getCharacter(true));
        assertEquals('1', rows[0][0].getCharacter(false));
        assertEquals('1', rows[0][0].getCharacter(true));
    }

    @Test
    public void exposesEveryTextEditingCommand() {
        Set<OnScreenKeyboardLayout.Command> commands =
            EnumSet.noneOf(OnScreenKeyboardLayout.Command.class);
        for (OnScreenKeyboardLayout.Key[] row : OnScreenKeyboardLayout.getRows(false)) {
            for (OnScreenKeyboardLayout.Key key : row) {
                commands.add(key.getCommand());
            }
        }

        assertTrue(commands.contains(OnScreenKeyboardLayout.Command.CAPS));
        assertTrue(commands.contains(OnScreenKeyboardLayout.Command.SYMBOLS));
        assertTrue(commands.contains(OnScreenKeyboardLayout.Command.SPACE));
        assertTrue(commands.contains(OnScreenKeyboardLayout.Command.BACKSPACE));
        assertTrue(commands.contains(OnScreenKeyboardLayout.Command.ENTER));
        assertTrue(commands.contains(OnScreenKeyboardLayout.Command.DONE));
    }
}
