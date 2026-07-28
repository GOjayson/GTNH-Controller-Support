package dev.gtnhcontroller.client.gui;

import java.util.List;

final class DirectionalNavigation {

    private static final float CROSS_AXIS_WEIGHT = 4.0F;

    private DirectionalNavigation() {}

    static GuiNavigationTarget findNext(int currentX, int currentY, int directionX, int directionY,
        List<GuiNavigationTarget> targets) {
        GuiNavigationTarget bestTarget = null;
        float bestScore = Float.MAX_VALUE;

        for (GuiNavigationTarget target : targets) {
            int deltaX = target.x - currentX;
            int deltaY = target.y - currentY;
            int forwardDistance = deltaX * directionX + deltaY * directionY;
            if (forwardDistance <= 0) {
                continue;
            }

            int crossDistance = deltaX * directionY - deltaY * directionX;
            float score = forwardDistance * forwardDistance
                + CROSS_AXIS_WEIGHT * crossDistance * crossDistance;
            if (score < bestScore) {
                bestTarget = target;
                bestScore = score;
            }
        }

        return bestTarget;
    }
}
