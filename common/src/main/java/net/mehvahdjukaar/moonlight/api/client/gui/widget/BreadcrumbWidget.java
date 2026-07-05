package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.CRUMB;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.CRUMB_CURRENT;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.CRUMB_HOVER;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.CRUMB_SEPARATOR;

/**
 * The clickable breadcrumb navigation trail in the config screen header (root › … › parent › current). Each
 * segment jumps to its screen when clicked, except the {@code current} one. When the full trail would overrun its
 * width the middle collapses to a single {@code …}, keeping the root and as many trailing crumbs as fit.
 */
public class BreadcrumbWidget extends AbstractWidget {

    /**
     * One breadcrumb segment. {@code target} is the screen to jump to when clicked; {@code current} marks the
     * segment for the page we're already on (not clickable).
     */
    public record Crumb(Component label, Screen target, boolean current) {
    }

    private static final String SEP = " › ";
    private static final String ELLIPSIS = "…";

    private final Font font;
    private final List<Crumb> crumbs;
    private final Consumer<Screen> onNavigate;

    // per-crumb text bounds, recomputed each render for hit-testing (-1 = collapsed/off-screen, not clickable)
    private final int[] crumbX0;
    private final int[] crumbX1;

    public BreadcrumbWidget(int x, int y, int width, int height, Font font, List<Crumb> crumbs, Consumer<Screen> onNavigate) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.crumbs = crumbs;
        this.onNavigate = onNavigate;
        this.crumbX0 = new int[crumbs.size()];
        this.crumbX1 = new int[crumbs.size()];
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Arrays.fill(crumbX0, -1);
        Arrays.fill(crumbX1, -1);
        int x = getX();
        int y = getY();
        boolean first = true;
        for (int i : computeVisibleCrumbs(getWidth())) {
            if (!first) {
                graphics.drawString(font, SEP, x, y, CRUMB_SEPARATOR);
                x += font.width(SEP);
            }
            first = false;
            if (i < 0) { // ellipsis placeholder for the collapsed middle
                graphics.drawString(font, ELLIPSIS, x, y, CRUMB_SEPARATOR);
                x += font.width(ELLIPSIS);
                continue;
            }
            Crumb c = crumbs.get(i);
            int w = font.width(c.label());
            crumbX0[i] = x;
            crumbX1[i] = x + w;
            boolean hover = !c.current() && inside(mouseX, mouseY, x, w);
            int color = c.current() ? CRUMB_CURRENT : (hover ? CRUMB_HOVER : CRUMB);
            graphics.drawString(font, c.label(), x, y, color);
            x += w;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && button == 0) {
            Screen target = crumbAt(mouseX, mouseY);
            if (target != null) {
                onNavigate.accept(target);
                return true;
            }
        }
        return false;
    }

    private boolean inside(double mouseX, double mouseY, int x, int w) {
        return mouseX >= x && mouseX <= x + w && mouseY >= getY() - 2 && mouseY <= getY() + 9;
    }

    /** If a clickable breadcrumb segment is under the cursor, returns its target screen; otherwise null. */
    @Nullable
    private Screen crumbAt(double mouseX, double mouseY) {
        for (int i = 0; i < crumbs.size(); i++) {
            Crumb c = crumbs.get(i);
            if (c.current() || crumbX0[i] < 0) continue; // skip current + collapsed (off-screen) crumbs
            if (inside(mouseX, mouseY, crumbX0[i], crumbX1[i] - crumbX0[i])) {
                return c.target();
            }
        }
        return null;
    }

    /**
     * Picks which crumbs to draw so the trail fits in {@code maxWidth}. If it all fits, returns every index;
     * otherwise it keeps the root and as many trailing crumbs as fit, marking the collapsed middle with a
     * {@code -1} ellipsis placeholder (root › … › parent › current).
     */
    private List<Integer> computeVisibleCrumbs(int maxWidth) {
        int n = crumbs.size();
        List<Integer> full = new ArrayList<>(n);
        for (int i = 0; i < n; i++) full.add(i);
        if (n <= 2 || trailWidth(full) <= maxWidth) return full;

        // hide the fewest middle crumbs that make it fit: root + "…" + the deepest trailing crumbs
        for (int tailCount = n - 2; tailCount >= 1; tailCount--) {
            List<Integer> display = new ArrayList<>();
            display.add(0);
            display.add(-1);
            for (int i = n - tailCount; i < n; i++) display.add(i);
            if (trailWidth(display) <= maxWidth) return display;
        }
        return List.of(0, -1, n - 1);
    }

    private int trailWidth(List<Integer> display) {
        int sep = font.width(SEP);
        int total = 0;
        for (int k = 0; k < display.size(); k++) {
            if (k > 0) total += sep;
            int i = display.get(k);
            total += i < 0 ? font.width(ELLIPSIS) : font.width(crumbs.get(i).label());
        }
        return total;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
    }
}
