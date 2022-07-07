package net.mehvahdjukaar.moonlight.api.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;

import java.util.Collection;

//wrapped tag builder for easier use
public class SimpleTagBuilder extends TagBuilder {

    private final ResourceLocation id;

    protected SimpleTagBuilder(ResourceLocation location) {
        this.id = location;
    }

    public ResourceLocation getId() {
        return id;
    }

    public static SimpleTagBuilder of(ResourceLocation location) {
        return new SimpleTagBuilder(location);
    }

    public static SimpleTagBuilder of(TagKey<?> key) {
        return new SimpleTagBuilder(key.location());
    }

    public SimpleTagBuilder add(ResourceLocation entry) {
        super.addElement(entry);
        return this;
    }

    //Forge stuff. we arent using it
    /*
    @Override
    public SimpleTagBuilder replace(boolean value) {
        super.replace(value);
        return this;
    }

    @Override
    public SimpleTagBuilder replace() {
        super.replace();
        return this;
    }*/

    public SimpleTagBuilder addTag(ResourceLocation pId) {
        super.addTag(pId);
        return this;
    }

    public SimpleTagBuilder addTag(TagKey<?> tagKey) {
        return this.addTag(tagKey.location());
    }

    public SimpleTagBuilder addOptionalTag(ResourceLocation pId) {
        super.addOptionalTag(pId);
        return this;
    }

    public SimpleTagBuilder addTag(SimpleTagBuilder otherBuilder) {
        return this.addTag(otherBuilder.getId());
    }

    public SimpleTagBuilder addEntries(Collection<?> entries) {
        entries.forEach(e -> this.add(Utils.getID(e)));
        return this;
    }

    public SimpleTagBuilder addEntry(Object entry) {
        this.add(Utils.getID(entry));
        return this;
    }


    public JsonElement serializeToJson() {
        return TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(this.build(), false))
                .getOrThrow(false, Moonlight.LOGGER::error);
    }

    public void addFromJson(JsonObject oldTag) {
        TagFile tagfile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, oldTag))
                .getOrThrow(false, Moonlight.LOGGER::error);
        if (tagfile.replace()) {
            //TODO: figure oout how to remove stuff
        }
        tagfile.entries().forEach(this::add);

    }
}
