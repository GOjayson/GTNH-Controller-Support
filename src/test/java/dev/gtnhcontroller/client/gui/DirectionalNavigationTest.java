package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class DirectionalNavigationTest {

    @Test
    public void selectsNearestTargetInRequestedDirection() {
        List<GuiNavigationTarget> targets = Arrays
            .asList(new GuiNavigationTarget(50, 20), new GuiNavigationTarget(50, 40), new GuiNavigationTarget(50, 80));

        GuiNavigationTarget result = DirectionalNavigation.findNext(50, 50, 0, -1, targets);

        assertEquals(50, result.x);
        assertEquals(40, result.y);
    }

    @Test
    public void prefersAlignedTargetOverCloserDiagonalTarget() {
        List<GuiNavigationTarget> targets = Arrays
            .asList(new GuiNavigationTarget(10, 20), new GuiNavigationTarget(30, 0));

        GuiNavigationTarget result = DirectionalNavigation.findNext(0, 0, 1, 0, targets);

        assertEquals(30, result.x);
        assertEquals(0, result.y);
    }

    @Test
    public void ignoresCurrentAndOppositeTargets() {
        List<GuiNavigationTarget> targets = Arrays
            .asList(new GuiNavigationTarget(20, 20), new GuiNavigationTarget(10, 20));

        assertNull(DirectionalNavigation.findNext(20, 20, 1, 0, targets));
    }
}
