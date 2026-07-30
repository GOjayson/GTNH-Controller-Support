package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ControllerSelectionTest {

    @Test
    public void automaticSelectionAcceptsNullAndCaseDifferences() {
        assertTrue(ControllerSelection.isAutomatic(null));
        assertTrue(ControllerSelection.isAutomatic("auto"));
        assertFalse(ControllerSelection.isAutomatic("Xbox Controller"));
    }

    @Test
    public void selectionKeysDistinguishDuplicateControllers() {
        String first = ControllerSelection.createKey("8BitDo Ultimate", 1);
        String second = ControllerSelection.createKey("8BitDo Ultimate", 2);

        assertFalse(first.equals(second));
        assertEquals(first, ControllerSelection.createKey("8BitDo Ultimate", 1));
        assertTrue(ControllerSelection.isValid(first));
        assertFalse(ControllerSelection.isValid("not a selection key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidOccurrence() {
        ControllerSelection.createKey("Controller", 0);
    }
}
