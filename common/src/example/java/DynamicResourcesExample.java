import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class DynamicResourcesExample {

    // call during mod init
    public static void init() {
        //register the generator
        RegHelper.registerDynamicResourceProvider(new ExampleModDynamicClientAssets());
    }

    // Class responsible to generate assets into your dynamic pack
    public static class ExampleModDynamicClientAssets extends DynamicClientResourceProvider {

        public ExampleModDynamicClientAssets() {
            super(Moonlight.res("example_pack"), PackGenerationStrategy.CACHED);
            //try other strategies aswell or implement your own
        }


        @Override
        protected Collection<String> gatherSupportedNamespaces() {
            return List.of("minecraft");
            //All known namespaces that the pack will support must be known beforehand
        }

        // generate here your assets
        @Override
        public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {

            //add tasks of reasonable size for max performance
            executor.accept(((manager, sink) -> {

                JsonObject json = new JsonObject();
                json.addProperty("parent", "block/stone");
                // adds a random json item model
                sink.addItemModel(Moonlight.res("sturdy_stone_bricks"), json);

                Identifier textureRes = Moonlight.res("entity/entity_texture");
                // We create another example texture and add it.
                // Remember to use try with resources with these
                try(TextureImage transformedTexture = TextureUtilsExample.createTransformedTexture(manager)) {
                    sink.addTexture(textureRes, transformedTexture);
                }

                // Helper method to only add a texture if it's not already there added by some pack. More efficent since its lazy
                sink.addTextureIfNotPresent(manager,  Moonlight.res("block/sturdy_stone_bricks"),
                        () -> TextureUtilsExample.createRecoloredTexture(manager));

                // Helper object to handle resources multiple times
                StaticResource resource = StaticResource.getOrThrow(manager, Identifier.parse("models/block/stone_bricks.json"));

                // Helper method to add similar resources, just string replaces its content. You can also do more complex operations
                sink.addSimilarJsonResource(manager, resource, "stone_bricks", "sturdy_stone_bricks");
            }));

        }

        @Override
        public void addDynamicTranslations(AfterLanguageLoadEvent languageEvent) {
            // Useful to add translation for dynamic blocks. See BlockSetExample
            languageEvent.addEntry("moonlight.test.translation", "Hello World!");
        }

    }
}
