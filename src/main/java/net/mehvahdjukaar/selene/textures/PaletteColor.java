package net.mehvahdjukaar.selene.textures;

import com.mojang.blaze3d.platform.NativeImage;

import java.awt.*;

public class PaletteColor {
    public final int color;
    public final float luminance;
    public final int x;
    public final int y;
    public int occurrence = 0;

    // public final Color debug;


    public PaletteColor(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
        int r = NativeImage.getR(color);
        int g = NativeImage.getG(color);
        int b = NativeImage.getB(color);
        // this.debug = new Color(r,g,b);
        this.luminance = SpriteUtils.getLuminance(r, g, b);
    }

    public PaletteColor(int color) {
        this(0, 0, color);
    }
}