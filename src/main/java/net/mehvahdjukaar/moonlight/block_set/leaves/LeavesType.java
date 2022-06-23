package net.mehvahdjukaar.moonlight.block_set.leaves;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.block_set.BlockType;
import net.mehvahdjukaar.moonlight.block_set.wood.WoodType;
import net.mehvahdjukaar.moonlight.block_set.wood.WoodTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class LeavesType extends BlockType {

    public final Block leaves;
    public WoodType woodType;

    protected LeavesType(ResourceLocation id, Block leaves) {
        super(id);
        this.leaves = leaves;
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
    protected void initializeVanillaChildren() {
        this.addChild("leaves", leaves);
        this.woodType = WoodTypeRegistry.getValue(id);

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
                        return Optional.of(new LeavesType(id, leaves));
                    }
                } catch (Exception ignored) {
                }
                Moonlight.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }

    }
}
