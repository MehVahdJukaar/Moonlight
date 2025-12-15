package net.mehvahdjukaar.moonlight.api.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//wrapped tag builder for easier use
public class SimpleTagBuilder extends TagBuilder {

    private final Set<String> uniqueKeys = new HashSet<>();
    private final ResourceLocation id;

    protected SimpleTagBuilder(ResourceLocation location) {
        this.id = location;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getTagString() {
        return "#" + id.toString();
    }

    public static SimpleTagBuilder of(ResourceLocation location) {
        return new SimpleTagBuilder(location);
    }

    public static SimpleTagBuilder of(TagKey<?> key) {
        return new SimpleTagBuilder(key.location());
    }

    public void merge(SimpleTagBuilder otherBuilder) {
        this.addAll(otherBuilder.build());
    }

    public SimpleTagBuilder addAll(Collection<TagEntry> entries) {
        entries.forEach(this::add);
        return this;
    }

    @Override
    public TagBuilder add(TagEntry entry) {
        if (validateEntry(entry)) {
            return super.add(entry);
        }
        return this;
    }

    public SimpleTagBuilder add(String str) {
        if (str.startsWith("#")) {
            return this.addTag(ResourceLocation.parse(str.substring(1)));
        } else {
            return this.add(ResourceLocation.parse(str));
        }
    }

    public SimpleTagBuilder add(ResourceLocation entry) {
        super.addElement(entry);
        return this;
    }

    public TagBuilder addOptional(ResourceLocation elementLocation) {
        super.addOptionalElement(elementLocation);
        return this;
    }

    //assure entry is unique
    private boolean validateEntry(TagEntry entry) {
        if (uniqueKeys.contains(entry.toString())) return false;
        else uniqueKeys.add(entry.toString());
        return true;
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

    @Override
    public SimpleTagBuilder addTag(ResourceLocation pId) {
        super.addTag(pId);
        return this;
    }

    public SimpleTagBuilder addTag(TagKey<?> tagKey) {
        return this.addTag(tagKey.location());
    }

    @Override
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
        if (entry instanceof ResourceLocation rl) {
            this.add(rl);
            return this;
        }
        this.add(Utils.getID(entry));
        return this;
    }


    public JsonElement serializeToJson() {
        return TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(this.build(), false)).getOrThrow();
    }

    public void addFromJson(JsonObject oldTag) {
        TagFile tagfile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, oldTag)).getOrThrow();
        if (tagfile.replace()) {
            //TODO: figure oout how to remove stuff
        }
        tagfile.entries().forEach(this::add);

    }
}
