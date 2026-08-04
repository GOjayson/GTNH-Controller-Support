package dev.gtnhcontroller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigMigrationTest {

    @Test
    public void movesTheOldKeyboardDefaultWhenQuickMoveIsFirstIntroduced() {
        assertEquals("BUTTON:BACK", Config.migratedGuiKeyboardBinding(false, "BUTTON:NORTH"));
    }

    @Test
    public void preservesAnExplicitKeyboardBinding() {
        assertEquals("BUTTON:MISC1", Config.migratedGuiKeyboardBinding(false, "BUTTON:MISC1"));
    }

    @Test
    public void doesNotRepeatMigrationOnceQuickMoveExists() {
        assertEquals("BUTTON:NORTH", Config.migratedGuiKeyboardBinding(true, "BUTTON:NORTH"));
    }
}
