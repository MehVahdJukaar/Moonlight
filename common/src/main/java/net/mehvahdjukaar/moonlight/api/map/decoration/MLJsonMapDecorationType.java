package net.mehvahdjukaar.moonlight.api.map.decoration;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
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

    private static final ResourceLocation FACTORY_ID = Moonlight.res("json_decoration_type");
    static final Codec<MLJsonMapDecorationType> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                //purposefully lenient
                RuleTest.CODEC.lenientOptionalFieldOf("target_block").forGetter(MLJsonMapDecorationType::getTarget),
                ComponentSerialization.FLAT_CODEC.optionalFieldOf("name").forGetter(MLJsonMapDecorationType::getDisplayName),
                Codec.FLOAT.optionalFieldOf("rotation", 0f).forGetter(MLJsonMapDecorationType::getRotation),
                ColorUtils.CODEC.optionalFieldOf("map_color", 0).forGetter(MLJsonMapDecorationType::getDefaultMapColor),
                //purposefully lenient for the client codec so we silently fail and dont send info we dont need as they rely on tags and we arent given registry ops there
                RegistryCodecs.homogeneousList(Registries.STRUCTURE).lenientOptionalFieldOf("target_structures").forGetter(
                        MLJsonMapDecorationType::getAssociatedStructure)
        ).apply(instance, MLJsonMapDecorationType::new));
    }

    //using this and not block predicate since it requires a worldLevelGen...
    private final Optional<RuleTest> target;
    private final Optional<Component> name;
    private final Optional<HolderSet<Structure>> structures;
    private final int defaultMapColor;
    private final float defaultRotation;


    public MLJsonMapDecorationType(Optional<RuleTest> target) {
        this(target, Optional.empty(), 0, 0);
    }

    public MLJsonMapDecorationType(Optional<RuleTest> target, Optional<Component> name, float rotation, int mapColor) {
        this(target, name, rotation, mapColor, Optional.empty());
    }

    public MLJsonMapDecorationType(Optional<RuleTest> target, Optional<Component> name, float rotation,
                                   int mapColor, Optional<HolderSet<Structure>> structure) {
        super(SimpleMapMarker.DIRECT_CODEC, MLMapDecoration.DIRECT_CODEC);
        this.target = target;
        this.name = name;
        this.defaultRotation = rotation;
        this.structures = structure;
        this.defaultMapColor = mapColor;
    }

    @Override
    public ResourceLocation getCustomFactoryID() {
        return FACTORY_ID;
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
        if (this.target.isPresent()) {
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
