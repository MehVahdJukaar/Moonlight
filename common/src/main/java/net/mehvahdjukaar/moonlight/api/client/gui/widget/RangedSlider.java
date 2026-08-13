package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/**
 * A slider over a number between min and max. Vanilla's AbstractSliderButton keeps its position as a 0 to 1
 * fraction, so this converts back and forth and sends edits to onValue. With integer it snaps to whole numbers,
 * with percent it shows NN% instead (use min 0, max 1).
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

    /** Moves the slider to show a value without any extra applyValue() calls. */
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
