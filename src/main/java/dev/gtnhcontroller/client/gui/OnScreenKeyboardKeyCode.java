package dev.gtnhcontroller.client.gui;

import org.lwjgl.input.Keyboard;

final class OnScreenKeyboardKeyCode {

    private OnScreenKeyboardKeyCode() {}

    static int forCharacter(char character) {
        switch (Character.toLowerCase(character)) {
            case 'a':
                return Keyboard.KEY_A;
            case 'b':
                return Keyboard.KEY_B;
            case 'c':
                return Keyboard.KEY_C;
            case 'd':
                return Keyboard.KEY_D;
            case 'e':
                return Keyboard.KEY_E;
            case 'f':
                return Keyboard.KEY_F;
            case 'g':
                return Keyboard.KEY_G;
            case 'h':
                return Keyboard.KEY_H;
            case 'i':
                return Keyboard.KEY_I;
            case 'j':
                return Keyboard.KEY_J;
            case 'k':
                return Keyboard.KEY_K;
            case 'l':
                return Keyboard.KEY_L;
            case 'm':
                return Keyboard.KEY_M;
            case 'n':
                return Keyboard.KEY_N;
            case 'o':
                return Keyboard.KEY_O;
            case 'p':
                return Keyboard.KEY_P;
            case 'q':
                return Keyboard.KEY_Q;
            case 'r':
                return Keyboard.KEY_R;
            case 's':
                return Keyboard.KEY_S;
            case 't':
                return Keyboard.KEY_T;
            case 'u':
                return Keyboard.KEY_U;
            case 'v':
                return Keyboard.KEY_V;
            case 'w':
                return Keyboard.KEY_W;
            case 'x':
                return Keyboard.KEY_X;
            case 'y':
                return Keyboard.KEY_Y;
            case 'z':
                return Keyboard.KEY_Z;
            default:
                return forNonLetter(character);
        }
    }

    private static int forNonLetter(char character) {
        switch (character) {
            case '0':
            case ')':
                return Keyboard.KEY_0;
            case '1':
            case '!':
                return Keyboard.KEY_1;
            case '2':
            case '@':
                return Keyboard.KEY_2;
            case '3':
            case '#':
                return Keyboard.KEY_3;
            case '4':
            case '$':
                return Keyboard.KEY_4;
            case '5':
            case '%':
                return Keyboard.KEY_5;
            case '6':
            case '^':
                return Keyboard.KEY_6;
            case '7':
            case '&':
                return Keyboard.KEY_7;
            case '8':
            case '*':
                return Keyboard.KEY_8;
            case '9':
            case '(':
                return Keyboard.KEY_9;
            case '-':
            case '_':
                return Keyboard.KEY_MINUS;
            case '=':
            case '+':
                return Keyboard.KEY_EQUALS;
            case '[':
            case '{':
                return Keyboard.KEY_LBRACKET;
            case ']':
            case '}':
                return Keyboard.KEY_RBRACKET;
            case '\\':
            case '|':
                return Keyboard.KEY_BACKSLASH;
            case ';':
            case ':':
                return Keyboard.KEY_SEMICOLON;
            case '\'':
            case '"':
                return Keyboard.KEY_APOSTROPHE;
            case ',':
            case '<':
                return Keyboard.KEY_COMMA;
            case '.':
            case '>':
                return Keyboard.KEY_PERIOD;
            case '/':
            case '?':
                return Keyboard.KEY_SLASH;
            case '`':
            case '~':
                return Keyboard.KEY_GRAVE;
            default:
                return Keyboard.KEY_NONE;
        }
    }
}
