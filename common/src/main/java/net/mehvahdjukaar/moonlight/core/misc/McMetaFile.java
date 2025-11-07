package net.mehvahdjukaar.moonlight.core.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
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

//TODO: move out of here
public record McMetaFile(@NotNull AnimationMetadataSection animation, JsonObject moddedStuff) {

    public static McMetaFile of(@NotNull AnimationMetadataSection vanillaMcmeta) {
        return new McMetaFile(vanillaMcmeta, new JsonObject());
    }

    public static McMetaFile of(@NotNull AnimationMetadataSection vanillaMcmeta, JsonObject moddedStuff) {
        return new McMetaFile(vanillaMcmeta, moddedStuff);
    }

    public static McMetaFile read(Resource resource) throws IOException {
        try (InputStream metadataStream = resource.open()) {
            var bytes = metadataStream.readAllBytes();
            AnimationMetadataSection metadata = AbstractPackResources.getMetadataFromStream(AnimationMetadataSection.SERIALIZER, new ByteArrayInputStream(bytes));
            if (metadata == null) {
                metadata = AnimationMetadataSection.EMPTY;
            }
            JsonObject moddedObj = readModdedObj(bytes);
            return new McMetaFile(metadata, moddedObj);
        }
    }

    private static JsonObject readModdedObj(byte[] bytes) {
        // read json from bytes
        JsonObject jo = GsonHelper.parse(new String(bytes));
        // remove vanilla fields
        for (String key : new String[]{"frametime", "width", "height", "interpolate", "frames"}) {
            jo.remove(key);
        }
        return jo;
    }

    public static @Nullable McMetaFile merge(@Nullable McMetaFile mostImportant, @Nullable McMetaFile leastImportant) {
        if (mostImportant == null && leastImportant == null) return null;
        if (leastImportant == null) return mostImportant;
        if (mostImportant == null) return leastImportant;
        if (mostImportant.animation == AnimationMetadataSection.EMPTY) {
            return of(leastImportant.animation, mostImportant.moddedStuff);
        }
        return mostImportant;
    }

    public JsonObject toJson() {
        JsonObject obj = moddedStuff.deepCopy();

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

        animObj.add("frames", frames);

        obj.add("animation", animObj);

        return obj;
    }

    public McMetaFile cloneWithSize(int frameWidth, int frameHeight) {
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
