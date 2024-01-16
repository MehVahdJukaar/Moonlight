package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.StrOpt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//Base type for simple data-driven type. Basically a simple version of CustomDecorationType that can be serialized
public final class JsonDecorationType implements MapDecorationType<CustomMapDecoration, SimpleMapBlockMarker> {


    public static final Codec<JsonDecorationType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StrOpt.of(RuleTest.CODEC,"target_block").forGetter(JsonDecorationType::getTarget),
            StrOpt.of(Codec.STRING, "name").forGetter(JsonDecorationType::getName),
            StrOpt.of(Codec.INT,"rotation", 0).forGetter(JsonDecorationType::getRotation),
            StrOpt.of(ColorUtils.CODEC, "map_color" ,0).forGetter(JsonDecorationType::getDefaultMapColor),
            StrOpt.of(RegistryCodecs.homogeneousList(Registries.STRUCTURE), "target_structures")
                    .forGetter(JsonDecorationType::getAssociatedStructure)
    ).apply(instance, JsonDecorationType::new));

    //we cant reference other datapack registries in network codec...
    public static final Codec<JsonDecorationType> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StrOpt.of(RuleTest.CODEC, "target_block").forGetter(JsonDecorationType::getTarget),
            StrOpt.of(Codec.STRING, "name").forGetter(JsonDecorationType::getName),
            StrOpt.of(Codec.INT,"rotation" ,0).forGetter(JsonDecorationType::getRotation),
            StrOpt.of(ColorUtils.CODEC, "map_color",0).forGetter(JsonDecorationType::getDefaultMapColor)
    ).apply(instance, JsonDecorationType::new));

    //using this and not block predicate since it requires a worldLevelGen...
    @Nullable
    private final RuleTest target;

    @Nullable
    private final String name;
    @Nullable
    private final HolderSet<Structure> structures;
    private final int mapColor;
    private final int rotation;

    public JsonDecorationType(Optional<RuleTest> target) {
        this(target, Optional.empty(), 0, 0);
    }

    public JsonDecorationType(Optional<RuleTest> target, Optional<String> name, int rotation,
                              int mapColor) {
        this(target, name, rotation, mapColor, Optional.empty());
    }

    public JsonDecorationType(Optional<RuleTest> target, Optional<String> name, int rotation,
                              int mapColor, Optional<HolderSet<Structure>> structure) {
        this.target = target.orElse(null);
        this.name = name.orElse(null);
        this.rotation = rotation;
        this.structures = structure.orElse(null);
        this.mapColor = mapColor;
    }

    public Optional<RuleTest> getTarget() {
        return Optional.ofNullable(target);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public int getRotation() {
        return rotation;
    }

    public Optional<HolderSet<Structure>> getAssociatedStructure() {
        return Optional.ofNullable(structures);
    }

    public int getDefaultMapColor() {
        return mapColor;
    }

    @Override
    public boolean isFromWorld() {
        return target != null;
    }

    public ResourceLocation getId() {
        return Utils.getID(this);
    }

    @Nullable
    @Override
    public CustomMapDecoration loadDecorationFromBuffer(FriendlyByteBuf buffer) {
        try {
            return new CustomMapDecoration(this, buffer);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load custom map decoration for decoration type" + this.getId() + ": " + e);
        }
        return null;
    }

    @Nullable
    @Override
    public SimpleMapBlockMarker loadMarkerFromNBT(CompoundTag compound) {
        SimpleMapBlockMarker marker = new SimpleMapBlockMarker(this);
        try {
            marker.loadFromNBT(compound);
            return marker;
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load world map marker for decoration type" + this.getId() + ": " + e);
        }
        return null;
    }

    @Nullable
    @Override
    public SimpleMapBlockMarker getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        if (this.target != null) {
            if (target.test(reader.getBlockState(pos), RandomSource.create())) {
                SimpleMapBlockMarker m = createEmptyMarker();
                m.setPos(pos);
                return m;
            }
        }
        return null;
    }


    @Override
    public SimpleMapBlockMarker createEmptyMarker() {
        var m = new SimpleMapBlockMarker(this);
        m.setRotation(rotation);
        m.setName(name == null ? null : Component.translatable(name));
        return m;
    }

}
