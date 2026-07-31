package dev.gtnhcontroller.client.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ControllerProfileStoreTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void exportsListsAndImportsProfilesWithSafetyBackup() throws Exception {
        File configDirectory = temporaryFolder.newFolder("config");
        File activeConfig = new File(configDirectory, "gtnhcontroller.cfg");
        Files.write(activeConfig.toPath(), "current".getBytes(StandardCharsets.UTF_8));
        ControllerProfileStore store = new ControllerProfileStore(activeConfig);

        File exported = store.exportProfile(activeConfig, "Accessible setup");
        Files.write(activeConfig.toPath(), "changed".getBytes(StandardCharsets.UTF_8));
        File safetyBackup = store.importProfile(activeConfig, exported);

        assertEquals("Accessible setup.cfg", exported.getName());
        assertEquals("current", read(activeConfig));
        assertEquals("changed", read(safetyBackup));
        List<File> profiles = store.listProfiles();
        assertEquals(2, profiles.size());
        assertTrue(
            profiles.get(0)
                .getName()
                .startsWith("Accessible setup"));
    }

    @Test
    public void sanitizesUnsafeFileNameCharacters() {
        String sanitized = ControllerProfileStore.sanitizeName(" ../My:Profile?.cfg ");

        assertEquals("___My_Profile_", sanitized);
        assertFalse(sanitized.contains("/"));
        assertFalse(sanitized.contains("\\"));
    }

    @Test(expected = java.io.IOException.class)
    public void refusesToOverwriteAnExistingProfile() throws Exception {
        File configDirectory = temporaryFolder.newFolder("existing");
        File activeConfig = new File(configDirectory, "gtnhcontroller.cfg");
        Files.write(activeConfig.toPath(), "current".getBytes(StandardCharsets.UTF_8));
        ControllerProfileStore store = new ControllerProfileStore(activeConfig);

        store.exportProfile(activeConfig, "same");
        store.exportProfile(activeConfig, "same");
    }

    private static String read(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
