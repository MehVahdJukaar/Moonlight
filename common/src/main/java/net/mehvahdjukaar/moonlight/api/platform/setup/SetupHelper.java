package net.mehvahdjukaar.moonlight.api.platform.setup;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class SetupHelper {

    public static void deferSetup(Class<? extends IDeferredCommonSetup> modSetup) {
        try {
            deferSetup(modSetup.getConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate class " + modSetup, e);
        }
    }

    @ExpectPlatform
    public static void deferSetup(IDeferredCommonSetup modSetup) {
        throw new AssertionError();
    }

}
