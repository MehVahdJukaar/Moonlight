package net.mehvahdjukaar.moonlight.fabric;

import java.util.ArrayList;
import java.util.List;

//fix concurrency. use queue
@Deprecated(forRemoval = true)
public class MLFabricSetupCallbacks {

    //use client helper
    /**
     * Equivalent of forge common setup. called by this mod client initializer and server initializer
     */
    @Deprecated
    public static List<Runnable> COMMON_SETUP = new ArrayList<>();
    /**
     * Equivalent of forge client setup. called by this mod client initializer
     */
    @Deprecated
    public static List<Runnable> CLIENT_SETUP = new ArrayList<>();


}
