import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

// for ClientHelper class
public class ClientHelperExample {

    // Call on mod init. Either from shared entry point or client only one
    public static void init() {
        // Here we register the various events and callbacks
        ClientHelper.addItemColorsRegistration(ClientHelperExample::registerItemColors);
        ClientHelper.addItemDecoratorsRegistration(ClientHelperExample::registerItemDecorator);
        ClientHelper.addModelLoaderRegistration(ClientHelperExample::registerModelLoaders);
        // Similar to PlatHelper, we can add a Client Setup
        ClientHelper.addClientSetup(ClientHelperExample::setup);
    }

    private static void setup() {
        // Client sided mod setup. Same as common one but for client only things
        ClientHelper.registerRenderType(Blocks.CYAN_STAINED_GLASS, RenderType.solid());
    }

    public static void registerItemColors(ClientHelper.ItemColorEvent event) {
        // Overriding potion color
        event.register((itemStack, i) -> 0, Items.POTION);
    }

    private static void registerItemDecorator(ClientHelper.ItemDecoratorEvent event) {
        // To register item decorator. Just wraps forge ones and provide a fabric implementation
        event.register(Items.DIAMOND, (graphics, font, stack, x, y) -> {
            graphics.drawString(font, "Hello", x, y, -1);
            return true;
        });
    }

    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(Moonlight.res("custom_loader"), new CustomModelLoaderExample());
    }


}
