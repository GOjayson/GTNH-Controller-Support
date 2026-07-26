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
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.input.RegisteredKeyBinding;

public final class GuiRadialMenuActionScreen extends GuiScreen implements ControllerConfigurationScreen {

    private static final int SELECT_BUTTON_BASE = 100;
    private static final int CATEGORY = 300;
    private static final int PREVIOUS_PAGE = 301;
    private static final int NEXT_PAGE = 302;
    private static final int CLEAR_SLOT = 303;
    private static final int CANCEL = 304;
    private static final int FIRST_ROW_Y = 70;
    private static final int ROW_HEIGHT = 22;

    private final GuiScreen parentScreen;
    private final ModKeyBindingController keyBindingController;
    private final int targetSlot;
    private final List<RegisteredKeyBinding> allBindings = new ArrayList<RegisteredKeyBinding>();
    private final List<RegisteredKeyBinding> filteredBindings = new ArrayList<RegisteredKeyBinding>();
    private final List<Category> categories = new ArrayList<Category>();

    private GuiTextField searchField;
    private int categoryIndex;
    private int page;

    public GuiRadialMenuActionScreen(GuiScreen parentScreen, ModKeyBindingController keyBindingController,
        int targetSlot) {
        this.parentScreen = parentScreen;
        this.keyBindingController = keyBindingController;
        this.targetSlot = targetSlot;
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
    protected void actionPerformed(GuiButton button) {
        int rowCount = visibleRowCount();
        if (button.id >= SELECT_BUTTON_BASE && button.id < SELECT_BUTTON_BASE + rowCount) {
            RegisteredKeyBinding binding = visibleBinding(button.id - SELECT_BUTTON_BASE);
            Config.setRadialMenuEntry(targetSlot, binding.getIdentifier());
            Config.saveControllerSettings();
            mc.displayGuiScreen(parentScreen);
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
        } else if (button.id == CLEAR_SLOT) {
            Config.setRadialMenuEntry(targetSlot, "");
            Config.saveControllerSettings();
            mc.displayGuiScreen(parentScreen);
        } else if (button.id == CANCEL) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
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
        drawCenteredString(fontRendererObj, "Choose Radial Action", width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Search and select one registered key action", width / 2, 27, 0xA0A0A0);

        if (searchField.getText()
            .isEmpty() && !searchField.isFocused()) {
            drawString(fontRendererObj, "Search...", searchField.xPosition + 4, searchField.yPosition + 5, 0x707070);
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
            drawCenteredString(
                fontRendererObj,
                "Page " + (page + 1) + " / " + pageCount(),
                width / 2,
                height - 46,
                0xA0A0A0);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        searchField.drawTextBox();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
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
        buttonList.add(new GuiButton(CATEGORY, width / 2 + 60, 42, 95, 20, categoryLabel));

        int firstBinding = page * rowsPerPage();
        int finalBinding = Math.min(firstBinding + rowsPerPage(), filteredBindings.size());
        for (int bindingIndex = firstBinding; bindingIndex < finalBinding; bindingIndex++) {
            int row = bindingIndex - firstBinding;
            buttonList.add(
                new GuiButton(
                    SELECT_BUTTON_BASE + row,
                    width / 2 - 35,
                    FIRST_ROW_Y + row * ROW_HEIGHT,
                    190,
                    20,
                    "Select"));
        }

        if (pageCount() > 1) {
            GuiButton previousButton = new GuiButton(PREVIOUS_PAGE, width / 2 - 155, height - 52, 70, 20, "< Previous");
            GuiButton nextButton = new GuiButton(NEXT_PAGE, width / 2 + 85, height - 52, 70, 20, "Next >");
            previousButton.enabled = page > 0;
            nextButton.enabled = page < pageCount() - 1;
            buttonList.add(previousButton);
            buttonList.add(nextButton);
        }

        GuiButton clearButton = new GuiButton(CLEAR_SLOT, width / 2 - 155, height - 28, 150, 20, "Clear Slot");
        clearButton.enabled = !Config.getRadialMenuEntry(targetSlot)
            .isEmpty();
        buttonList.add(clearButton);
        buttonList.add(new GuiButton(CANCEL, width / 2 + 5, height - 28, 150, 20, "Cancel"));
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
