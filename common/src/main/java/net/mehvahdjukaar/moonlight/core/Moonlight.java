package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.core.misc.ModCriteriaTriggers;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //called either on mod creation
    public static void commonInit() {
        ModMessages.registerMessages();
        VillagerAIInternal.init();
        ModCriteriaTriggers.register();
    }


    //mod registration
    public static void commonRegistration() {

    }

    //mod setup
    public static void commonSetup() {
        //./gradlew build publishToMavenLocal
    }
}
