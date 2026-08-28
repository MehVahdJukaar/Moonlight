import net.mehvahdjukaar.moonlight.api.client.gui.ConfigScreenExtensions;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton;
import net.minecraft.resources.Identifier;

// for ConfigScreenExtensions class. Adds your own buttons next to Back on your config screen
public class ConfigScreenExtensionsExample {

    private static final String MOD_ID = "my_mod";
    private static final Identifier WIKI_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "wiki");

    // Call on client init
    public static void init() {
        // mod page and social links are read from the loader metadata, these are not
        ConfigScreenExtensions.registerLink(MOD_ID, MediaButton.MediaIcon.KO_FI, "https://ko-fi.com/me");
        ConfigScreenExtensions.registerLink(MOD_ID, MediaButton.MediaIcon.LINK, "https://example.com");

        // anything else goes in as a plain 20x20 button
        ConfigScreenExtensions.registerFooterButton(MOD_ID, ConfigScreenExtensions.Side.RIGHT,
                (screen, x, y) -> MediaButton.create(screen, x, y, WIKI_SPRITE, "https://example.com/wiki", "Wiki"));
    }
}
