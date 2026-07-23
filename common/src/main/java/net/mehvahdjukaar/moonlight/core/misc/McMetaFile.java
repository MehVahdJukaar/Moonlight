package net.mehvahdjukaar.moonlight.core.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A parsed .png.mcmeta. A null animation means the file had no "animation" section at all,
 * which is the common case for connected texture mods that only use it to carry their own data.
 * That is NOT the same as a present but defaulted section, so it must not be collapsed into
 * {@link AnimationMetadataSection#EMPTY}: doing so makes a ctm-only file look animated and
 * ends up writing out an animation with no frames.
 */
public record McMetaFile(@Nullable AnimationMetadataSection animation, JsonObject moddedStuff) {

    public static McMetaFile of(@NotNull AnimationMetadataSection vanillaMcmeta) {
        return of(vanillaMcmeta, new JsonObject());
    }

    public static McMetaFile of(@NotNull AnimationMetadataSection vanillaMcmeta, JsonObject moddedStuff) {
        //old api used to pass EMPTY around to mean "no animation"
        return new McMetaFile(vanillaMcmeta == AnimationMetadataSection.EMPTY ? null : vanillaMcmeta, moddedStuff);
    }

    public static McMetaFile read(Resource resource) throws IOException {
        try (InputStream metadataStream = resource.open()) {
            var bytes = metadataStream.readAllBytes();
            AnimationMetadataSection metadata = AbstractPackResources.getMetadataFromStream(AnimationMetadataSection.SERIALIZER, new ByteArrayInputStream(bytes));
            JsonObject moddedObj = readModdedObj(bytes);
            return new McMetaFile(metadata, moddedObj);
        }
    }

    private static JsonObject readModdedObj(byte[] bytes) {
        // read json from bytes
        JsonObject jo = GsonHelper.parse(new String(bytes));
        // remove vanilla fields. animation is parsed separately and re serialized by toJson
        for (String key : new String[]{"animation", "frametime", "width", "height", "interpolate", "frames"}) {
            jo.remove(key);
        }
        return jo;
    }

    public static @Nullable McMetaFile merge(@Nullable McMetaFile mostImportant, @Nullable McMetaFile leastImportant) {
        if (mostImportant == null && leastImportant == null) return null;
        if (leastImportant == null) return mostImportant;
        if (mostImportant == null) return leastImportant;
        if (!mostImportant.hasAnimation()) {
            return new McMetaFile(leastImportant.animation, mostImportant.moddedStuff);
        }
        return mostImportant;
    }

    public boolean hasAnimation() {
        return this.animation != null;
    }

    /**
     * How many frames a texture must have for this animation's frame indices to be valid.
     * 0 when there's no explicit frame list, meaning the animation just plays every frame in order
     */
    public int requiredFrameCount() {
        if (animation == null) return 0;
        int[] highest = {-1};
        animation.forEachFrame((i, t) -> highest[0] = Math.max(highest[0], i));
        return highest[0] + 1;
    }

    //Note that these can be different from TextureImage.frameWidth
    public int getAnimationFrameWidth() {
        return this.animation == null ? AnimationMetadataSection.UNKNOWN_SIZE : this.animation.frameWidth;
    }

    public int getAnimationFrameHeight() {
        return this.animation == null ? AnimationMetadataSection.UNKNOWN_SIZE : this.animation.frameHeight;
    }

    public JsonObject toJson() {
        JsonObject obj = moddedStuff.deepCopy();
        if (animation == null) return obj;

        JsonObject animObj = new JsonObject();

        animObj.addProperty("frametime", animation.getDefaultFrameTime());
        animObj.addProperty("interpolate", animation.isInterpolatedFrames());
        animObj.addProperty("height", animation.frameHeight);
        animObj.addProperty("width", animation.frameWidth);

        JsonArray frames = new JsonArray();

        animation.forEachFrame((i, t) -> {
            if (t != -1) {
                JsonObject o = new JsonObject();
                o.addProperty("time", t);
                o.addProperty("index", i);
                frames.add(o);
            } else frames.add(i);
        });

        //an empty list means "every frame in order", which is only valid if we omit it entirely
        if (!frames.isEmpty()) animObj.add("frames", frames);

        obj.add("animation", animObj);

        return obj;
    }

    public McMetaFile copy() {
        return new McMetaFile(animation, moddedStuff.deepCopy());
    }

    public McMetaFile cloneWithSize(int frameWidth, int frameHeight) {
        if (this.animation == null) return copy();
        List<AnimationFrame> frameData = new ArrayList<>();
        this.animation.forEachFrame((i, t) -> frameData.add(new AnimationFrame(i, t)));
        AnimationMetadataSection newMetadata = new AnimationMetadataSection(frameData, frameWidth, frameHeight,
                this.animation.getDefaultFrameTime(), this.animation.isInterpolatedFrames());
        JsonObject newModdedStuff = moddedStuff.deepCopy();
        return new McMetaFile(newMetadata, newModdedStuff);
    }

    private static List<Field> FIELDS = null;
    private static final int VANILLA_FIELDS = 5;

    public static void copyAllMixinAddedFields(AnimationMetadataSection from, AnimationMetadataSection to) {
        if (FIELDS == null) {
            FIELDS = new ArrayList<>();
            Field[] f = AnimationMetadataSection.class.getDeclaredFields();
            for (int i = 0; i < f.length; i++) {
                if (i > VANILLA_FIELDS - 1) {
                    Field field = f[i];
                    FIELDS.add(field);
                    field.setAccessible(true);
                }
            }
        }
        for (Field field : FIELDS) {
            try {
                field.set(to, field.get(from));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
