package net.mehvahdjukaar.moonlight.api.map.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//Base type for simple data-driven type. Basically a simple version of CustomDecorationType that can be serialized
public final class MLJsonMapDecorationType extends MLMapDecorationType<MLMapDecoration, SimpleMapMarker> {


    static final Codec<MLJsonMapDecorationType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            //purposefully lenient
            RuleTest.CODEC.lenientOptionalFieldOf("target_block").forGetter(MLJsonMapDecorationType::getTarget),
            ComponentSerialization.FLAT_CODEC.optionalFieldOf("name").forGetter(MLJsonMapDecorationType::getDisplayName),
            Codec.FLOAT.optionalFieldOf("rotation", 0f).forGetter(MLJsonMapDecorationType::getRotation),
            ColorUtils.CODEC.optionalFieldOf("map_color", 0).forGetter(MLJsonMapDecorationType::getDefaultMapColor),
            RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("target_structures").forGetter(
                    MLJsonMapDecorationType::getAssociatedStructure), Codec.STRING.xmap(PlatHelper::isModLoaded, b -> "minecraft")
                    .optionalFieldOf("from_mod", true)
                    .forGetter(t -> t.enabled)
    ).apply(instance, MLJsonMapDecorationType::new));

    //using this and not block predicate since it requires a worldLevelGen...
    private final Optional<RuleTest> target;
    private final Optional<Component> name;
    private final Optional<HolderSet<Structure>> structures;
    private final int defaultMapColor;
    private final float defaultRotation;

    private final boolean enabled;


    public MLJsonMapDecorationType(Optional<RuleTest> target) {
        this(target, Optional.empty(), 0, 0, true);
    }

    public MLJsonMapDecorationType(Optional<RuleTest> target, Optional<Component> name, float rotation,
                                   int mapColor, boolean enabled) {
        this(target, name, rotation, mapColor, Optional.empty(), enabled);
    }

    public MLJsonMapDecorationType(Optional<RuleTest> target, Optional<Component> name, float rotation,
                                   int mapColor, Optional<HolderSet<Structure>> structure, Boolean enabled) {
        super(SimpleMapMarker.DIRECT_CODEC, MLMapDecoration.DIRECT_CODEC);
        this.target = target;
        this.name = name;
        this.defaultRotation = rotation;
        this.structures = structure;
        this.defaultMapColor = mapColor;
        this.enabled = enabled;
    }


    public Optional<RuleTest> getTarget() {
        return target;
    }

    public Optional<Component> getDisplayName() {
        return name;
    }

    public float getRotation() {
        return defaultRotation;
    }

    public Optional<HolderSet<Structure>> getAssociatedStructure() {
        return structures;
    }

    public int getDefaultMapColor() {
        return defaultMapColor;
    }

    @Override
    public boolean isFromWorld() {
        return target.isPresent();
    }

    @Nullable
    @Override
    public SimpleMapMarker createMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        if (this.target.isPresent() && enabled) {
            if (target.get().test(reader.getBlockState(pos), RandomSource.create())) {
                Optional<Component> name = this.getDisplayName();
                if (!name.isPresent()) {
                    BlockEntity be = reader.getBlockEntity(pos);
                    if (be instanceof Nameable n) {
                        // auto names named stuff
                        name = Optional.ofNullable(n.getCustomName());
                    }
                }
                return new SimpleMapMarker(
                        this.wrapAsHolder(),
                        pos, defaultRotation, name);
            }
        }
        return null;
    }

}
