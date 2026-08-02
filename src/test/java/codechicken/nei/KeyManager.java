package codechicken.nei;

import java.util.HashMap;
import java.util.Map;

public final class KeyManager {

    public static final Map<String, KeyState> keyStates = new HashMap<String, KeyState>();

    private KeyManager() {}

    public static final class KeyState {

        public boolean down;
        public boolean held;
        public boolean up;
    }
}
