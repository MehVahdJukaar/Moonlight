package net.mehvahdjukaar.moonlight.api.set.leaves;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class LeavesType extends BlockType {

    public final Block leaves;

    private final Supplier<WoodType> woodType;

    protected LeavesType(ResourceLocation id, Block leaves) {
        this(id, leaves, Suppliers.memoize(() -> Objects.requireNonNullElse(WoodTypeRegistry.getValue(id), WoodTypeRegistry.OAK_TYPE)));
    }

    protected LeavesType(ResourceLocation id, Block leaves, Supplier<WoodType> woodType) {
        super(id);
        this.leaves = leaves;
        this.woodType = woodType;
    }

    public WoodType getWoodType() {
        return woodType.get();
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
        this.addChild("leaves", (Object) leaves);
        this.addChild("sapling", (Object) this.findRelatedEntry("sapling", Registry.BLOCK));
        this.woodType.get();
    }

    @Override
    public void initializeChildrenItems() {
    }

    public static class Finder implements SetFinder<LeavesType> {

        private final Supplier<Block> leavesFinder;
        private final Supplier<WoodType> woodFinder;
        private final ResourceLocation id;

        public Finder(ResourceLocation id, Supplier<Block> leaves, @Nullable Supplier<WoodType> wood) {
            this.id = id;
            this.leavesFinder = leaves;
            this.woodFinder = wood;
        }

        public static Finder simple(String modId, String leavesTypeName, String leavesName) {
            return new Finder(new ResourceLocation(modId, leavesTypeName),
                    () -> Registry.BLOCK.get(new ResourceLocation(modId, leavesName)), null);
        }

        public static Finder simple(String modId, String leavesTypeName, String leavesName, String woodTypeName) {
            return new Finder(new ResourceLocation(modId, leavesTypeName),
                    () -> Registry.BLOCK.get(new ResourceLocation(modId, leavesName)),
                    () -> WoodTypeRegistry.INSTANCE.get(new ResourceLocation(woodTypeName)));
        }

        @Override
        public Optional<LeavesType> get() {
            if (PlatformHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block leaves = leavesFinder.get();
                    var d = Registry.BLOCK.get(Registry.BLOCK.getDefaultKey());
                    if (leaves != d && leaves != null) {
                        if (woodFinder == null) {
                            return Optional.of(new LeavesType(id, leaves));
                        } else {
                            return Optional.of(new LeavesType(id, leaves, woodFinder));
                        }
                    }
                } catch (Exception ignored) {
                }
                Moonlight.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }

    }
}
