package net.mehvahdjukaar.selene.block_set.leaves;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.mehvahdjukaar.selene.block_set.IBlockType;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class LeavesType extends IBlockType {

    public static LeavesType OAK_LEAVES_TYPE = new LeavesType(new ResourceLocation("oak"), Blocks.OAK_LEAVES, WoodType.OAK_WOOD_TYPE);

    public final Block leaves;
    @Nullable
    public final WoodType woodType;

    protected LeavesType(ResourceLocation id, Block leaves, @Nullable WoodType woodType) {
        super(id);
        this.leaves = leaves;
        this.woodType = woodType;
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
    protected void initializeChildren() {
        this.addChild("leaves", leaves);
    }

    public static class Finder extends SetFinder<LeavesType> {

        private final Supplier<Block> leavesFinder;
        private final ResourceLocation id;

        public Finder(ResourceLocation id, Supplier<Block> leaves) {
            this.id = id;
            this.leavesFinder = leaves;
        }

        public static Finder simple(String modId, String leavesTypeName, String leavesName) {
            return new Finder(new ResourceLocation(modId, leavesTypeName),
                    () -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modId, leavesName)));
        }

        @Override
        public Optional<LeavesType> get() {
            if (ModList.get().isLoaded(id.getNamespace())) {
                try {
                    Block leaves = leavesFinder.get();
                    var d = ForgeRegistries.BLOCKS.getValue(ForgeRegistries.BLOCKS.getDefaultKey());
                    if (leaves != d && leaves != null) {
                        WoodType w = WoodTypeRegistry.WOOD_TYPES.get(id);
                        return Optional.of(new LeavesType(id, leaves, w));
                    }
                } catch (Exception ignored) {
                }
                Selene.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }

    }
}
