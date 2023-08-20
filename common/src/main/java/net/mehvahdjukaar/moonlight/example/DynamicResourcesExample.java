package net.mehvahdjukaar.moonlight.example;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Logger;

public class DynamicResourcesExample {

    // call during mod init
    public static void init() {
        ClientAssets generator = new ClientAssets();
        generator.register();
    }

    public static class ClientAssets extends DynClientResourcesGenerator {

        protected ClientAssets() {
            //here you pass the dynamic texture pack instance
            super(new DynamicTexturePack(Moonlight.res("generated_pack"), Pack.Position.TOP, false, false));
        }

        @Override
        public Logger getLogger() {
            return Moonlight.LOGGER;
        }

        //
        @Override
        public boolean dependsOnLoadedPacks() {
            return true;
        }

        // generate here your assets
        @Override
        public void regenerateDynamicAssets(ResourceManager manager) {

            JsonObject json = new JsonObject();
            json.addProperty("parent", "block/stone");
            // adds a random json item model
            this.dynamicPack.addItemModel(Moonlight.res("sturdy_stone_bricks"), json);

            ResourceLocation textureRes = Moonlight.res("entity/entity_texture");
            // we create another example texture and add it. Last parameter must be false for non atlas textures
            this.dynamicPack.addAndCloseTexture(textureRes, TextureUtilsExample.createTransformedTexture(manager), false);

            // helper method to only add a texture if its not already there added by some pack
            this.addTextureIfNotPresent(manager, "moonlight:block/sturdy_stone_bricks",
                    () -> TextureUtilsExample.createRecoloredTexture(manager));

            // helper object to handle resources multiple times
            StaticResource resource = StaticResource.getOrFail(manager, new ResourceLocation("models/block/stone_bricks.json"));

            // helper method to add similar resources, just string replaces its content. You can also do more complex operations
            this.addSimilarJsonResource(manager, resource, "stone_bricks", "sturdy_stone_bricks");

        }

    }
}
