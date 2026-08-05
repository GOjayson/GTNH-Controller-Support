package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ControllerBindingTest {

    @Test
    public void parsesAlternativeButtonsAndTriggers() {
        ControllerBinding binding = ControllerBinding
            .parse("BUTTON:LEFT_PADDLE1 | TRIGGER:RIGHT_TRIGGER | BUTTON:SOUTH");

        assertEquals(2, binding.getButtonCount());
        assertEquals(1, binding.getTriggerCount());
    }

    @Test
    public void parsesMultiButtonAndTriggerChords() {
        ControllerBinding binding = ControllerBinding
            .parse("BUTTON:LEFT_SHOULDER+BUTTON:SOUTH|TRIGGER:LEFT_TRIGGER+BUTTON:NORTH");

        assertEquals(3, binding.getButtonCount());
        assertEquals(1, binding.getTriggerCount());
        assertEquals(2, binding.getAlternativeCount());
    }

    @Test
    public void everyDefaultActionBindingIsValid() {
        Set<String> configKeys = new HashSet<String>();
        for (ControllerAction action : ControllerAction.values()) {
            ControllerBinding.parse(action.defaultBinding);
            assertTrue(configKeys.add(action.configKey));
        }
    }

    @Test
    public void quickMoveAndKeyboardHaveSeparateDefaults() {
        ControllerBinding quickMove = ControllerBinding.parse(ControllerAction.GUI_QUICK_MOVE.defaultBinding);
        ControllerBinding keyboard = ControllerBinding.parse(ControllerAction.GUI_KEYBOARD.defaultBinding);

        assertFalse(quickMove.conflictsWith(keyboard));
        assertTrue(quickMove.conflictsWith(ControllerBinding.parse("BUTTON:NORTH")));
        assertTrue(keyboard.conflictsWith(ControllerBinding.parse("BUTTON:BACK")));
    }

    @Test
    public void noneCreatesAnEmptyBinding() {
        assertTrue(
            ControllerBinding.parse("NONE")
                .isEmpty());
    }

    @Test
    public void conflictsDetectSharedAlternativeInputs() {
        ControllerBinding first = ControllerBinding.parse("BUTTON:SOUTH|TRIGGER:LEFT_TRIGGER");

        assertTrue(first.conflictsWith(ControllerBinding.parse("BUTTON:SOUTH")));
        assertTrue(first.conflictsWith(ControllerBinding.parse("TRIGGER:LEFT_TRIGGER")));
        assertFalse(first.conflictsWith(ControllerBinding.parse("BUTTON:NORTH")));
        assertFalse(first.conflictsWith(ControllerBinding.parse("NONE")));
    }

    @Test
    public void componentButtonDoesNotConflictWithAChord() {
        ControllerBinding plain = ControllerBinding.parse("BUTTON:SOUTH");
        ControllerBinding chord = ControllerBinding.parse("BUTTON:LEFT_SHOULDER+BUTTON:SOUTH");

        assertFalse(plain.conflictsWith(chord));
        assertTrue(chord.conflictsWith(ControllerBinding.parse("BUTTON:SOUTH+BUTTON:LEFT_SHOULDER")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyChordParts() {
        ControllerBinding.parse("BUTTON:SOUTH+");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsStickAxesAsTriggerBindings() {
        ControllerBinding.parse("TRIGGER:LEFT_X");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownButtons() {
        ControllerBinding.parse("BUTTON:NOT_A_REAL_BUTTON");
    }
}
