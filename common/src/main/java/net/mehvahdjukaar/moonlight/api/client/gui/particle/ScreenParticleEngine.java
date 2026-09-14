package net.mehvahdjukaar.moonlight.api.client.gui.particle;

import net.mehvahdjukaar.moonlight.api.client.gui.FrameClock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

import java.util.ArrayList;
import java.util.List;

public class ScreenParticleEngine implements Renderable {

    private static final int DEFAULT_CAP = 256;

    private final List<ScreenParticle> particles = new ArrayList<>();
    private final int cap;
    private final FrameClock clock = new FrameClock();

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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.renderAndTick(graphics);
    }

    public void renderAndTick(GuiGraphicsExtractor graphics) {
        float dt = this.clock.advance();
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
