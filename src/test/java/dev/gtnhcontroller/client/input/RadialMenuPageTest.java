package dev.gtnhcontroller.client.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RadialMenuPageTest {

    @Test
    public void shouldersSelectTheirOwnPages() {
        assertEquals(RadialMenuPage.LEFT_SHOULDER, RadialMenuPage.select(true, false));
        assertEquals(RadialMenuPage.RIGHT_SHOULDER, RadialMenuPage.select(false, true));
    }

    @Test
    public void noShoulderOrBothShouldersSelectBasePage() {
        assertEquals(RadialMenuPage.BASE, RadialMenuPage.select(false, false));
        assertEquals(RadialMenuPage.BASE, RadialMenuPage.select(true, true));
    }
}
