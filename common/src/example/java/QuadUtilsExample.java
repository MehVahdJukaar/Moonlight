import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;

import java.util.List;

// Showcases the use of QuadTransformer and QuadBuilder classes
public class QuadUtilsExample {


    public static List<BakedQuad> transformQuads(List<BakedQuad> original) {
        // we use this to transform existing quads. it's better if you store it in a class field,
        // so it won't be created every time
        BakedQuadsTransformer transformer = BakedQuadsTransformer.create();
        Matrix4f mat = new Matrix4f();
        mat.translate(0, 1, 0);
        // changing model sprite
        transformer.applyingSprite(Minecraft.getInstance().getPaintingTextures().getBackSprite());
        // apply matrix transform
        transformer.applyingTransform(mat);
        // adds emissivity
        transformer.applyingEmissivity(2);
        // applying the transformation
        return transformer.transformAll(original);
    }

    public static void addCube(List<BakedQuad> quads, TextureAtlasSprite sprite) {
        // Works just like Forge one
        BakedQuadBuilder builder = BakedQuadBuilder.create(sprite);
        builder.setAutoDirection();
        // needed so all finished quads will be added to the list
        builder.setAutoBuild(quads::add);
        // you can also use this helper with vanilla vertex consumers in your entity renderers
        PoseStack pose = new PoseStack();
        pose.translate(0, 0, 1);
        // Adding a simple cube using helper function. You can also add vertices manually
        VertexUtil.addCube(builder, pose, 0, 0, 0.2f, 0.2f, 0, 0);
    }

}
