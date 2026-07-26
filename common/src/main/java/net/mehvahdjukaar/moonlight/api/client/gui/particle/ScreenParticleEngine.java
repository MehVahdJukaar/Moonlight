package net.mehvahdjukaar.moonlight.api.client.gui.particle;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

import java.util.ArrayList;
import java.util.List;

public class ScreenParticleEngine implements Renderable {

    private static final int DEFAULT_CAP = 256;

    private final List<ScreenParticle> particles = new ArrayList<>();
    private final int cap;
    private long lastMs = -1;

    public ScreenParticleEngine() {
        this(DEFAULT_CAP);
    }

    public ScreenParticleEngine(int cap) {
        this.cap = cap;
    }

    public ScreenParticle add(ScreenParticle particle) {
        if (this.particles.size() >= this.cap) this.particles.removeFirst();
        this.particles.add(particle);
        return particle;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderAndTick(graphics);
    }

    public void renderAndTick(GuiGraphics graphics) {
        long now = Util.getMillis();
        float dt = this.lastMs < 0 ? 0 : Math.min((now - this.lastMs) / 1000f, 0.1f); // clamp screen-reopen gaps
        this.lastMs = now;
        this.particles.removeIf(p -> !p.tick(dt));
        for (ScreenParticle p : this.particles) {
            p.render(graphics);
        }
    }

    public void clear() {
        this.particles.clear();
    }

    public boolean isEmpty() {
        return this.particles.isEmpty();
    }
}
