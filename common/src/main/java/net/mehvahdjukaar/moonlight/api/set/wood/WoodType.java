package net.mehvahdjukaar.moonlight.api.set.wood;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodChildKeys.*;

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
        this.addChild(PLANKS, this.planks);
        this.addChild(LOG, this.log);
        this.addChild(LEAVES, this.findRelatedEntry("leaves", BuiltInRegistries.BLOCK));
        this.addChild(WOOD, this.findLogRelatedBlock("", "wood", "hyphae", "bark"));
        this.addChild(STRIPPED_LOG, this.findStrippedLog("log", "stem", "stalk"));
        this.addChild(STRIPPED_WOOD, this.findStrippedLog("wood", "hyphae", "bark"));
        this.addChild(SLAB, this.findRelatedEntry("slab", BuiltInRegistries.BLOCK));
        this.addChild(STAIRS, this.findRelatedEntry("stairs", BuiltInRegistries.BLOCK));
        Block fence = this.findRelatedEntry("fence", BuiltInRegistries.BLOCK);
        this.addChild(FENCE, fence);
        this.addChild(FENCE_GATE, this.findRelatedEntry("fence_gate", BuiltInRegistries.BLOCK));
        this.addChild(DOOR, this.findRelatedEntry("door", BuiltInRegistries.BLOCK));
        this.addChild(TRAPDOOR, this.findRelatedEntry("trapdoor", BuiltInRegistries.BLOCK));
        this.addChild(BUTTON, this.findRelatedEntry("button", BuiltInRegistries.BLOCK));
        this.addChild(PRESSURE_PLATE, this.findRelatedEntry("pressure_plate", BuiltInRegistries.BLOCK));
        this.addChild(HANGING_SIGN, this.findRelatedEntry("hanging_sign", BuiltInRegistries.BLOCK));
        this.addChild(WALL_HANGING_SIGN, this.findRelatedEntry("wall_hanging_sign", BuiltInRegistries.BLOCK));
        this.addChild(SIGN, this.findRelatedEntry("sign", BuiltInRegistries.BLOCK));
        this.addChild(WALL_SIGN, this.findRelatedEntry("wall_sign", BuiltInRegistries.BLOCK));

        if (this.id.getNamespace().matches("tfc|afc")) { // Including unidue blocks' path
            this.addChild(SIGN, this.findRelatedEntry("sign", "", BuiltInRegistries.BLOCK));
            this.addChild(HANGING_SIGN, this.findRelatedEntry("hanging_sign/wrought_sign", "", BuiltInRegistries.BLOCK));
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
        this.addChild(BOAT, this.findRelatedItem("boat", "raft"));
        this.addChild(CHEST_BOAT, this.findRelatedItem("chest_boat", "chest_raft"));
        this.addChild(SAPLING, this.findRelatedEntry("sapling", BuiltInRegistries.ITEM));
        if (this.id.getNamespace().matches("tfc|afc")) { // Including unidue blocks' path
            this.addChild(STICK, this.findRelatedEntry("twig", BuiltInRegistries.BLOCK));
            this.addChild(BOAT, this.findRelatedEntry("boat", "", BuiltInRegistries.BLOCK));
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
        return Utils.findFirstInRegistry(reg, targets);
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
    protected Item findRelatedItem(String... names) {
        for (var n : names) {
            var b = findRelatedEntry(n, BuiltInRegistries.ITEM);
            if (b != null) return b;
        }
        return null;
    }

    @Nullable
    protected Block findLogRelatedBlock(String prefix, String... possibleSuffix) {
        for (var n : possibleSuffix) {
            var b = findLogWithPrefix(prefix, n);
            if (b != null) return b;
        }
        return null;
    }

    @Nullable
    protected Block findLogWithPrefix(String prefix, String suffix) {
        // ugly, shouldnt be here
        // SUPPORT: TFC & AFC
        if (this.id.getNamespace().matches("tfc|afc")) {
            String prefix_ = prefix.isEmpty() ? "" : prefix + "_";
            var o = BuiltInRegistries.BLOCK.getOptional(
                    new ResourceLocation(this.getNamespace(),
                            "wood/" + prefix_ + suffix + "/" + id.getPath()));
            if (o.isPresent()) return o.get();
        }

        List<ResourceLocation> targets = makeKnownIDConventionsPrefix(
                this.id.getNamespace(), this.id.getPath(),
                prefix, suffix, Utils.getID(this.log).getPath());
        return Utils.findFirstInRegistry(BuiltInRegistries.BLOCK, targets);
    }

    private static @NotNull List<ResourceLocation> makeKnownIDConventionsPrefix(
            String myNamespace, String myPath,
            String prefix, String suffix,
            String alternateNamespace) {

        boolean noneEmpty = !prefix.isEmpty() && !suffix.isEmpty();
        String prefix_ = prefix.isEmpty() ? "" : prefix + "_";

        List<ResourceLocation> targets = new ArrayList<>();
        targets.add(new ResourceLocation(myNamespace, myPath + "_" + prefix_ + suffix));
        targets.add(new ResourceLocation(myNamespace, alternateNamespace + "_" + prefix_ + suffix));
        if (!noneEmpty) {
            targets.add(new ResourceLocation(myNamespace, prefix_ + myPath + "_" + suffix));
            targets.add(new ResourceLocation(myNamespace, prefix_ + alternateNamespace + "_" + suffix));
        }

        //For things like grimwood_wood -> grimwood
        if (myPath.endsWith(suffix)) {
            targets.add(new ResourceLocation(myNamespace, prefix_ + myPath));
        }
        return targets;
    }

    static List<ResourceLocation> makeKnownIDConventions(ResourceLocation id, String... suffixKeyword) {
        String myPath = id.getPath();
        String myNamespace = id.getNamespace();
        List<ResourceLocation> resources = new ArrayList<>();
        for (String suffix : suffixKeyword) {
            resources.addAll(makeKnownIDConventionsPrefix(myNamespace, myPath, "", suffix, myPath));
        }
        return resources;
    }

    @Nullable
    static Block findLog(ResourceLocation id) {
        var tests = makeKnownIDConventions(id, "log", "stem", "stalk", "hyphae");
        return Utils.findFirstInRegistry(BuiltInRegistries.BLOCK, tests);
    }

    @Nullable
    static Block findPlanks(ResourceLocation id) {
        var tests = makeKnownIDConventions(id, "planks", "plank");
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


    //TODO:move in wood registry class
    public static class Finder extends SetFinderBuilder<WoodType> {
        private Supplier<Block> planksFinder;
        private Supplier<Block> logFinder;

        public Finder(ResourceLocation id) {
            super(id, WoodTypeRegistry.INSTANCE);
            this.log(() -> findLog(id)); // defaults
            this.planks(() -> findPlanks(id)); // defaults
        }

        /// PLANKS \\\
        public Finder planks(Supplier<Block> planksFinder) {
            this.planksFinder = planksFinder;
            return this;
        }

        public Finder planks(ResourceLocation id) {
            return this.planks(() -> BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(
                    () -> new IllegalStateException("Failed to find planks block: " + id)
            ));
        }

        public Finder planks(String planksName) {
            return this.planks(Utils.idWithOptionalNamespace(planksName, id.getNamespace()));
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

        /// LOG \\\
        public Finder log(Supplier<Block> logFinder) {
            this.logFinder = logFinder;
            return this;
        }

        /// @param id Full Id of MudType as ResourceLocation
        public Finder log(ResourceLocation id) {
            return this.log(() -> BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(
                    () -> new IllegalStateException("Failed to find log block: " + id)));
        }

        /// @param nameLog name of Log without modId or namespace
        public Finder log(String nameLog) {
            return this.log(Utils.idWithOptionalNamespace(nameLog, id.getNamespace()));
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
        @ApiStatus.Internal
        public Optional<WoodType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block plank = Preconditions.checkNotNull(planksFinder.get(), "Manual Finder - failed to find a plank block for {}", id);
                    Block log = Preconditions.checkNotNull(logFinder.get(), "Manual Finder - failed to find a log block for {}", id);
                    var woodType = new WoodType(id, plank, log);
                    childNames.forEach((key, value) -> {
                        try {
                            ItemLike obj = Preconditions.checkNotNull(value.get());
                            woodType.addChild(key, obj);
                        } catch (Exception e) {
                            Moonlight.LOGGER.warn("Failed to find child for WoodType: {} - {}. Ignored! ERROR: {}", id, key, e.getMessage());
                        }
                    });
                    return Optional.of(woodType);
                } catch (Exception e) {
                    Moonlight.LOGGER.warn("Failed to find custom WoodType:  {} - ", id, e);
                }
            }
            return Optional.empty();
        }

// ─────────────────────────────────────────── Marked For Removal ────────────────────────────────────────────

        /// USE {@link WoodTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public Finder(ResourceLocation id, Supplier<Block> planks, Supplier<Block> log) {
            super(id, WoodTypeRegistry.INSTANCE);
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
