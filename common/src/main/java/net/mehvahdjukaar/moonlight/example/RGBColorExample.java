package net.mehvahdjukaar.moonlight.example;

import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;

public class RGBColorExample {


    // Here some uses of the BaseColor class is shown
    public static RGBColor modifyColor(RGBColor color){
        // we can convert colors into many color spaces depending on our needs
        LABColor lab = color.asLAB();
        LABColor pureRed = new RGBColor(1,0,0,1).asLAB();

        // we mix the color with 20% pure red. Color mixing changes depending on color space used
        lab.mixWith(pureRed, 0.2f);

        // now we set the luminance to a value we want
        lab.withLuminance(0.4f);
        return lab.asRGB();
    }
}
