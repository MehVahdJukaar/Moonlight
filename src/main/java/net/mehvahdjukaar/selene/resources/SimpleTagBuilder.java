package net.mehvahdjukaar.selene.resources;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.selene.util.Utils;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

import java.util.Collection;

//wrapped tag builder for easier use
public class SimpleTagBuilder extends TagBuilder {

    private final ResourceLocation id;

    private SimpleTagBuilder(ResourceLocation location) {
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

    public SimpleTagBuilder replace(boolean value) {
        super.replace(value);
        return this;
    }

    public SimpleTagBuilder replace() {
        super.replace();
        return this;
    }

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

    public SimpleTagBuilder addEntries(Collection<Object> entries) {
        entries.forEach(e -> this.add(Utils.getID(e)));
        return this;
    }

    public SimpleTagBuilder addEntry(Object entry) {
        this.add(Utils.getID(entry));
        return this;
    }


}
