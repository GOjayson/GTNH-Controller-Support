package codechicken.nei.config;

public final class OptionKeyBind extends Option {

    @SuppressWarnings("unused")
    private final boolean useHash;

    public OptionKeyBind(String identifier, boolean useHash) {
        super("keys." + identifier);
        this.useHash = useHash;
    }
}
