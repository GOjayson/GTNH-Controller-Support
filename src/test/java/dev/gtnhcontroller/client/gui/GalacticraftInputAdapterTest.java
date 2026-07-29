package dev.gtnhcontroller.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GalacticraftInputAdapterTest {

    private static final float DELTA = 0.0001F;

    @Test
    public void overviewZoomUsesGalacticraftsScaleDependentStep() {
        assertEquals(0.2F, GalacticraftInputAdapter.nextOverviewZoom(0.0F, 1), DELTA);
        assertEquals(-0.2F, GalacticraftInputAdapter.nextOverviewZoom(0.0F, -1), DELTA);
    }

    @Test
    public void overviewZoomStaysWithinGalacticraftsLimits() {
        assertEquals(3.0F, GalacticraftInputAdapter.nextOverviewZoom(3.0F, 1), DELTA);
        assertEquals(-1.0F, GalacticraftInputAdapter.nextOverviewZoom(-1.0F, -1), DELTA);
    }

    @Test
    public void selectedPlanetZoomStaysWithinGalacticraftsLimits() {
        assertEquals(1.0F, GalacticraftInputAdapter.nextPlanetZoom(0.0F, 1), DELTA);
        assertEquals(-1.0F, GalacticraftInputAdapter.nextPlanetZoom(0.0F, -1), DELTA);
        assertEquals(5.0F, GalacticraftInputAdapter.nextPlanetZoom(5.0F, 1), DELTA);
        assertEquals(-4.9F, GalacticraftInputAdapter.nextPlanetZoom(-4.9F, -1), DELTA);
    }
}
