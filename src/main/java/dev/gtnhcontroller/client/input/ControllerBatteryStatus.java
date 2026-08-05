package dev.gtnhcontroller.client.input;

public final class ControllerBatteryStatus {

    public static final ControllerBatteryStatus UNAVAILABLE = new ControllerBatteryStatus("Unavailable", -1, false);

    private final String state;
    private final int percent;
    private final boolean available;

    private ControllerBatteryStatus(String state, int percent, boolean available) {
        this.state = state;
        this.percent = percent;
        this.available = available;
    }

    static ControllerBatteryStatus fromSdl(int powerState, int percent) {
        String state;
        switch (powerState) {
            case 1:
                state = "On battery";
                break;
            case 2:
                state = "No battery";
                break;
            case 3:
                state = "Charging";
                break;
            case 4:
                state = "Charged";
                break;
            default:
                state = "Unknown";
                break;
        }
        int safePercent = percent >= 0 && percent <= 100 ? percent : -1;
        return new ControllerBatteryStatus(state, safePercent, powerState >= 0);
    }

    public boolean isAvailable() {
        return available;
    }

    public int getPercent() {
        return percent;
    }

    public String getDisplayText() {
        return percent >= 0 ? percent + "% (" + state + ")" : state;
    }
}
