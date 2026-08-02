package codechicken.nei;

import codechicken.nei.config.OptionList;

public final class NEIClientConfig {

    public static final OptionList OPTIONS = new OptionList("nei.options");

    private NEIClientConfig() {}

    public static OptionList getOptionList() {
        return OPTIONS;
    }
}
