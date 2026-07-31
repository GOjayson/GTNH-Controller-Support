package dev.gtnhcontroller.client.profile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class ControllerProfileStore {

    private static final String PROFILE_EXTENSION = ".cfg";

    private final File directory;

    public ControllerProfileStore(File activeConfigFile) {
        if (activeConfigFile == null || activeConfigFile.getParentFile() == null) {
            throw new IllegalArgumentException("The active configuration file is unavailable");
        }
        directory = new File(activeConfigFile.getParentFile(), "gtnhcontroller-profiles");
    }

    public File getDirectory() {
        return directory;
    }

    public List<File> listProfiles() throws IOException {
        ensureDirectory();
        File[] files = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName()
                    .toLowerCase(Locale.ROOT)
                    .endsWith(PROFILE_EXTENSION);
            }
        });
        if (files == null) {
            throw new IOException("Could not read " + directory.getAbsolutePath());
        }

        List<File> profiles = new ArrayList<File>();
        Collections.addAll(profiles, files);
        Collections.sort(profiles, new Comparator<File>() {

            @Override
            public int compare(File first, File second) {
                return first.getName()
                    .compareToIgnoreCase(second.getName());
            }
        });
        return profiles;
    }

    public File exportProfile(File activeConfigFile, String requestedName) throws IOException {
        ensureDirectory();
        if (activeConfigFile == null || !activeConfigFile.isFile()) {
            throw new IOException("The active controller configuration does not exist");
        }
        File profileFile = resolveProfileFile(requestedName);
        if (profileFile.exists()) {
            throw new IOException("A profile with that name already exists");
        }
        Files.copy(activeConfigFile.toPath(), profileFile.toPath());
        return profileFile;
    }

    public File importProfile(File activeConfigFile, File profileFile) throws IOException {
        ensureManagedProfile(profileFile);
        if (activeConfigFile == null) {
            throw new IOException("The active controller configuration is unavailable");
        }

        boolean hadActiveConfig = activeConfigFile.isFile();
        File safetyBackup = uniqueSafetyBackup();
        if (hadActiveConfig) {
            Files.copy(activeConfigFile.toPath(), safetyBackup.toPath());
        }
        Files.copy(profileFile.toPath(), activeConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return hadActiveConfig ? safetyBackup : null;
    }

    public String displayName(File profileFile) {
        String fileName = profileFile.getName();
        return fileName.toLowerCase(Locale.ROOT)
            .endsWith(PROFILE_EXTENSION) ? fileName.substring(0, fileName.length() - PROFILE_EXTENSION.length())
                : fileName;
    }

    public static String sanitizeName(String requestedName) {
        String name = requestedName == null ? "" : requestedName.trim();
        if (name.toLowerCase(Locale.ROOT)
            .endsWith(PROFILE_EXTENSION)) {
            name = name.substring(0, name.length() - PROFILE_EXTENSION.length());
        }
        StringBuilder sanitized = new StringBuilder();
        for (int index = 0; index < name.length() && sanitized.length() < 48; index++) {
            char character = name.charAt(index);
            if (Character.isLetterOrDigit(character) || character == ' ' || character == '-' || character == '_') {
                sanitized.append(character);
            } else {
                sanitized.append('_');
            }
        }
        return sanitized.toString()
            .trim();
    }

    private File resolveProfileFile(String requestedName) throws IOException {
        String sanitizedName = sanitizeName(requestedName);
        if (sanitizedName.isEmpty()) {
            throw new IOException("Enter a profile name");
        }
        File profileFile = new File(directory, sanitizedName + PROFILE_EXTENSION);
        ensureManagedProfile(profileFile);
        return profileFile;
    }

    private void ensureManagedProfile(File profileFile) throws IOException {
        if (profileFile == null || !profileFile.getName()
            .toLowerCase(Locale.ROOT)
            .endsWith(PROFILE_EXTENSION)
            || !directory.getCanonicalFile()
                .equals(
                    profileFile.getCanonicalFile()
                        .getParentFile())) {
            throw new IOException("The selected file is outside the controller profile directory");
        }
        if (!profileFile.isFile() && profileFile.exists()) {
            throw new IOException("The selected profile is not a normal file");
        }
    }

    private File uniqueSafetyBackup() {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
        File backup = new File(directory, "before-import-" + timestamp + PROFILE_EXTENSION);
        int suffix = 2;
        while (backup.exists()) {
            backup = new File(directory, "before-import-" + timestamp + "-" + suffix + PROFILE_EXTENSION);
            suffix++;
        }
        return backup;
    }

    private void ensureDirectory() throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Could not create " + directory.getAbsolutePath());
        }
    }
}
