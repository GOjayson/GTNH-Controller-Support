package dev.gtnhcontroller.client.gui;

final class OnScreenKeyboardLayout {

    enum Command {
        CHARACTER,
        CAPS,
        SYMBOLS,
        SPACE,
        BACKSPACE,
        ENTER,
        DONE
    }

    static final class Key {

        private final String label;
        private final char normalCharacter;
        private final char shiftedCharacter;
        private final Command command;
        private final float widthWeight;

        private Key(String label, char normalCharacter, char shiftedCharacter, Command command, float widthWeight) {
            this.label = label;
            this.normalCharacter = normalCharacter;
            this.shiftedCharacter = shiftedCharacter;
            this.command = command;
            this.widthWeight = widthWeight;
        }

        String getLabel(boolean shifted) {
            if (command == Command.CHARACTER) {
                return String.valueOf(getCharacter(shifted));
            }
            if (command == Command.CAPS && shifted) {
                return label + "*";
            }
            return label;
        }

        char getCharacter(boolean shifted) {
            return shifted ? shiftedCharacter : normalCharacter;
        }

        Command getCommand() {
            return command;
        }

        float getWidthWeight() {
            return widthWeight;
        }
    }

    private static final Key[][] LETTERS = {
        characterKeys("1234567890"),
        characterKeys("qwertyuiop"),
        characterKeys("asdfghjkl"),
        characterKeys("zxcvbnm,.-"),
        {
            commandKey("Caps", Command.CAPS, 1.4F),
            commandKey("#+=", Command.SYMBOLS, 1.4F),
            commandKey("Space", Command.SPACE, 3.0F),
            commandKey("Back", Command.BACKSPACE, 1.5F),
            commandKey("Enter", Command.ENTER, 1.5F),
            commandKey("Done", Command.DONE, 1.4F) } };

    private static final Key[][] SYMBOLS = {
        characterKeys("!@#$%^&*"),
        characterKeys("()-_=+[]"),
        characterKeys("{}\\|;:'\""),
        characterKeys(",.<>?/`~"),
        {
            commandKey("Caps", Command.CAPS, 1.4F),
            commandKey("ABC", Command.SYMBOLS, 1.4F),
            commandKey("Space", Command.SPACE, 3.0F),
            commandKey("Back", Command.BACKSPACE, 1.5F),
            commandKey("Enter", Command.ENTER, 1.5F),
            commandKey("Done", Command.DONE, 1.4F) } };

    private OnScreenKeyboardLayout() {}

    static Key[][] getRows(boolean symbols) {
        return symbols ? SYMBOLS : LETTERS;
    }

    static boolean containsCharacter(char character) {
        return containsCharacter(LETTERS, character) || containsCharacter(SYMBOLS, character);
    }

    private static boolean containsCharacter(Key[][] rows, char character) {
        for (Key[] row : rows) {
            for (Key key : row) {
                if (key.command == Command.CHARACTER
                    && (key.normalCharacter == character || key.shiftedCharacter == character)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Key[] characterKeys(String characters) {
        Key[] keys = new Key[characters.length()];
        for (int index = 0; index < characters.length(); index++) {
            char character = characters.charAt(index);
            char shiftedCharacter = Character.isLetter(character) ? Character.toUpperCase(character) : character;
            keys[index] = new Key("", character, shiftedCharacter, Command.CHARACTER, 1.0F);
        }
        return keys;
    }

    private static Key commandKey(String label, Command command, float widthWeight) {
        return new Key(label, '\0', '\0', command, widthWeight);
    }
}
