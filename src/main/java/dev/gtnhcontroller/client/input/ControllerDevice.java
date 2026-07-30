package dev.gtnhcontroller.client.input;

public final class ControllerDevice {

    private final String selectionKey;
    private final String displayName;
    private final boolean connected;

    ControllerDevice(String selectionKey, String displayName, boolean connected) {
        this.selectionKey = selectionKey;
        this.displayName = displayName;
        this.connected = connected;
    }

    public String getSelectionKey() {
        return selectionKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isConnected() {
        return connected;
    }
}
