package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.*;
import com.mojang.math.Transformation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.Objects;

public class LazyBlockstateDefinition implements ModelState {
    private final ResourceLocation modelLocation;
    private final Transformation rotation;
    private final boolean uvLock;
    private final int weight;

    public LazyBlockstateDefinition(ResourceLocation resourceLocation, Transformation transformation, boolean bl, int i) {
        this.modelLocation = resourceLocation;
        this.rotation = transformation;
        this.uvLock = bl;
        this.weight = i;
    }

    public ResourceLocation getModelLocation() {
        return this.modelLocation;
    }

    public Transformation getRotation() {
        return this.rotation;
    }

    public boolean isUvLocked() {
        return this.uvLock;
    }

    public int getWeight() {
        return this.weight;
    }

    public String toString() {
        return "Variant{modelLocation=" + this.modelLocation + ", rotation=" + this.rotation + ", uvLock=" + this.uvLock + ", weight=" + this.weight + "}";
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof net.minecraft.client.renderer.block.model.Variant)) {
            return false;
        } else {
            net.minecraft.client.renderer.block.model.Variant variant = (net.minecraft.client.renderer.block.model.Variant)object;
            return this.modelLocation.equals(variant.modelLocation) && Objects.equals(this.rotation, variant.rotation) && this.uvLock == variant.uvLock && this.weight == variant.weight;
        }
    }

    public int hashCode() {
        int i = this.modelLocation.hashCode();
        i = 31 * i + this.rotation.hashCode();
        i = 31 * i + Boolean.valueOf(this.uvLock).hashCode();
        i = 31 * i + this.weight;
        return i;
    }

    @Environment(EnvType.CLIENT)
    public static class Deserializer implements JsonDeserializer<net.minecraft.client.renderer.block.model.Variant> {
        @VisibleForTesting
        static final boolean DEFAULT_UVLOCK = false;
        @VisibleForTesting
        static final int DEFAULT_WEIGHT = 1;
        @VisibleForTesting
        static final int DEFAULT_X_ROTATION = 0;
        @VisibleForTesting
        static final int DEFAULT_Y_ROTATION = 0;

        public net.minecraft.client.renderer.block.model.Variant deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            ResourceLocation resourceLocation = this.getModel(jsonObject);
            BlockModelRotation blockModelRotation = this.getBlockRotation(jsonObject);
            boolean bl = this.getUvLock(jsonObject);
            int i = this.getWeight(jsonObject);
            return new net.minecraft.client.renderer.block.model.Variant(resourceLocation, blockModelRotation.getRotation(), bl, i);
        }

        private boolean getUvLock(JsonObject json) {
            return GsonHelper.getAsBoolean(json, "uvlock", false);
        }

        protected BlockModelRotation getBlockRotation(JsonObject json) {
            int i = GsonHelper.getAsInt(json, "x", 0);
            int j = GsonHelper.getAsInt(json, "y", 0);
            BlockModelRotation blockModelRotation = BlockModelRotation.by(i, j);
            if (blockModelRotation == null) {
                throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
            } else {
                return blockModelRotation;
            }
        }

        protected ResourceLocation getModel(JsonObject json) {
            return new ResourceLocation(GsonHelper.getAsString(json, "model"));
        }

        protected int getWeight(JsonObject json) {
            int i = GsonHelper.getAsInt(json, "weight", 1);
            if (i < 1) {
                throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
            } else {
                return i;
            }
        }
    }
}
