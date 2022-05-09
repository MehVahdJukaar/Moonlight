package net.mehvahdjukaar.selene.math.colors;

public final class ColorSpaces {

    public static HSVColor RGBtoHSV(RGBColor color) {
        float r = color.red();
        float g = color.green();
        float b = color.blue();
        float hue, saturation, brightness;

        float cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        float cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        brightness = cmax;
        if (cmax != 0)
            saturation = (cmax - cmin) / cmax;
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = (cmax - r) / (cmax - cmin);
            float greenc = (cmax - g) / (cmax - cmin);
            float bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        return new HSVColor(hue, saturation, brightness, color.alpha());
    }

    public static RGBColor HSVtoRGB(HSVColor color) {
        float hue = color.hue();
        float saturation = color.saturation();
        float brightness = color.value();
        float r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> {
                    r = (brightness);
                    g = (t);
                    b = (p);
                }
                case 1 -> {
                    r = (q);
                    g = (brightness);
                    b = (p);
                }
                case 2 -> {
                    r = (p);
                    g = (brightness);
                    b = t;
                }
                case 3 -> {
                    r = (p);
                    g = (q);
                    b = (brightness);
                }
                case 4 -> {
                    r = (t);
                    g = (p);
                    b = (brightness);
                }
                case 5 -> {
                    r = (brightness);
                    g = (p);
                    b = (q);
                }
            }
        }
        return new RGBColor(r, g, b, color.alpha());
    }

    /**
     * Converts an RGB color value to HSL. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     */
    public static HSLColor RGBtoHSL(RGBColor color) {
        float r = color.red();
        float g = color.green();
        float b = color.blue();

        float max = (r > g && r > b) ? r : Math.max(g, b);
        float min = (r < g && r < b) ? r : Math.min(g, b);

        float h, s, l;
        l = (max + min) / 2.0f;

        if (max == min) {
            h = s = 0.0f;
        } else {
            float d = max - min;
            s = (l > 0.5f) ? d / (2.0f - max - min) : d / (max + min);

            if (r > g && r > b)
                h = (g - b) / d + (g < b ? 6.0f : 0.0f);

            else if (g > b)
                h = (b - r) / d + 2.0f;

            else
                h = (r - g) / d + 4.0f;

            h /= 6.0f;
        }
        return new HSLColor(h, s, l, color.alpha());
    }

    /**
     * Converts an HSL color value to RGB. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes h, s, and l are contained in the set [0, 1] and
     * returns r, g, and b in the set [0, 255].
     */
    public static RGBColor HSLtoRGB(HSLColor color) {
        float h = color.hue();
        float s = color.saturation();
        float l = color.lightness();
        float r, g, b;

        if (s == 0f) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }
        return new RGBColor(r, g, b, color.alpha());
    }

    /**
     * Helper method that converts hue to rgb
     */
    private static float hueToRgb(float p, float q, float t) {
        if (t < 0f)
            t += 1f;
        if (t > 1f)
            t -= 1f;
        if (t < 1f / 6f)
            return p + (q - p) * 6f * t;
        if (t < 1f / 2f)
            return q;
        if (t < 2f / 3f)
            return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    /**
     * values from https://en.wikipedia.org/wiki/SRGB#The_sRGB_transfer_function_.28.22gamma.22.29
     */
    public static XYZColor RGBtoXYZ(RGBColor color) {
        float red = color.red();
        float green = color.green();
        float blue = color.blue();

        double r = ((red > 0.04045f) ? Math.pow((red + 0.055f) / 1.055f, 2.4f) : (red / 12.92f));
        double g = ((green > 0.04045f) ? Math.pow((green + 0.055f) / 1.055f, 2.4f) : (green / 12.92f));
        double b = ((blue > 0.04045f) ? Math.pow((blue + 0.055f) / 1.055f, 2.4f) : (blue / 12.92f));


        float x = (float) (0.4124f * r + 0.3576f * g + 0.1805f * b);
        float y = (float) (0.2126f * r + 0.7152f * g + 0.0722f * b);
        float z = (float) (0.0193f * r + 0.1192f * g + 0.9505f * b);
        return new XYZColor(x, y, z, color.alpha());
    }

    public static RGBColor XYZtoRGB(XYZColor color) {
        float x = color.x();
        float y = color.y();
        float z = color.z();

        float r = 3.2406f * x - 1.5372f * y - 0.4986f * z;
        float g = -0.9689f * x + 1.8758f * y + 0.0415f * z;
        float b = 0.0557f * x - 0.2040f * y + 1.0570f * z;

        r = (r > 0.0031308f) ? (float) (1.055f * Math.pow(r, 1.f / 2.4f) - 0.055f) : (12.92f * r);
        g = (g > 0.0031308f) ? (float) (1.055f * Math.pow(g, 1.f / 2.4f) - 0.055f) : (12.92f * g);
        b = (b > 0.0031308f) ? (float) (1.055f * Math.pow(b, 1.f / 2.4f) - 0.055f) : (12.92f * b);
        return new RGBColor(r, g, b, color.alpha());
    }

    private static final float SCALE_X = 95.047f / 100f;
    private static final float SCALE_Y = 1.0f;
    private static final float SCALE_Z = 108.883f / 100f;
    private static final float SCALE_L = 100;
    private static final float SCALE_A = 255;
    private static final float SCALE_B = 255;

    public static LABColor XYZtoLAB(XYZColor color) {
        float x = color.x() / SCALE_X;
        float y = color.y() / SCALE_Y;
        float z = color.z() / SCALE_Z;

        x = (x > 0.008856f) ? (float) Math.cbrt(x) : ((7.787f * x) + 16.f / 116.f);
        y = (y > 0.008856f) ? (float) Math.cbrt(y) : ((7.787f * y) + 16.f / 116.f);
        z = (z > 0.008856f) ? (float) Math.cbrt(z) : ((7.787f * z) + 16.f / 116.f);

        float l = (116 * y) - 16;
        float a = 500 * (x - y);
        float b = 200 * (y - z);
        return new LABColor(l / SCALE_L, a / SCALE_A, b / SCALE_B, color.alpha());
    }

    public static XYZColor LABtoXYZ(LABColor color) {
        float y0 = (color.luminance() * SCALE_L + 16f) / 116f;
        float x0 = color.a() * SCALE_A / 500f + y0;
        float z0 = y0 - color.b() * SCALE_B / 200f;

        float x3 = x0 * x0 * x0;
        float x = (float) (((x3 > 0.008856f) ? x3 : ((x0 - 16.f / 116.f) / 7.787)) * SCALE_X);
        float y3 = y0 * y0 * y0;
        float y = (float) (((y3 > 0.008856f) ? y3 : ((y0 - 16.f / 116.f) / 7.787)) * SCALE_Y);
        float z3 = z0 * z0 * z0;
        float z = (float) (((z3 > 0.008856f) ? z3 : ((z0 - 16.f / 116.f) / 7.787)) * SCALE_Z);

        return new XYZColor(x, y, z, color.alpha());
    }

    /**
     * https://en.wikipedia.org/wiki/CIELAB_color_space#Cylindrical_model
     */
    public static HCLColor LABtoHCL(LABColor color) {
        float l = color.luminance();
        float a = color.a();
        float b = color.b();

        float c = (float) Math.sqrt((a * a) + (b * b));
        float h = (float) Math.atan2(b, a);

        h = (float) (h / (Math.PI * 2));
        while (h < 0) ++h;

        if (c < 0 || h < 0 || c > 1 || h > 1) {

            int aa = 1;
        }

        return new HCLColor(h, c, l, color.alpha());
    }

    public static LABColor HCLtoLAB(HCLColor color) {

        float h = color.hue();
        float c = color.chroma();
        float l = color.luminance();

        float a = (float) (c * Math.cos(h * Math.PI * 2));
        float b = (float) (c * Math.sin(h * Math.PI * 2));


        return new LABColor(l, a, b, color.alpha());
    }

    private static final float UN = 0.2009f;
    private static final float VN = 0.4610f;

    /**
     * https://en.wikipedia.org/wiki/CIELUV
     */
    public static LUVColor XYZtoLUV(XYZColor color) {
        float X = color.x();
        float Y = color.y();
        float Z = color.z();

        float P = (X + 15 * Y + 3 * Z);
        float uP = 4 * X / P;
        float vP = 9 * Y / P;
        float f = 6 / 29f;
        float F = f * f * f;
        float L = (float) (Y <= F ? Math.pow(29 / 3f, 3) * Y : 116 * Math.cbrt(Y) - 16);
        float u = (13 * L * (uP - UN));
        float v = (13 * L * (vP - VN));

        return new LUVColor(L / SCALE_A, u / SCALE_A, v / SCALE_A, color.alpha());
    }

    public static XYZColor LUVtoXYZ(LUVColor color) {
        float L = color.luminance() * SCALE_A;
        float u = color.u() * SCALE_A;
        float v = color.v() * SCALE_A;
        float uP = UN + (u / (13 * L));
        float vP = VN + (v / (13 * L));
        float Y = (float) (L <= 8 ? L * Math.pow(3 / 29f, 3) : Math.pow((L + 16) / 116f, 3));
        float X = Y * 9 * uP / (4 * vP);
        float Z = Y * (12 - 3 * uP - 20 * vP) / (4 * vP);

        return new XYZColor(X, Y, Z, color.alpha());
    }

    public static HCLVColor LUVtoHCLV(LUVColor color) {
        float l = color.luminance();
        float a = color.u();
        float b = color.v();

        float c = (float) Math.sqrt((a * a) + (b * b));
        float h = (float) Math.atan2(b, a);

        h = (float) (h / (Math.PI * 2));
        while (h < 0) ++h;

        return new HCLVColor(h, c, l, color.alpha());
    }

    public static LUVColor HCLVtoLUV(HCLVColor color) {

        float h = color.hue();
        float c = color.chroma();
        float l = color.luminance();

        float u = (float) (c * Math.cos(h * Math.PI * 2));
        float v = (float) (c * Math.sin(h * Math.PI * 2));

        return new LUVColor(l, u, v, color.alpha());
    }
}
