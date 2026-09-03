package net.mehvahdjukaar.moonlight.api.set.leaves;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodChildKeys.*;

public class LeavesType extends BlockType {

    public static final Codec<LeavesType> CODEC = ResourceLocation.CODEC.flatXmap(r -> {
                LeavesType w = LeavesTypeRegistry.INSTANCE.get(r);
                if (w == null) return DataResult.error(() -> "No such leaves type: " + r);
                return DataResult.success(w);
            },
            t -> DataResult.success(t.id));

    public final Block leaves;

    protected LeavesType(ResourceLocation id, Block leaves) {
        super(id);
        this.leaves = leaves;
    }

    /// USE {@link LeavesType#getAssociatedWoodType()}
    @NotNull
    @Deprecated(forRemoval = true)
    public WoodType getWoodType() {
        var w = getAssociatedWoodType();
        if (w == null) return VanillaWoodTypes.OAK;
        return w;
    }

    @Nullable
    public WoodType getAssociatedWoodType() {
        return LeavesTypeRegistry.INSTANCE.getEquivalentWoodType(this);
    }

    @Override
    public ItemLike mainChild() {
        return leaves;
    }

    @Override
    public String getTranslationKey() {
        return "leaves_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    public void initializeChildrenBlocks() {
        this.addChild(LEAVES, leaves);

        WoodType equivalentWood = getAssociatedWoodType();
        Block log;
        if (equivalentWood != null) {
            log = equivalentWood.log;
        }
        else {
            log = findRelatedEntry(LOG, BuiltInRegistries.BLOCK);
        }
        if (log != null) this.addChild(LOG, log);

        this.addChild(SAPLING, this.findRelatedEntry("sapling", BuiltInRegistries.BLOCK));
    }

    @Override
    public void initializeChildrenItems() {
    }

    public static class Finder extends SetFinderBuilder<LeavesType> {

        private Supplier<Block> leavesFinder;

        public Finder(ResourceLocation id) {
            super(id, LeavesTypeRegistry.INSTANCE);
            this.leavesSuffix("_leaves"); // defaults
        }

        public Finder leaves(Supplier<Block> planksFinder) {
            this.leavesFinder = planksFinder;
            return this;
        }

        public Finder leaves(ResourceLocation id) {
            return this.leaves(() -> BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(
                    () -> new IllegalStateException("Failed to find leaves block: " + id)
            ));
        }

        public Finder leaves(String leavesName) {
            return this.leaves(Utils.idWithOptionalNamespace(leavesName, id.getNamespace()));
        }

        /**
         * @param prefix include the underscore, "_" if the blockId has one
         * @param suffix include the underscore, "_" if the blockId has one
         */
        public Finder leavesAffix(String prefix, String suffix) {
            return leaves(prefix + id.getPath() + suffix);
        }

        /**
         * @param suffix include the underscore, "_" if the blockId has one
         */
        @SuppressWarnings("UnusedReturnValue")
        public Finder leavesSuffix(String suffix) {
            return leaves(id.getPath() + suffix);
        }

        @SuppressWarnings("UnusedReturnValue")
        /// Associated WoodType
        public Finder equivalentWood(String id) {
            LeavesTypeRegistry.INSTANCE.addLeavesToWoodMapping(this.id, new ResourceLocation(id)); //this is ass too
            return this;
        }

        @Override
        public Optional<LeavesType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block leaves = Preconditions.checkNotNull(leavesFinder.get(), "Manual Finder - failed to find a leaf block for {}", id);
                    var leavesType = new LeavesType(id, leaves);
                    childNames.forEach((key, value) -> {
                        try {
                            ItemLike obj = Preconditions.checkNotNull(value.get());
                            if (key.equals(LOG)) LeavesTypeRegistry.INSTANCE.addToLeavesToWoodMap(id, (Block) obj);
                            leavesType.addChild(key, obj);
                        } catch (Exception e) {
                            Moonlight.LOGGER.warn("Failed to find child for LeavesType: {} - {}. Ignored! ERROR: {}", id, key, e.getMessage());
                        }
                    });
                    return Optional.of(leavesType);
                } catch (Exception e) {
                    Moonlight.LOGGER.warn("Failed to find custom LeavesType:  {} - ", id, e);
                }
            }
            return Optional.empty();
        }

// ─────────────────────────────────────────── Marked For Removal ────────────────────────────────────────────

        /// USE {@link LeavesTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public Finder(ResourceLocation id, Supplier<Block> leaves, @Nullable Supplier<WoodType> wood) {
            this(id, leaves);
        }

        /// USE {@link LeavesTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public Finder(ResourceLocation id, Supplier<Block> leaves) {
            super(id, LeavesTypeRegistry.INSTANCE);
            this.leavesFinder = leaves;
        }

        /// USE {@link LeavesTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public static Finder simple(String modId, String leavesTypeName, String leavesName) {
            return new Finder(new ResourceLocation(modId, leavesTypeName),
                    () -> BuiltInRegistries.BLOCK.get(new ResourceLocation(modId, leavesName)), null);
        }

        /// USE {@link LeavesTypeRegistry#addSimpleFinder(String, String)}
        @Deprecated(forRemoval = true)
        public static Finder simple(String modId, String leavesTypeName, String leavesName, String woodTypeID) {
            ResourceLocation leavesId = new ResourceLocation(modId, leavesName);
            LeavesTypeRegistry.INSTANCE.addLeavesToWoodMapping(leavesId, new ResourceLocation(woodTypeID));
            return new Finder(new ResourceLocation(modId, leavesTypeName),
                    () -> BuiltInRegistries.BLOCK.get(leavesId));
        }

        /**
         * USE {@link LeavesTypeRegistry#addSimpleFinder(String, String)}
         * <br>add {@link SetFinderBuilder#childBlockAffix(String, String, String)}
         * <br>OR
         * <br>add {@link SetFinderBuilder#childBlockSuffix(String, String)}
         */
        @Deprecated(forRemoval = true)
        public void addChild(String childType, String childName) {
            addChild(childType, new ResourceLocation(id.getNamespace(), childName));
        }

        /**
         * USE {@link LeavesTypeRegistry#addSimpleFinder(String, String)}
         * <br>add {@link SetFinderBuilder#childBlockAffix(String, String, String)}
         * <br>OR
         * <br>add {@link SetFinderBuilder#childBlockSuffix(String, String)}
         */
        @Deprecated(forRemoval = true)
        public void addChild(String childType, ResourceLocation childName) {
            childBlock(childType, childName);
        }

    }
}
