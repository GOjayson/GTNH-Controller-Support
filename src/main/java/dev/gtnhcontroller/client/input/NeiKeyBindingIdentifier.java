package dev.gtnhcontroller.client.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Builds stable controller identifiers for bindings owned by NEI's separate key registry. */
public final class NeiKeyBindingIdentifier {

    private static final String PREFIX = "NEI\u001F";
    private static final Map<String, String> LEGACY_ALIASES = legacyAliases();

    private NeiKeyBindingIdentifier() {}

    public static String create(String neiIdentifier) {
        String canonicalIdentifier = canonical(neiIdentifier);
        if (canonicalIdentifier.isEmpty()) {
            throw new IllegalArgumentException("NEI identifier cannot be empty");
        }
        return PREFIX + canonicalIdentifier;
    }

    static String canonical(String neiIdentifier) {
        String safeIdentifier = neiIdentifier == null ? "" : neiIdentifier.trim();
        String alias = LEGACY_ALIASES.get(safeIdentifier);
        return alias == null ? safeIdentifier : alias;
    }

    private static Map<String, String> legacyAliases() {
        Map<String, String> aliases = new HashMap<String, String>();
        aliases.put("gui.recipe", "recipe.recipe");
        aliases.put("gui.usage", "recipe.usage");
        aliases.put("gui.back", "recipe.back");
        aliases.put("gui.prev_machine", "recipe.prev_machine");
        aliases.put("gui.next_machine", "recipe.next_machine");
        aliases.put("gui.prev_recipe", "recipe.prev_recipe");
        aliases.put("gui.next_recipe", "recipe.next_recipe");
        aliases.put("gui.bookmark", "bookmark.add");
        aliases.put("gui.favorite", "bookmark.favorite");
        aliases.put("gui.favorite_item", "bookmark.favorite_item");
        aliases.put("gui.remove_recipe", "bookmark.remove_recipe");
        aliases.put("gui.bookmark_pull_items", "bookmark.pull_items");
        aliases.put("gui.hide_bookmarks", "bookmark.hide");
        aliases.put("gui.itemzoom_toggle", "itemzoom.toggle");
        aliases.put("gui.itemzoom_hold", "itemzoom.hold");
        aliases.put("gui.itemzoom_zoom_in", "itemzoom.zoom_in");
        aliases.put("gui.itemzoom_zoom_out", "itemzoom.zoom_out");
        aliases.put("gui.copy_name", "copy.name");
        aliases.put("gui.copy_oredict", "copy.oredict");
        aliases.put("gui.copy_id", "copy.identifier");
        aliases.put("gui.chat_link_item", "bookmark.chat_link");
        return Collections.unmodifiableMap(aliases);
    }
}
