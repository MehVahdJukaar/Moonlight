package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

//Base type for simple data-driven type. Basically a simple version of CustomDecorationType that can be serialized
public final class MLJsonMapDecorationType implements MlMapDecorationType<MLMapDecoration, SimpleMapBlockMarker> {


    static final Codec<MLJsonMapDecorationType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.optionalFieldOf("target_block").forGetter(MLJsonMapDecorationType::getTarget),
            Codec.STRING.optionalFieldOf("name").forGetter(MLJsonMapDecorationType::getName),
            Codec.INT.optionalFieldOf("rotation", 0).forGetter(MLJsonMapDecorationType::getRotation),
            ColorUtils.CODEC.optionalFieldOf("map_color", 0).forGetter(MLJsonMapDecorationType::getDefaultMapColor),
            RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("target_structures").forGetter(
                    MLJsonMapDecorationType::getAssociatedStructure), Codec.STRING.xmap(PlatHelper::isModLoaded, b -> "minecraft")
                    .optionalFieldOf("from_mod", true)
                    .forGetter(t -> t.enabled)
    ).apply(instance, MLJsonMapDecorationType::new));

    //we cant reference other data pack registries in network codec...
    static final Codec<MLJsonMapDecorationType> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.optionalFieldOf("target_block").forGetter(MLJsonMapDecorationType::getTarget),
            Codec.STRING.optionalFieldOf("name").forGetter(MLJsonMapDecorationType::getName),
            Codec.INT.optionalFieldOf("rotation", 0).forGetter(MLJsonMapDecorationType::getRotation),
            ColorUtils.CODEC.optionalFieldOf("map_color", 0).forGetter(MLJsonMapDecorationType::getDefaultMapColor),
            Codec.BOOL.fieldOf("enabled").forGetter(t -> t.enabled)
    ).apply(instance, MLJsonMapDecorationType::new));

    //using this and not block predicate since it requires a worldLevelGen...
    @Nullable
    private final RuleTest target;

    @Nullable
    private final String name;
    @Nullable
    private final HolderSet<Structure> structures;
    private final int mapColor;
    private final int rotation;
    private final boolean enabled;

    private final StreamCodec<RegistryFriendlyByteBuf, MLMapDecoration> decorationCodec;

    public MLJsonMapDecorationType(Optional<RuleTest> target) {
        this(target, Optional.empty(), 0, 0, true);
    }

    public MLJsonMapDecorationType(Optional<RuleTest> target, Optional<String> name, int rotation,
                                   int mapColor, boolean enabled) {
        this(target, name, rotation, mapColor, Optional.empty(), enabled);
    }

    public MLJsonMapDecorationType(Optional<RuleTest> target, Optional<String> name, int rotation,
                                   int mapColor, Optional<HolderSet<Structure>> structure, Boolean enabled) {
        this.target = target.orElse(null);
        this.name = name.orElse(null);
        this.rotation = rotation;
        this.structures = structure.orElse(null);
        this.mapColor = mapColor;
        this.enabled = enabled;

        this.decorationCodec = MLMapDecoration.DIRECT_CODEC.map(d -> {
            d.type = this;
            return d;
        }, Function.identity());
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

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends MLMapDecoration> getDecorationCodec() {
        return decorationCodec;
    }

    @Nullable
    @Override
    public SimpleMapBlockMarker load(CompoundTag compound, HolderLookup.Provider registries) {
        SimpleMapBlockMarker marker = new SimpleMapBlockMarker(this);
        try {
            marker.load(compound, registries);
            return marker;
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load world map marker for decoration type" + this.getId() + ": " + e);
        }
        return null;
    }

    @Nullable
    @Override
    public SimpleMapBlockMarker getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        if (this.target != null && enabled) {
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
