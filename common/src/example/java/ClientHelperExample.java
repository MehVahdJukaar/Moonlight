import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;

// for ClientHelper class
public class ClientHelperExample {

    // Call on mod init. Either from shared entry point or client only one
    public static void init() {
        // Here we register the various events and callbacks
        ClientHelper.addItemDecoratorsRegistration(ClientHelperExample::registerItemDecorator);
        ClientHelper.addBlockModelRegistration(ClientHelperExample::registerBlockModels);
        // Similar to PlatHelper, we can add a Client Setup
        ClientHelper.addClientSetup(ClientHelperExample::setup);
    }

    private static void setup() {
        // Client sided mod setup. Same as common one but for client only things
    }

    private static void registerItemDecorator(ClientHelper.ItemDecoratorEvent event) {
        // To register item decorator. Just wraps forge ones and provide a fabric implementation
        event.register(Items.DIAMOND, (graphics, font, stack, x, y) -> {
            graphics.drawString(font, "Hello", x, y, -1);
            return true;
        });
    }

    private static void registerBlockModels(ClientHelper.BlockModelEvent event) {
        event.register(Moonlight.res("custom_model"), CustomBlockModelExample.Unbaked.CODEC);
    }


}
