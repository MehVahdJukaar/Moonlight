package net.mehvahdjukaar.selene.resourcepack.resources;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;

//wrapped tag builder for easier use
public class TagBuilder extends Tag.Builder {

    private final ResourceLocation id;

    private TagBuilder(ResourceLocation location) {
        this.id = location;
    }

    public ResourceLocation getId() {
        return id;
    }

    public static TagBuilder of(ResourceLocation location) {
        return new TagBuilder(location);
    }

    public static TagBuilder of(TagKey<?> key) {
        return new TagBuilder(key.location());
    }

    private static final String SOURCE = "dyn_pack";


    public TagBuilder add(ResourceLocation entry) {
        if (entry.toString().equals("minecraft:air")) {
            throw new UnsupportedOperationException("Tried to tag air block. This is bad");
        }
        super.addElement(entry, SOURCE);
        return this;
    }

    public TagBuilder replace(boolean value) {
        super.replace(value);
        return this;
    }

    public TagBuilder replace() {
        super.replace();
        return this;
    }

    public TagBuilder addTag(ResourceLocation pId) {
        super.addTag(pId, SOURCE);
        return this;
    }

    public TagBuilder addTag(TagKey<?> tagKey) {
        return this.addTag(tagKey.location());
    }

    public TagBuilder addOptionalTag(ResourceLocation pId) {
        super.addOptionalTag(pId, SOURCE);
        return this;
    }

    public TagBuilder addTag(TagBuilder otherBuilder) {
        return this.addTag(otherBuilder.getId());
    }

    public <V extends IForgeRegistryEntry<V>, T extends ForgeRegistryEntry<V>> TagBuilder addEntries(Collection<T> entries) {
        entries.forEach(e -> this.add(e.getRegistryName()));
        return this;
    }

    public <V extends IForgeRegistryEntry<V>, T extends ForgeRegistryEntry<V>> TagBuilder addEntry(T entry) {
        this.add(entry.getRegistryName());
        return this;
    }


    public TagBuilder addFromJson(JsonObject pJson) {
        super.addFromJson(pJson, SOURCE);
        return this;
    }

    @Override
    public JsonObject serializeToJson() {
        return super.serializeToJson();
    }


}
