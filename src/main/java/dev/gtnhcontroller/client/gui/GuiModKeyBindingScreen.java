package dev.gtnhcontroller.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.client.input.ControllerBindingLayer;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.RegisteredKeyBinding;
import dev.gtnhcontroller.client.input.SdlGamepadManager;

/**
 * Searchable controller bindings for every registered key action not already handled by the special gameplay page.
 */
public final class GuiModKeyBindingScreen extends GuiScreen
    implements ControllerConfigurationScreen, ControllerInputCaptureScreen {

    private static final int BINDING_BUTTON_BASE = 100;
    private static final int CLEAR_BUTTON_BASE = 200;
    private static final int CATEGORY = 300;
    private static final int PREVIOUS_PAGE = 301;
    private static final int NEXT_PAGE = 302;
    private static final int DONE = 303;
    private static final int TOGGLE_LAYER = 304;
    private static final float CAPTURE_TRIGGER_THRESHOLD = 0.50F;
    private static final int FIRST_ROW_Y = 70;
    private static final int ROW_HEIGHT = 22;

    private final GuiScreen parentScreen;
    private final SdlGamepadManager gamepadManager;
    private final ModKeyBindingController keyBindingController;
    private final List<RegisteredKeyBinding> allBindings = new ArrayList<RegisteredKeyBinding>();
    private final List<RegisteredKeyBinding> filteredBindings = new ArrayList<RegisteredKeyBinding>();
    private final List<Category> categories = new ArrayList<Category>();

    private GuiTextField searchField;
    private int categoryIndex;
    private int page;
    private ControllerBindingLayer bindingLayer = ControllerBindingLayer.PRIMARY;
    private RegisteredKeyBinding captureBinding;
    private final ControllerChordCapture chordCapture = new ControllerChordCapture();
    private boolean captureArmed;
    private boolean waitingForCapturedInputRelease;

    public GuiModKeyBindingScreen(GuiScreen parentScreen, SdlGamepadManager gamepadManager,
        ModKeyBindingController keyBindingController) {
        this.parentScreen = parentScreen;
        this.gamepadManager = gamepadManager;
        this.keyBindingController = keyBindingController;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        keyBindingController.refreshBindings();
        allBindings.clear();
        allBindings.addAll(keyBindingController.getRegisteredBindings());
        Collections.sort(allBindings, new Comparator<RegisteredKeyBinding>() {

            @Override
            public int compare(RegisteredKeyBinding first, RegisteredKeyBinding second) {
                int categoryComparison = first.getCategoryName()
                    .compareToIgnoreCase(second.getCategoryName());
                if (categoryComparison != 0) {
                    return categoryComparison;
                }
                int nameComparison = first.getDisplayName()
                    .compareToIgnoreCase(second.getDisplayName());
                return nameComparison != 0 ? nameComparison
                    : first.getIdentifier()
                        .compareTo(second.getIdentifier());
            }
        });

        String previousSearch = searchField == null ? "" : searchField.getText();
        searchField = new GuiTextField(fontRendererObj, width / 2 - 155, 43, 210, 18);
        searchField.setMaxStringLength(100);
        searchField.setText(previousSearch);
        rebuildCategories();
        filterBindings();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();

        if (waitingForCapturedInputRelease) {
            if (!gamepadManager.hasBindableInputDown(CAPTURE_TRIGGER_THRESHOLD)) {
                waitingForCapturedInputRelease = false;
                rebuildButtons();
            }
            return;
        }
        if (captureBinding == null) {
            return;
        }

        if (!captureArmed) {
            if (!gamepadManager.hasBindableInputDown(CAPTURE_TRIGGER_THRESHOLD)) {
                captureArmed = true;
                rebuildButtons();
            }
            return;
        }

        String capturedBinding = chordCapture.update(gamepadManager.getBindableInputsDown(CAPTURE_TRIGGER_THRESHOLD));
        if (capturedBinding != null) {
            applyBinding(capturedBinding);
        } else if (chordCapture.hasStarted()) {
            rebuildButtons();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int rowCount = visibleRowCount();
        if (button.id >= BINDING_BUTTON_BASE && button.id < BINDING_BUTTON_BASE + rowCount) {
            beginCapture(visibleBinding(button.id - BINDING_BUTTON_BASE));
        } else if (button.id >= CLEAR_BUTTON_BASE && button.id < CLEAR_BUTTON_BASE + rowCount) {
            RegisteredKeyBinding binding = visibleBinding(button.id - CLEAR_BUTTON_BASE);
            keyBindingController.setBinding(binding.getIdentifier(), "NONE", bindingLayer);
            waitForInputRelease();
        } else if (button.id == CATEGORY) {
            categoryIndex = (categoryIndex + 1) % categories.size();
            page = 0;
            filterBindings();
        } else if (button.id == PREVIOUS_PAGE && page > 0) {
            page--;
            rebuildButtons();
        } else if (button.id == NEXT_PAGE && page < pageCount() - 1) {
            page++;
            rebuildButtons();
        } else if (button.id == TOGGLE_LAYER) {
            bindingLayer = bindingLayer == ControllerBindingLayer.PRIMARY ? ControllerBindingLayer.MODIFIER
                : ControllerBindingLayer.PRIMARY;
            rebuildButtons();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
        if (captureBinding != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                waitForInputRelease();
            } else if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                applyBinding("NONE");
            }
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        if (searchField.textboxKeyTyped(typedCharacter, keyCode)) {
            page = 0;
            filterBindings();
            return;
        }
        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Minecraft & Mod Controller Bindings", width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, statusLine(), width / 2, 27, 0xA0A0A0);

        if (searchField.getText()
            .isEmpty() && !searchField.isFocused()) {
            drawString(fontRendererObj, "Search", searchField.xPosition + 4, searchField.yPosition + 5, 0x707070);
        }

        int firstBinding = page * rowsPerPage();
        int finalBinding = Math.min(firstBinding + rowsPerPage(), filteredBindings.size());
        for (int bindingIndex = firstBinding; bindingIndex < finalBinding; bindingIndex++) {
            int row = bindingIndex - firstBinding;
            RegisteredKeyBinding binding = filteredBindings.get(bindingIndex);
            String label = categories.get(categoryIndex).key == null
                ? binding.getCategoryName() + " / " + binding.getDisplayName()
                : binding.getDisplayName();
            String trimmedLabel = fontRendererObj.trimStringToWidth(label, Math.max(width / 2 - 115, 80));
            drawString(
                fontRendererObj,
                trimmedLabel,
                width / 2 - 45 - fontRendererObj.getStringWidth(trimmedLabel),
                FIRST_ROW_Y + 6 + row * ROW_HEIGHT,
                0xFFFFFF);
        }

        if (filteredBindings.isEmpty()) {
            drawCenteredString(fontRendererObj, "No matching registered key bindings", width / 2, 92, 0xA0A0A0);
        } else if (pageCount() > 1) {
            drawCenteredString(fontRendererObj, "Page " + (page + 1) + " / " + pageCount(), width / 2, 33, 0xA0A0A0);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        searchField.drawTextBox();
        drawConflictTooltip(mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public boolean isCapturingControllerInput() {
        return captureBinding != null || waitingForCapturedInputRelease;
    }

    private void rebuildCategories() {
        String selectedCategoryKey = categories.isEmpty() ? null : categories.get(categoryIndex).key;
        Set<Category> discoveredCategories = new LinkedHashSet<Category>();
        for (RegisteredKeyBinding binding : allBindings) {
            discoveredCategories.add(new Category(binding.getCategoryKey(), binding.getCategoryName()));
        }

        categories.clear();
        categories.add(new Category(null, "All categories"));
        categories.addAll(discoveredCategories);
        categoryIndex = 0;
        if (selectedCategoryKey != null) {
            for (int index = 1; index < categories.size(); index++) {
                if (selectedCategoryKey.equals(categories.get(index).key)) {
                    categoryIndex = index;
                    break;
                }
            }
        }
    }

    private void filterBindings() {
        filteredBindings.clear();
        Category category = categories.get(categoryIndex);
        String query = searchField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);
        for (RegisteredKeyBinding binding : allBindings) {
            if (category.key != null && !category.key.equals(binding.getCategoryKey())) {
                continue;
            }
            String searchableText = (binding.getCategoryName() + " "
                + binding.getDisplayName()
                + " "
                + binding.getCategoryKey()
                + " "
                + binding.getDescriptionKey()).toLowerCase(Locale.ROOT);
            if (query.isEmpty() || searchableText.contains(query)) {
                filteredBindings.add(binding);
            }
        }

        page = Math.min(page, pageCount() - 1);
        rebuildButtons();
    }

    private void rebuildButtons() {
        buttonList.clear();
        Category category = categories.get(categoryIndex);
        String categoryLabel = fontRendererObj.trimStringToWidth(category.name, 89);
        GuiButton categoryButton = new GuiButton(CATEGORY, width / 2 + 60, 42, 95, 20, categoryLabel);
        categoryButton.enabled = captureBinding == null && !waitingForCapturedInputRelease;
        buttonList.add(categoryButton);

        int firstBinding = page * rowsPerPage();
        int finalBinding = Math.min(firstBinding + rowsPerPage(), filteredBindings.size());
        for (int bindingIndex = firstBinding; bindingIndex < finalBinding; bindingIndex++) {
            int row = bindingIndex - firstBinding;
            RegisteredKeyBinding binding = filteredBindings.get(bindingIndex);
            String bindingLabel = captureBinding == binding
                ? (captureArmed ? "> " + chordCapture.displayValue() + " <" : "Release inputs")
                : formatBinding(binding);
            GuiButton bindingButton = new GuiButton(
                BINDING_BUTTON_BASE + row,
                width / 2 - 40,
                FIRST_ROW_Y + row * ROW_HEIGHT,
                140,
                20,
                bindingLabel);
            GuiButton clearButton = new GuiButton(
                CLEAR_BUTTON_BASE + row,
                width / 2 + 105,
                FIRST_ROW_Y + row * ROW_HEIGHT,
                50,
                20,
                "Clear");
            boolean rowEnabled = captureBinding == null && !waitingForCapturedInputRelease;
            bindingButton.enabled = rowEnabled || binding == captureBinding;
            clearButton.enabled = rowEnabled
                && !"NONE".equalsIgnoreCase(Config.getModKeyBinding(binding.getIdentifier(), bindingLayer));
            buttonList.add(bindingButton);
            buttonList.add(clearButton);
        }

        if (pageCount() > 1) {
            GuiButton previousButton = new GuiButton(PREVIOUS_PAGE, width / 2 - 155, height - 52, 70, 20, "< Previous");
            GuiButton nextButton = new GuiButton(NEXT_PAGE, width / 2 + 85, height - 52, 70, 20, "Next >");
            previousButton.enabled = page > 0 && captureBinding == null && !waitingForCapturedInputRelease;
            nextButton.enabled = page < pageCount() - 1 && captureBinding == null && !waitingForCapturedInputRelease;
            buttonList.add(previousButton);
            buttonList.add(nextButton);
        }
        GuiButton layerButton = new GuiButton(
            TOGGLE_LAYER,
            width / 2 - 75,
            height - 52,
            150,
            20,
            "Layer: " + bindingLayer.displayName);
        layerButton.enabled = captureBinding == null && !waitingForCapturedInputRelease;
        buttonList.add(layerButton);

        GuiButton doneButton = new GuiButton(DONE, width / 2 - 100, height - 28, 200, 20, "Done");
        doneButton.enabled = captureBinding == null && !waitingForCapturedInputRelease;
        buttonList.add(doneButton);
    }

    private String formatBinding(RegisteredKeyBinding binding) {
        String formatted = ControllerBindingDisplay
            .format(Config.getModKeyBinding(binding.getIdentifier(), bindingLayer));
        return keyBindingController.hasConflict(binding.getIdentifier(), bindingLayer) ? "\u00A7c! " + formatted
            : formatted;
    }

    private void drawConflictTooltip(int mouseX, int mouseY) {
        int row = (mouseY - FIRST_ROW_Y) / ROW_HEIGHT;
        if (mouseX < width / 2 - 40 || mouseX >= width / 2 + 100
            || mouseY < FIRST_ROW_Y
            || row < 0
            || row >= visibleRowCount()
            || mouseY >= FIRST_ROW_Y + row * ROW_HEIGHT + 20) {
            return;
        }
        List<String> conflicts = keyBindingController
            .getConflictNames(visibleBinding(row).getIdentifier(), bindingLayer);
        if (conflicts.isEmpty()) {
            return;
        }
        List<String> tooltip = new ArrayList<String>();
        tooltip.add("\u00A7cConflicts with:");
        tooltip.addAll(conflicts);
        drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
    }

    private void beginCapture(RegisteredKeyBinding binding) {
        captureBinding = binding;
        captureArmed = false;
        chordCapture.reset();
        searchField.setFocused(false);
        rebuildButtons();
    }

    private void applyBinding(String bindingSpecification) {
        keyBindingController.setBinding(captureBinding.getIdentifier(), bindingSpecification, bindingLayer);
        waitForInputRelease();
    }

    private void waitForInputRelease() {
        captureBinding = null;
        captureArmed = false;
        chordCapture.reset();
        waitingForCapturedInputRelease = true;
        rebuildButtons();
    }

    private RegisteredKeyBinding visibleBinding(int row) {
        return filteredBindings.get(page * rowsPerPage() + row);
    }

    private int visibleRowCount() {
        int firstBinding = page * rowsPerPage();
        return Math.max(Math.min(filteredBindings.size() - firstBinding, rowsPerPage()), 0);
    }

    private int rowsPerPage() {
        return Math.max((height - 122) / ROW_HEIGHT, 3);
    }

    private int pageCount() {
        return Math.max((filteredBindings.size() + rowsPerPage() - 1) / rowsPerPage(), 1);
    }

    private String statusLine() {
        if (waitingForCapturedInputRelease) {
            return "Release the controller input to continue";
        }
        if (captureBinding != null) {
            return captureArmed ? "Hold a button combination, then release it - Escape cancels"
                : "Release all controller buttons and triggers";
        }
        return "Hover a red ! binding to see the exact conflicting actions";
    }

    private static final class Category {

        private final String key;
        private final String name;

        private Category(String key, String name) {
            this.key = key;
            this.name = name;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Category)) {
                return false;
            }
            Category other = (Category) object;
            return key == null ? other.key == null : key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return key == null ? 0 : key.hashCode();
        }
    }
}
