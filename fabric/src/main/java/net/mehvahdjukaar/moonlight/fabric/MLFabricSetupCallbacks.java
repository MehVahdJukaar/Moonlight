package net.mehvahdjukaar.moonlight.fabric;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

//fix concurrency
public interface MLFabricSetupCallbacks {

    /**
     * Equivalent of forge common setup. called by this mod client initializer and server initializer
     */
    List<Runnable> COMMON_SETUP = new ArrayList<>();
    /**
     * Equivalent of forge client setup. called by this mod client initializer
     */
    List<Runnable> CLIENT_SETUP = new ArrayList<>();


    @Deprecated(forRemoval = true)
    static void finishModInit(String modId){
       // RegHelperImpl.finishRegistration(modId);
    }

    @ApiStatus.Internal
    List<Runnable> BEFORE_COMMON_SETUP = new ArrayList<>();

}
