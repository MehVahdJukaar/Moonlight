package net.mehvahdjukaar.moonlight.example;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.AnimalFoodHelper;
import net.minecraft.world.item.Items;

// PlatHelper is a helper class which contains many wrapper methods for platform specific functions which differ enough to require a
// different implementation for both platforms. More forge specific calls that dont have a fabric equivalent are in ForgeHelper
public class PlatHelperExample {

    // call on mod init
    public static void init(){
        // you can add this line in your mod init to add a common setup method
        PlatHelper.addCommonSetup(PlatHelperExample::setup);

        if(PlatHelper.getPhysicalSide().isClient()){
            //from here we also initialize client stuff. No need for 2 separate initializer with this
            ClientHelperExample.init();
        }
        //the above code will be the basics for any mod that uses this lib
    }

    private static void setup() {
        // setup work. will run after all registration is done
        if(PlatHelper.isDev()) {
            // adding some animal food only if we are in dev environment
            RegHelper.registerParrotFood(Items.APPLE);

            if(PlatHelper.isModLoaded("jei")){
                // note that this call is fabric only. for forge you should override getBurnTime in your item
                RegHelper.registerItemBurnTime(Items.SKELETON_SKULL, 2);
            }
        }



    }

}
