package net.mehvahdjukaar.moonlight.core.misc;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// null animation means no "animation" section at all (ctm mods use mcmeta for their own data). Not the same as a
// present but defaulted one, or a ctm-only file would be written out as animated with no frames
public record McMetaFile(@Nullable AnimationMetadataSection animation, JsonObject moddedStuff) {

    public static final int UNKNOWN_SIZE = -1;

    public static McMetaFile of(@NotNull AnimationMetadataSection vanillaMcmeta) {
        return of(vanillaMcmeta, new JsonObject());
    }

    public static McMetaFile of(@NotNull AnimationMetadataSection vanillaMcmeta, JsonObject moddedStuff) {
        return new McMetaFile(vanillaMcmeta, moddedStuff);
    }

    public static McMetaFile read(Resource resource) throws IOException {
        try (InputStream metadataStream = resource.open()) {
            var bytes = metadataStream.readAllBytes();
            JsonObject json = GsonHelper.parse(new String(bytes));
            AnimationMetadataSection metadata = json.has("animation")
                    ? AnimationMetadataSection.CODEC.parse(JsonOps.INSTANCE, json.get("animation")).getOrThrow(IOException::new)
                    : null;
            return new McMetaFile(metadata, readModdedObj(bytes));
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

    // 0 when there is no explicit frame list
    public int requiredFrameCount() {
        if (animation == null) return 0;
        int highest = -1;
        for (AnimationFrame frame : animation.frames().orElse(List.of())) {
            highest = Math.max(highest, frame.index());
        }
        return highest + 1;
    }

    //Note that these can be different from TextureImage.frameWidth
    public int getAnimationFrameWidth() {
        return this.animation == null ? UNKNOWN_SIZE : this.animation.frameWidth().orElse(UNKNOWN_SIZE);
    }

    public int getAnimationFrameHeight() {
        return this.animation == null ? UNKNOWN_SIZE : this.animation.frameHeight().orElse(UNKNOWN_SIZE);
    }

    public JsonObject toJson() {
        JsonObject obj = moddedStuff.deepCopy();
        if (animation == null) return obj;
        obj.add("animation", AnimationMetadataSection.CODEC.encodeStart(JsonOps.INSTANCE, animation).getOrThrow());
        return obj;
    }

    public McMetaFile copy() {
        return new McMetaFile(animation, moddedStuff.deepCopy());
    }

    public McMetaFile cloneWithSize(int frameWidth, int frameHeight) {
        if (this.animation == null) return copy();
        AnimationMetadataSection newMetadata = new AnimationMetadataSection(
                this.animation.frames().map(ArrayList::new).map(l -> (List<AnimationFrame>) l),
                Optional.of(frameWidth), Optional.of(frameHeight),
                this.animation.defaultFrameTime(), this.animation.interpolatedFrames());
        return new McMetaFile(newMetadata, moddedStuff.deepCopy());
    }

}
