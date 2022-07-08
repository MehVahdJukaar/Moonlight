package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HCLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PaletteColor implements Comparable<PaletteColor> {

    //caches all of these, so we don't have to look them up constantly
    private final int value;
    private final RGBColor color;
    private final LABColor lab;
    private final HCLColor hcl;

    public int occurrence = 0;

    public PaletteColor(int color) {
        this(new RGBColor(color));
    }

    public PaletteColor(BaseColor<?> color, int occurrence) {
        this(color);
        this.occurrence = occurrence;
    }

    public PaletteColor(BaseColor<?> color) {
        var c = color.asRGB();
        if (c.alpha() == 0) this.color = new RGBColor(0);
        else this.color = c;
        this.lab = this.color.asLAB();
        this.value = this.color.toInt();
        this.hcl = lab.asHCL();
    }

    public int value() {
        return value;
    }

    public RGBColor rgb() {
        return color;
    }

    public LABColor lab() {
        return lab;
    }

    public HCLColor hcl() {
        return hcl;
    }

    /**
     * Creates a new palette color from this by darkening it
     */
    public PaletteColor getDarkened() {
        var p = new PaletteColor(this.lab.withLuminance(this.lab.luminance() * 0.9f));
        p.occurrence = this.occurrence;
        return p;
    }

    /**
     * Creates a new palette color from this by lightnening it
     */
    public PaletteColor getLightened() {
        var p = new PaletteColor(this.lab.withLuminance(this.lab.luminance() * 0.9f + 0.1f));
        p.occurrence = this.occurrence;
        return p;
    }

    public float luminance() {
        return lab.luminance();
    }

    public float distanceTo(PaletteColor color) {
        return this.lab.distTo(color.lab);
    }

    @Override
    public int compareTo(@NotNull PaletteColor o) {
        return Float.compare(this.lab.luminance(), o.lab.luminance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaletteColor that = (PaletteColor) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, color, lab, hcl, occurrence);
    }

    @Override
    public String toString() {
        return "PaletteColor:" + value;
    }
}