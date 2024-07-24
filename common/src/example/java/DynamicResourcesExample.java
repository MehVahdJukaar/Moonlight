import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
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
        ClientAssetsGenerator generator = new ClientAssetsGenerator();
        generator.register();
    }

    // Class responsible to generate assets into your dynamic pack
    public static class ClientAssetsGenerator extends DynClientResourcesGenerator {

        protected ClientAssetsGenerator() {
            //here you pass the dynamic texture pack instance
            super(new DynamicTexturePack(Moonlight.res("generated_pack"), Pack.Position.TOP, false, false));
        }

        // generate here your assets
        @Override
        public void regenerateDynamicAssets(ResourceManager manager) {

            JsonObject json = new JsonObject();
            json.addProperty("parent", "block/stone");
            // adds a random json item model
            this.dynamicPack.addItemModel(Moonlight.res("sturdy_stone_bricks"), json);

            ResourceLocation textureRes = Moonlight.res("entity/entity_texture");
            // We create another example texture and add it. The last parameter must be false for non-atlas textures
            this.dynamicPack.addAndCloseTexture(textureRes, TextureUtilsExample.createTransformedTexture(manager), false);

            // Helper method to only add a texture if it's not already there added by some pack
            this.addTextureIfNotPresent(manager, "moonlight:block/sturdy_stone_bricks",
                    () -> TextureUtilsExample.createRecoloredTexture(manager));

            // Helper object to handle resources multiple times
            StaticResource resource = StaticResource.getOrFail(manager, ResourceLocation.parse("models/block/stone_bricks.json"));

            // Helper method to add similar resources, just string replaces its content. You can also do more complex operations
            this.addSimilarJsonResource(manager, resource, "stone_bricks", "sturdy_stone_bricks");
        }

        @Override
        public void addDynamicTranslations(AfterLanguageLoadEvent languageEvent) {
            // Useful to add translation for dynamic blocks. See BlockSetExample
            languageEvent.addEntry("moonlight.test.translation", "Hello World!");
        }

        @Override
        public Logger getLogger() {
            return Moonlight.LOGGER;
        }

        @Override
        public boolean dependsOnLoadedPacks() {
            return true;
        }
    }
}
