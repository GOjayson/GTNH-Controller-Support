package codechicken.nei.config;

import java.util.ArrayList;
import java.util.List;

public final class OptionList extends Option {

    public final List<Option> optionList = new ArrayList<Option>();

    public OptionList(String name) {
        super(name);
    }
}
