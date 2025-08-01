package net.mehvahdjukaar.moonlight.api.set.wood;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.api.set.DebugBlockTypes.appendToDebugFile;

/**
 * CHILD AVAILABLITY:
 * <p>BLOCK:</p>
 * <ul>
 * planks, log, stripped_log, wood, stripped_wood, leaves,
 * slab, stairs, fence, fence_gate, door, trapdoor,
 * button, pressure_plate, hanging_sign, wall_hanging_sign, sign, wall_sign
 * </ul>
 * <p>ITEM:</p>
 * <ul>
 * boat, chest_boat, sapling
 * </ul>
 */
public class WoodType extends BlockType {

    public static final Codec<WoodType> CODEC = ResourceLocation.CODEC.flatXmap(r -> {
                WoodType w = WoodTypeRegistry.INSTANCE.get(r);
                if (w == null) return DataResult.error(() -> "No such wood type: " + r);
                return DataResult.success(w);
            },
            t -> DataResult.success(t.id));

    public final Block planks;
    public final Block log;

    // like this so it can be called early. not too early tho as children might not be initialized
    //mega ugly. i cant initialize it immediately as mods might have not run setup yet
    private final Supplier<net.minecraft.world.level.block.state.properties.WoodType> vanillaType = Suppliers.memoize(this::detectVanillaWood);

    @Nullable
    private net.minecraft.world.level.block.state.properties.WoodType detectVanillaWood() {
        if (getChild("hanging_sign") instanceof CeilingHangingSignBlock c) {
            return c.type();
        }
        if (getChild("sign") instanceof SignBlock f) {
            return f.type();
        }
        String i = id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
        var values = net.minecraft.world.level.block.state.properties.WoodType.values();
        var o = values.filter(v -> v.name().equals(i)).findAny();
        return o.orElse(null);
    }

    public WoodType(ResourceLocation id, Block baseBlock, Block logBlock) {
        super(id);
        this.planks = baseBlock;
        this.log = logBlock;

        if (ClientConfigs.WOODTYPE_DEBUG.get() && !this.isVanilla()) appendToDebugFile(getTranslationKey());
    }

    @Override
    public ItemLike mainChild() {
        return planks;
    }

    @Nullable
    public net.minecraft.world.level.block.state.properties.WoodType toVanilla() {
        return this.vanillaType.get();
    }

    @NotNull
    public net.minecraft.world.level.block.state.properties.WoodType toVanillaOrOak() {
        var v = toVanilla();
        if (v != null) return v;
        return net.minecraft.world.level.block.state.properties.WoodType.OAK;
    }

    /**
     * Use this to get the texture path of a wood type
     *
     * @return something like minecraft/oak
     */
    public String getTexturePath() {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return this.getTypeName();
        return this.getNamespace() + "/" + this.getTypeName();
    }

    public boolean canBurn() {
        return this.planks.defaultBlockState().ignitedByLava();
    }

    public MapColor getColor() {
        return this.planks.defaultMapColor();
    }

    @Override
    public String getTranslationKey() {
        return "wood_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    public void initializeChildrenBlocks() {
        this.addChild("planks", this.planks);
        this.addChild("log", this.log);
        this.addChild("leaves", this.findRelatedEntry("leaves", BuiltInRegistries.BLOCK));
        this.addChild("wood", this.findLogRelatedBlock("", "wood", "hyphae", "bark"));
        this.addChild("stripped_log", this.findStrippedLog("log", "stem", "stalk"));
        this.addChild("stripped_wood", this.findStrippedLog("wood", "hyphae", "bark"));
        this.addChild("slab", this.findRelatedEntry("slab", BuiltInRegistries.BLOCK));
        this.addChild("stairs", this.findRelatedEntry("stairs", BuiltInRegistries.BLOCK));
        Block fence = this.findRelatedEntry("fence", BuiltInRegistries.BLOCK);
        this.addChild("fence", fence);
        this.addChild("fence_gate", this.findRelatedEntry("fence_gate", BuiltInRegistries.BLOCK));
        this.addChild("door", this.findRelatedEntry("door", BuiltInRegistries.BLOCK));
        this.addChild("trapdoor", this.findRelatedEntry("trapdoor", BuiltInRegistries.BLOCK));
        this.addChild("button", this.findRelatedEntry("button", BuiltInRegistries.BLOCK));
        this.addChild("pressure_plate", this.findRelatedEntry("pressure_plate", BuiltInRegistries.BLOCK));
        this.addChild("hanging_sign", this.findRelatedEntry("hanging_sign", BuiltInRegistries.BLOCK));
        this.addChild("wall_hanging_sign", this.findRelatedEntry("wall_hanging_sign", BuiltInRegistries.BLOCK));
        this.addChild("sign", this.findRelatedEntry("sign", BuiltInRegistries.BLOCK));
        this.addChild("wall_sign", this.findRelatedEntry("wall_sign", BuiltInRegistries.BLOCK));

        if (this.id.getNamespace().matches("tfc|afc")) { // Including unidue blocks' path
            this.addChild("sign", this.findRelatedEntry("sign", "", BuiltInRegistries.BLOCK));
            this.addChild("hanging_sign", this.findRelatedEntry("hanging_sign/wrought_sign", "", BuiltInRegistries.BLOCK));
        }

        if (fence != null && CompatHandler.DIAGONALFENCES) {
            var diagonalFence = BuiltInRegistries.BLOCK.getOptional(
                    new ResourceLocation("diagonalfences", Utils.getID(fence)
                            .toString().replace(":", "/")));
            diagonalFence.ifPresent(block -> this.addChild("diagonalfences:fence", block));
        }
    }

    @Override
    public void initializeChildrenItems() {
        this.addChild("boat", this.findRelatedEntry("boat", BuiltInRegistries.ITEM));
        this.addChild("chest_boat", this.findRelatedEntry("chest_boat", BuiltInRegistries.ITEM));
        this.addChild("sapling", this.findRelatedEntry("sapling", BuiltInRegistries.ITEM));
        if (this.id.getNamespace().matches("tfc|afc")) { // Including unidue blocks' path
            this.addChild("stick", this.findRelatedEntry("twig", BuiltInRegistries.BLOCK));
            this.addChild("boat", this.findRelatedEntry("boat", "", BuiltInRegistries.BLOCK));
        }
    }

    @Nullable
    protected <V> V findRelatedEntry(String prefix, String suffix, Registry<V> reg) {
        if (!suffix.isEmpty()) suffix = "_" + suffix;
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + prefix + suffix),
                new ResourceLocation(id.getNamespace(), prefix + "_" + id.getPath() + suffix),
                //weird conventions here
                new ResourceLocation(id.getNamespace(), id.getPath() + "_planks_" + prefix + suffix),
                // TFC & AFC: Include children of wood_type: stairs, slab...
                new ResourceLocation(id.getNamespace(), "wood/planks/" + id.getPath() + "_" + prefix),
                // TFC & AFC: Include twig (sticks), leaves, planks, sign
                new ResourceLocation(id.getNamespace(), "wood/" + prefix + suffix + "/" + id.getPath())
        };
        V found = null;
        for (var r : targets) {
            if (reg.containsKey(r)) {
                found = reg.get(r);
                break;
            }
        }
        return found;
    }


    @Nullable
    protected Block findStrippedLog(String... possibleNames) {
        for (var v : possibleNames) {
            var b = this.getBlockOfThis(v);
            if (v != null) {
                Block stripped = AxeItem.STRIPPABLES.get(b);
                if (stripped != null && stripped != b) {
                    return stripped;
                }
            }
        }
        return findLogRelatedBlock("stripped", possibleNames);
    }

    @Nullable
    protected Block findLogRelatedBlock(String prefix, String... possibleSuffix) {
        for (var n : possibleSuffix) {
            var b = findWithPrefix(prefix, n);
            if (b != null) return b;
        }
        return null;
    }

    @Nullable
    protected Block findWithPrefix(String prefix, String suffix) {
        String prefix_ = prefix.isEmpty() ? "" : prefix + "_";
        var id = this.getId();
        String logNamespace = Utils.getID(this.log).getPath();

        // SUPPORT: TFC & AFC
        String path = id.getPath();
        String namespace = id.getNamespace();
        if (this.id.getNamespace().matches("tfc|afc")) {
            var o = BuiltInRegistries.BLOCK.getOptional(
                    new ResourceLocation(namespace,
                            "wood/" + prefix_ + suffix + "/" + path));
            if (o.isPresent()) return o.get();
        }

        Set<ResourceLocation> targets = new HashSet<>();
        Collections.addAll(targets,
                new ResourceLocation(namespace, path + "_" + prefix_ + suffix),
                new ResourceLocation(namespace, prefix_ + path + "_" + suffix),
                new ResourceLocation(namespace, logNamespace + "_" + prefix_ + suffix),
                new ResourceLocation(namespace, prefix_ + logNamespace + "_" + suffix)
        );
        //For things like grimwood_wood -> grimwood
        if (path.endsWith(suffix)) {
            targets.add(new ResourceLocation(namespace, prefix_ + path));
        }
        return Utils.findFirstInRegistry(BuiltInRegistries.BLOCK, targets.toArray(new ResourceLocation[0]));
    }


    static ResourceLocation[] makeKnownIDConventions(ResourceLocation id, String... suffixKeyword) {
        List<ResourceLocation> resources = new ArrayList<>();
        for (String keyword : suffixKeyword) {
            String path = id.getPath();
            String namespace = id.getNamespace();
            resources.add(new ResourceLocation(namespace, path + "_" + keyword));
            resources.add(new ResourceLocation(namespace, keyword + "_" + path));
            //resources.add(new ResourceLocation(path + "_" + keyword));//vanilla
            //resources.add(new ResourceLocation(keyword + "_" + path)); //vanilla
        }
        return resources.toArray(new ResourceLocation[0]);
    }

    @Nullable
    static Block findLog(ResourceLocation id) {
        ResourceLocation[] tests = makeKnownIDConventions(id, "log", "stem", "stalk", "hyphae");
        return Utils.findFirstInRegistry(BuiltInRegistries.BLOCK, tests);
    }

    @Nullable
    static Block findPlanks(ResourceLocation id) {
        ResourceLocation[] tests = makeKnownIDConventions(id, "planks", "plank");
        return Utils.findFirstInRegistry(BuiltInRegistries.BLOCK, tests);
    }


    //just copies base properties without calling copy
    public BlockBehaviour.Properties copyProperties() {
        var p = BlockBehaviour.Properties.of();
        p.mapColor(this.getColor());
        if (this.canBurn()) p.ignitedByLava();
        p.sound(this.getSound());
        return p;
    }


    public static class Finder extends SetFinderBuilder<WoodType> {
        private Supplier<Block> planksFinder;
        private Supplier<Block> logFinder;

        public Finder(ResourceLocation id) {
            super(id);
            this.log(() -> findLog(id)); // defaults
            this.planks(() -> findPlanks(id)); // defaults
        }

        public Finder planks(Supplier<Block> planksFinder) {
            if (this.planksFinder != null) {
                throw new IllegalStateException("WoodType builder already has planks defined: " + id);
            }
            this.planksFinder = planksFinder;
            return this;
        }

        public Finder planks(ResourceLocation id) {
            return this.planks(() -> BuiltInRegistries.BLOCK.getOptional(id).orElseThrow());
        }

        /**
         * @param prefix include the underscore, "_" if the blockId has one
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder planksAffix(String prefix, String suffix) {
            return planks(prefix + id.getPath() + suffix);
        }

        /**
         * @param suffix include the underscore, "_" if the blockId has one
         */
        @SuppressWarnings("UnusedReturnValue")
        public Finder planksSuffix(String suffix) {
            return planks(id.getPath() + suffix);
        }

        public Finder planks(String planksName) {
            return this.planks(Utils.idWithOptionalNamespace(planksName, id.getNamespace()));
        }

        public Finder log(Supplier<Block> logFinder) {
            if (this.logFinder != null) {
                throw new IllegalStateException("WoodType builder already has a log defined: " + id);
            }
            this.logFinder = logFinder;
            return this;
        }

        public Finder log(ResourceLocation id) {
            return this.log(() -> BuiltInRegistries.BLOCK.getOptional(id).orElseThrow());
        }

        public Finder log(String logName) {
            return this.log(Utils.idWithOptionalNamespace(logName, id.getNamespace()));
        }

        /**
         * @param prefix include the underscore, "_" if the blockId has one
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder logAffix(String prefix, String suffix) {
            return log(prefix + id.getPath() + suffix);
        }

        /**
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder logSuffix(String suffix) {
            return log(id.getPath() + suffix);
        }

        @Override
        public Optional<WoodType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block plank = Preconditions.checkNotNull(planksFinder.get(), "Manual finder {} failed to find a plank block", id);
                    Block log = Preconditions.checkNotNull(logFinder.get(), "Manual finder {} failed to find a log block", id);
                    var w = new WoodType(id, plank, log);
                    childNames.forEach((key, value) -> {
                        try {
                            ItemLike obj = Preconditions.checkNotNull(value.get());
                            w.addChild(key, obj);
                        } catch (Exception e) {
                            Moonlight.LOGGER.warn("Failed to find child for wood type {}: {}. Ignoring", id, key, e);
                        }
                    });
                    return Optional.of(w);
                } catch (Exception e) {
                    Moonlight.LOGGER.warn("Failed to find custom wood type {}", id, e);
                }
            }
            return Optional.empty();
        }



        /// USE {@link WoodTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public Finder(ResourceLocation id, Supplier<Block> planks, Supplier<Block> log) {
            super(id);
            this.planksFinder = planks;
            this.logFinder = log;
        }

        /// USE {@link WoodTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public static Finder simple(String modId, String woodTypeName, String planksName, String logName) {
            return simple(new ResourceLocation(modId, woodTypeName), new ResourceLocation(modId, planksName), new ResourceLocation(modId, logName));
        }

        /// USE {@link WoodTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public static Finder simple(ResourceLocation woodTypeName, ResourceLocation planksName, ResourceLocation logName) {
            return new Finder(woodTypeName,
                    () -> BuiltInRegistries.BLOCK.get(planksName),
                    () -> BuiltInRegistries.BLOCK.get(logName));
        }

        /**
         * USE {@link WoodTypeRegistry#addSimpleFinder(String, String)}
         * <br>add {@link SetFinderBuilder#childBlockAffix(String, String, String)}
         * <br>OR
         * <br>add {@link SetFinderBuilder#childBlockSuffix(String, String)}
         */
        @Deprecated(forRemoval = true)
        public void addChild(String childType, String childName) {
            this.childBlock(childType, childName);
        }

        /**
         * USE {@link WoodTypeRegistry#addSimpleFinder(String, String)}
         * <br>add {@link SetFinderBuilder#childBlockAffix(String, String, String)}
         * <br>OR
         * <br>add {@link SetFinderBuilder#childBlockSuffix(String, String)}
         */
        @Deprecated(forRemoval = true)
        public void addChild(String childType, ResourceLocation childName) {
            this.childBlock(childType, childName);
        }

    }

}
