import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Items;

public class RenderedTextureExample {


    // this class allows to run any block or any object into a flat texture we can use in our renderers.
    // Useful for stuff like labels
    public static void render(MultiBufferSource buffer){
        // In this example we are requestin a flat texture for a diamond block
        FrameBufferBackedDynamicTexture texture =
                RenderedTexturesManager.requestFlatItemTexture(Items.DIAMOND_BLOCK, 32, i->{});

        if(texture.isInitialized()){
            // draw texture as normal
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(texture.getTextureLocation()));
            //... draw texture with vertex consumer
        }


        // request more complex model
        var texture2 = RenderedTexturesManager.requestTexture(Moonlight.res("unique_complex_texture_id"),
              64, t->{
                    // draw stuff onto the texture.
                    // See RenderedTexturesManager.drawTexture for an example
                }, true);
    }
}
