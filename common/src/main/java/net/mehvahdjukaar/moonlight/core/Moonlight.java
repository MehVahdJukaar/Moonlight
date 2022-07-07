package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger();
    public static Object COMMON_INSTANCE ;

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //called either on mod creation
    public static void commonInit() {
        ModMessages.registerMessages();
        VillagerAIInternal.init();

        ConfigBuilder cb = ConfigBuilder.create(res("aa"), ConfigType.COMMON);
        cb.push("a");
        cb.push("aaaaa");
        cb.push("aaaaa2");
        cb.define("aaa",true);

        cb.define("bbb",true);
        cb.pop();
        cb.pop();

        cb.pop();
       COMMON_INSTANCE =  cb.buildAndRegister();
    }


    //mod registration
    public static void commonRegistration() {

    }

    //mod setup
    public static void commonSetup() {
//./gradlew build publishToMavenLocal
    }



    //hanging roots item override (mixin for fabric override for forge)
    //RE add lightning strike growths
    //TODO: disabled conditional growth. add command system
    //fire mixin
    //figure out thin ice situation
    //thin ice repair and solidify when snow??
    //CHARRED stuff falling in water
}
