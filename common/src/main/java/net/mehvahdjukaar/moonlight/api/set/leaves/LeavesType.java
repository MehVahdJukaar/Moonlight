package net.mehvahdjukaar.moonlight.api.set.leaves;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodChildKeys.*;

public class LeavesType extends BlockType {

    public static Codec<LeavesType> CODEC;
    public static StreamCodec<ByteBuf, LeavesType> STREAM_CODEC;

    static {
        LeavesTypeRegistry.touch();
    }

    public final Block leaves;

    protected LeavesType(Identifier id, Block leaves) {
        super(id);
        this.leaves = leaves;
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

        public Finder(Identifier id) {
            super(id, LeavesTypeRegistry.INSTANCE);
            this.leavesSuffix("_leaves"); // defaults
        }

        public Finder leaves(Supplier<Block> planksFinder) {
            this.leavesFinder = planksFinder;
            return this;
        }

        public Finder leaves(Identifier id) {
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
            LeavesTypeRegistry.INSTANCE.addLeavesToWoodMapping(this.id, Identifier.parse(id)); //this is ass too
            return this;
        }

        @Override
        public Optional<LeavesType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block leaves = Preconditions.checkNotNull(leavesFinder.get(), "Manual Finder - failed to find a leaf block for {}", id);
                    var w = new LeavesType(id, leaves);
                    childNames.forEach((key, value) -> {
                        try {
                            ItemLike obj = Preconditions.checkNotNull(value.get());
                            w.addChild(key, obj);
                        } catch (Exception e) {
                            Moonlight.LOGGER.warn("Failed to find child for WoodType: {} - {}. Ignored! ERROR: {}", id, key, e.getMessage());
                        }
                    });
                    return Optional.of(w);
                } catch (Exception e) {
                    Moonlight.LOGGER.warn("Failed to find custom WoodType:  {} - ", id, e);
                }
            }
            return Optional.empty();
        }

    }
}
