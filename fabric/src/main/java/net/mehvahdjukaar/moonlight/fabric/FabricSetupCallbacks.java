package net.mehvahdjukaar.moonlight.fabric;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;

import java.util.ArrayList;
import java.util.List;

//fix concurrency
public interface FabricSetupCallbacks {

    /**
     * Equivalent of forge common setup. called by this mod client initializer and server initializer
     */
    List<Runnable> COMMON_SETUP = new ArrayList<>();
    /**
     * Equivalent of forge client setup. called by this mod client initializer
     */
    List<Runnable> CLIENT_SETUP = new ArrayList<>();


    static void finishModInit(String modId){
        RegHelperImpl.finishRegistration(modId);
    }

}
