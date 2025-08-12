import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

public class TextureUtilsExample {

    public static TextureImage createRecoloredTexture(ResourceManager manager) {

        // We start by creating textureImages, helper objects that we use to transform images
        try (TextureImage stone = TextureImage.open(manager, ResourceLocation.parse("block/stone"));
             TextureImage deepslate = TextureImage.open(manager, ResourceLocation.parse("block/deepslate"))) {

            // Respriter object that will create textures with the shape of a "blocks/stone" texture using the colors we feed into it
            Respriter respriter = Respriter.of(stone);

            Palette deepslatePalette = PaletteExample.modifyPaletteExample(deepslate);

            // Respriter object is used to create a new texture (which must be closed)
            TextureImage newImage = respriter.recolor(deepslatePalette);

            // We can alter the texture directly
            newImage.setFramePixel(0, 1, 1, 0xff0000ff);


            return newImage;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public static TextureImage createTransformedTexture(ResourceManager manager) {

        try {
            // Helper to get the first texture of a block
            ResourceLocation diamondRes = RPUtils.findFirstBlockTextureLocation(manager, Blocks.DIAMOND_BLOCK);
            try (TextureImage diamond = TextureImage.open(manager, diamondRes);
                 TextureImage pick = TextureImage.open(manager, ResourceLocation.parse("item/diamond_pickaxe"));
                 TextureImage emerald = TextureImage.open(manager, ResourceLocation.parse("block/emerald"))) {

                // New image is created, so we can keep using old ones
               try( TextureImage newImage = diamond.makeCopy()) {
                   ;
                   // grayscale the image
                   TextureOps.grayscale(newImage);

                   // We apply an overlay to the texture, drawing a diamond pick on it
                   TextureOps.applyOverlay(newImage, pick);

                   // Here an image transformer object is created; It can copy parts of a texture onto another
                   // In this case it will copy a square from the center of the emerald textures to the two upper cornets of our one
                   TextureCollager transformer = TextureCollager.builder(16, 16, 16, 16)
                           .copyFrom(6, 6, 4, 4)
                           .to(12, 0)
                           .flippedX()
                           .copyFrom(0,0,4,5)
                           .to(2,3)
                           .rotated(Rotation.CLOCKWISE_90)
                           .build();

                   transformer.apply(emerald, newImage);

                   return newImage;
               }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


}
