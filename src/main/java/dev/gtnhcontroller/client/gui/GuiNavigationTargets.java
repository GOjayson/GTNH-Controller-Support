package dev.gtnhcontroller.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import dev.gtnhcontroller.mixins.GuiContainerControllerAccessor;
import dev.gtnhcontroller.mixins.GuiScreenControllerAccessor;

final class GuiNavigationTargets {

    private GuiNavigationTargets() {}

    static List<GuiNavigationTarget> collect(GuiScreen screen, boolean includeButtons, boolean includeSlots) {
        List<GuiNavigationTarget> targets = new ArrayList<GuiNavigationTarget>();
        if (includeButtons) {
            addButtons(screen, targets);
        }
        if (includeSlots && screen instanceof GuiContainer) {
            addSlots((GuiContainer) screen, targets);
        }
        return targets;
    }

    private static void addButtons(GuiScreen screen, List<GuiNavigationTarget> targets) {
        GuiScreenControllerAccessor accessor = (GuiScreenControllerAccessor) (Object) screen;
        for (Object entry : accessor.gtnhcontroller$getButtonList()) {
            if (!(entry instanceof GuiButton)) {
                continue;
            }

            GuiButton button = (GuiButton) entry;
            if (button.visible && button.enabled) {
                addIfOnScreen(
                    screen,
                    targets,
                    button.xPosition + button.width / 2,
                    button.yPosition + button.height / 2);
            }
        }
    }

    private static void addSlots(GuiContainer screen, List<GuiNavigationTarget> targets) {
        GuiContainerControllerAccessor accessor = (GuiContainerControllerAccessor) (Object) screen;
        int guiLeft = accessor.gtnhcontroller$getGuiLeft();
        int guiTop = accessor.gtnhcontroller$getGuiTop();

        for (Object entry : screen.inventorySlots.inventorySlots) {
            if (!(entry instanceof Slot)) {
                continue;
            }

            Slot slot = (Slot) entry;
            if (!slot.func_111238_b()) {
                continue;
            }
            addIfOnScreen(
                screen,
                targets,
                guiLeft + slot.xDisplayPosition + 8,
                guiTop + slot.yDisplayPosition + 8);
        }
    }

    private static void addIfOnScreen(GuiScreen screen, List<GuiNavigationTarget> targets, int x, int y) {
        if (x >= 0 && x < screen.width && y >= 0 && y < screen.height) {
            targets.add(new GuiNavigationTarget(x, y));
        }
    }
}
