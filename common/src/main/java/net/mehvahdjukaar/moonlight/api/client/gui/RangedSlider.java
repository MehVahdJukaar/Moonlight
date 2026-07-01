package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/**
 * A slider bound to a numeric value within {@code [min, max]}. Vanilla's {@link AbstractSliderButton} stores its
 * position as a 0..1 fraction; this maps to/from the real value range and reports edits through {@code onValue}.
 * {@code integer} snaps to whole numbers; {@code percent} shows the value as {@code NN%} (use {@code min=0, max=1}).
 */
public class RangedSlider extends AbstractSliderButton {

    private final double min;
    private final double max;
    private final boolean integer;
    private final boolean percent;
    private final Consumer<Double> onValue;

    public RangedSlider(int width, int height, double min, double max, double current, boolean integer, boolean percent, Consumer<Double> onValue) {
        super(0, 0, width, height, Component.empty(), fraction(min, max, current));
        this.min = min;
        this.max = max;
        this.integer = integer;
        this.percent = percent;
        this.onValue = onValue;
        this.updateMessage();
    }

    private static double fraction(double min, double max, double value) {
        if (max <= min) return 0;
        return Mth.clamp((value - min) / (max - min), 0, 1);
    }

    public double actualValue() {
        double v = min + this.value * (max - min);
        return integer ? Math.round(v) : v;
    }

    /**
     * Repositions the slider to show the given value without firing {@link #applyValue()} extra times beyond the
     * normal message update.
     */
    public void setActualValue(double value) {
        this.value = fraction(min, max, value);
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        double v = actualValue();
        String text = percent ? Math.round(v * 100) + "%"
                : integer ? String.valueOf((long) v)
                : String.format("%.2f", v);
        this.setMessage(Component.literal(text));
    }

    @Override
    protected void applyValue() {
        this.onValue.accept(actualValue());
    }
}
