import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.item.Items;

// PlatHelper is a helper class which contains many wrapper methods for platform specific functions which differ enough to require a
// different implementation for both platforms. More forge specific calls that dont have a fabric equivalent are in ForgeHelper
public class PlatHelperExample {

    // Call on mod init
    public static void init() {
        // Adding a Common Setup step, called after registration. Equivalent of Forge one
        PlatHelper.addCommonSetup(PlatHelperExample::setup);

        if (PlatHelper.getPhysicalSide().isClient()) {
            // From here we also initialize client stuff. No need for two separate initializers with this
            ClientHelperExample.init();
        }
        // The above code will be the basics for any mod that uses this lib
    }

    // Setup will run after all registrations are done
    private static void setup() {
        // Adding some animal foods
        RegHelper.registerParrotFood(Items.APPLE);

        // Showcasing some other PlatHelper functions
        if (PlatHelper.isModLoaded("jei") && !PlatHelper.isDev()) {
            // Note that this call is fabric only. For Forge, you should override getBurnTime in your item
            RegHelper.registerItemBurnTime(Items.SKELETON_SKULL, 2);
        }
    }

}
