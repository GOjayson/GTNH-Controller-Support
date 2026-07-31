package dev.gtnhcontroller.client.input;

public final class ChatMacro {

    public static final String RADIAL_IDENTIFIER_PREFIX = "CHAT_MACRO:";
    public static final int MAX_NAME_LENGTH = 40;
    public static final int MAX_MESSAGE_LENGTH = 100;

    private final String id;
    private final String name;
    private final String message;

    public ChatMacro(String id, String name, String message) {
        this.id = validateId(id);
        this.name = validateName(name);
        this.message = validateMessage(message);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getRadialIdentifier() {
        return RADIAL_IDENTIFIER_PREFIX + id;
    }

    public static String idFromRadialIdentifier(String identifier) {
        if (identifier == null || !identifier.startsWith(RADIAL_IDENTIFIER_PREFIX)) {
            return null;
        }
        String id = identifier.substring(RADIAL_IDENTIFIER_PREFIX.length());
        return isValidId(id) ? id : null;
    }

    private static String validateId(String value) {
        if (!isValidId(value)) {
            throw new IllegalArgumentException(
                "Chat macro ID must contain only letters, numbers, underscores or dashes");
        }
        return value;
    }

    private static boolean isValidId(String value) {
        if (value == null || value.isEmpty() || value.length() > 64) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (!Character.isLetterOrDigit(character) && character != '_' && character != '-') {
                return false;
            }
        }
        return true;
    }

    private static String validateName(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Enter a macro name");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Macro names are limited to " + MAX_NAME_LENGTH + " characters");
        }
        validateSingleLine(trimmed, "Macro names");
        return trimmed;
    }

    private static String validateMessage(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Enter a chat message or command");
        }
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Chat messages are limited to " + MAX_MESSAGE_LENGTH + " characters");
        }
        validateSingleLine(trimmed, "Chat messages");
        return trimmed;
    }

    private static void validateSingleLine(String value, String label) {
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '\r' || character == '\n' || Character.isISOControl(character)) {
                throw new IllegalArgumentException(label + " must contain one printable line");
            }
        }
    }
}
