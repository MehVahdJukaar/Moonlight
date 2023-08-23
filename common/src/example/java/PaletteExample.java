import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.PaletteColor;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;

public class PaletteExample {

    // Here Palette class use is shown
    public static Palette modifyPaletteExample(TextureImage deepslate) {
        // We create a palette from the deepslate block.
        // Palette size will be adapted automatically by the respriter, but for optimal results we can alter it as well
        Palette originalPalette = Palette.fromImage(deepslate);
        // we can also use Palette.masked to only grab certain colors from the palette

        // we remove its darkest color
        originalPalette.remove(originalPalette.getDarkest());
        // we increase the palette up, adding a new bright color if palette doesnt span enough luminance
        if (originalPalette.getLuminanceSpan() < 0.2) {
            originalPalette.increaseUp();
        }
        // we also increase inner adding an inner color
        originalPalette.increaseInner();

        // gets the two closest colors, removes them and adds their average
        originalPalette.reduceAndAverage();

        RGBColor darkestColor = originalPalette.getColorAtSlope(0.2f).rgb();
        RGBColor newColor = RGBColorExample.modifyColor(darkestColor);

        // we add the new palette color
        originalPalette.add(new PaletteColor(newColor));

        return originalPalette;
    }
}
