package dev.gtnhcontroller.client.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

import org.lwjgl.input.Keyboard;

import dev.gtnhcontroller.Config;
import dev.gtnhcontroller.GTNHController;
import dev.gtnhcontroller.client.input.ControllerProfile;
import dev.gtnhcontroller.client.input.ModKeyBindingController;
import dev.gtnhcontroller.client.profile.ControllerProfileStore;

public final class GuiControllerProfileScreen extends GuiScreen
    implements ControllerConfigurationScreen, GuiYesNoCallback {

    private static final int IMPORT_BUTTON_BASE = 100;
    private static final int IMPORT_CONFIRM_BASE = 1000;
    private static final int EXPORT = 300;
    private static final int PREVIOUS_PAGE = 301;
    private static final int NEXT_PAGE = 302;
    private static final int DONE = 303;
    private static final int ROW_HEIGHT = 23;

    private final GuiScreen parentScreen;
    private final ControllerProfile controllerProfile;
    private final ModKeyBindingController modKeyBindingController;
    private final List<File> profiles = new ArrayList<File>();

    private ControllerProfileStore profileStore;
    private GuiTextField profileNameField;
    private int page;
    private String status = "";
    private boolean statusIsError;

    public GuiControllerProfileScreen(GuiScreen parentScreen, ControllerProfile controllerProfile,
        ModKeyBindingController modKeyBindingController) {
        this.parentScreen = parentScreen;
        this.controllerProfile = controllerProfile;
        this.modKeyBindingController = modKeyBindingController;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        String previousName = profileNameField == null ? "" : profileNameField.getText();
        profileNameField = new GuiTextField(fontRendererObj, width / 2 - 155, 45, 210, 18);
        profileNameField.setMaxStringLength(48);
        profileNameField.setText(previousName);

        try {
            profileStore = new ControllerProfileStore(Config.getConfigFile());
            refreshProfiles();
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
            statusIsError = true;
            rebuildButtons();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        profileNameField.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int firstProfile = page * rowsPerPage();
        int visibleCount = Math.min(profiles.size() - firstProfile, rowsPerPage());
        if (button.id >= IMPORT_BUTTON_BASE && button.id < IMPORT_BUTTON_BASE + Math.max(visibleCount, 0)) {
            int profileIndex = firstProfile + button.id - IMPORT_BUTTON_BASE;
            File profile = profiles.get(profileIndex);
            mc.displayGuiScreen(
                new GuiYesNo(
                    this,
                    "Import controller profile '" + profileStore.displayName(profile) + "'?",
                    "Current settings are backed up first, then replaced.",
                    IMPORT_CONFIRM_BASE + profileIndex));
        } else if (button.id == EXPORT) {
            exportProfile();
        } else if (button.id == PREVIOUS_PAGE && page > 0) {
            page--;
            rebuildButtons();
        } else if (button.id == NEXT_PAGE && page < pageCount() - 1) {
            page++;
            rebuildButtons();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void confirmClicked(boolean confirmed, int id) {
        int profileIndex = id - IMPORT_CONFIRM_BASE;
        mc.displayGuiScreen(this);
        if (!confirmed || profileIndex < 0 || profileIndex >= profiles.size()) {
            return;
        }

        File profile = profiles.get(profileIndex);
        String selectedController = Config.controllerSelection;
        try {
            Config.saveControllerSettings();
            File backup = profileStore.importProfile(Config.getConfigFile(), profile);
            Config.synchronize(Config.getConfigFile());
            Config.controllerSelection = selectedController;
            Config.saveControllerSettings();
            controllerProfile.reloadBindings();
            modKeyBindingController.refreshBindings();
            status = "Imported " + profileStore.displayName(profile)
                + (backup == null ? "" : "; backup: " + backup.getName());
            statusIsError = false;
            refreshProfiles();
        } catch (IOException exception) {
            setError("Import failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    protected void keyTyped(char typedCharacter, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        if (profileNameField.textboxKeyTyped(typedCharacter, keyCode)) {
            status = "";
            return;
        }
        super.keyTyped(typedCharacter, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        profileNameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Controller Profiles", width / 2, 12, 0xFFFFFF);
        drawCenteredString(
            fontRendererObj,
            status.isEmpty() ? "Profiles include bindings, tuning, accessibility, radial actions and chat macros"
                : fontRendererObj.trimStringToWidth(status, 310),
            width / 2,
            27,
            status.isEmpty() ? 0xA0A0A0 : statusIsError ? 0xFF8080 : 0x80FF80);
        if (profiles.isEmpty() && status.isEmpty()) {
            drawCenteredString(fontRendererObj, "No exported profiles yet", width / 2, 94, 0xA0A0A0);
        } else if (pageCount() > 1) {
            drawCenteredString(
                fontRendererObj,
                "Page " + (page + 1) + " / " + pageCount(),
                width / 2,
                height - 46,
                0xA0A0A0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        profileNameField.drawTextBox();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    private void exportProfile() {
        try {
            Config.saveControllerSettings();
            File profile = profileStore.exportProfile(Config.getConfigFile(), profileNameField.getText());
            profileNameField.setText("");
            status = "Exported " + profile.getName();
            statusIsError = false;
            refreshProfiles();
        } catch (IOException exception) {
            setError("Export failed: " + exception.getMessage(), exception);
        }
    }

    private void refreshProfiles() {
        try {
            profiles.clear();
            profiles.addAll(profileStore.listProfiles());
            page = Math.min(page, pageCount() - 1);
            rebuildButtons();
        } catch (IOException exception) {
            setError("Cannot read profiles: " + exception.getMessage(), exception);
            rebuildButtons();
        }
    }

    private void rebuildButtons() {
        buttonList.clear();
        GuiButton exportButton = new GuiButton(EXPORT, width / 2 + 60, 44, 95, 20, "Export Current");
        exportButton.enabled = profileStore != null;
        buttonList.add(exportButton);

        int firstProfile = page * rowsPerPage();
        int finalProfile = Math.min(firstProfile + rowsPerPage(), profiles.size());
        for (int profileIndex = firstProfile; profileIndex < finalProfile; profileIndex++) {
            int row = profileIndex - firstProfile;
            String name = profileStore == null ? profiles.get(profileIndex)
                .getName() : profileStore.displayName(profiles.get(profileIndex));
            buttonList.add(
                new GuiButton(
                    IMPORT_BUTTON_BASE + row,
                    width / 2 - 155,
                    72 + row * ROW_HEIGHT,
                    310,
                    20,
                    "Import: " + fontRendererObj.trimStringToWidth(name, 252)));
        }

        if (pageCount() > 1) {
            GuiButton previousButton = new GuiButton(PREVIOUS_PAGE, width / 2 - 155, height - 52, 80, 20, "< Previous");
            GuiButton nextButton = new GuiButton(NEXT_PAGE, width / 2 + 75, height - 52, 80, 20, "Next >");
            previousButton.enabled = page > 0;
            nextButton.enabled = page < pageCount() - 1;
            buttonList.add(previousButton);
            buttonList.add(nextButton);
        }
        buttonList.add(new GuiButton(DONE, width / 2 - 100, height - 28, 200, 20, "Done"));
    }

    private void setError(String message, Exception exception) {
        status = message;
        statusIsError = true;
        GTNHController.LOG.error(message, exception);
    }

    private int rowsPerPage() {
        return Math.max((height - 150) / ROW_HEIGHT, 1);
    }

    private int pageCount() {
        return Math.max((profiles.size() + rowsPerPage() - 1) / rowsPerPage(), 1);
    }
}
