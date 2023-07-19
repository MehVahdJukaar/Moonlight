package net.mehvahdjukaar.moonlight.api.platform.setup;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ClientSetupHelper {

    public static void deferClientSetup(Class<? extends IDeferredClientSetup> modSetup) {
        try {
            deferClientSetup(modSetup.getConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate class " + modSetup, e);
        }
    }

    @ExpectPlatform
    public static void deferClientSetup(IDeferredClientSetup clientSetup) {
        throw new AssertionError();
    }
}
