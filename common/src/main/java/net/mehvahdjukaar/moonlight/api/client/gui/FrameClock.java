package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.Util;

//for wall time stuff
public final class FrameClock {

    private long lastMs = -1;

    public float advance() {
        long now = Util.getMillis();
        float dt = lastMs < 0 ? 0 : Math.min((now - lastMs) / 1000f, 0.1f);
        lastMs = now;
        return dt;
    }
}
