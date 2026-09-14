package net.mehvahdjukaar.moonlight.core.criteria_triggers;

import net.mehvahdjukaar.moonlight.api.client.PostShadersHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;

public class PostShadersExample {

    //applies first
    private static final PostShadersHelper.Group LENSES_GROUP = new PostShadersHelper.Group(
            Moonlight.res("contact_lenses"), 0
    );

    //applies later on
    private static final PostShadersHelper.Group BINOCULAR_GROUP = new PostShadersHelper.Group(
            Moonlight.res("binocular_effect"), 10
    );

    private static final ResourceLocation LENS_POST = Moonlight.res("shaders/post/colored_contact.json");
    private static final ResourceLocation BINOCULAR_POST = ResourceLocation.withDefaultNamespace("shaders/post/binocular_fisheye.json");

    // register with loader events
    public static void onClientTick() {
        boolean hasBinoculars = false;
        boolean hasContactLenses = false;

        PostShadersHelper.toggleEffect(hasBinoculars ? null : BINOCULAR_POST, BINOCULAR_GROUP);
        PostShadersHelper.toggleEffect(hasContactLenses ? null : LENS_POST, LENSES_GROUP);
    }
}