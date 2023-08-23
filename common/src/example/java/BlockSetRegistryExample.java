import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collection;
import java.util.Optional;

public class BlockSetRegistryExample {

    // Call this on mod init
    public static void init() {
        // Defining a block set
        BlockSetAPI.registerBlockSetDefinition(new FlowerTypeRegistry());
        // Using a block set to register dynamic blocks
        BlockSetAPI.addDynamicBlockRegistration(BlockSetRegistryExample::registerFlowerTypeBlocks, FlowerType.class);
    }


    // Registry class which will hold our block types
    public static class FlowerTypeRegistry extends BlockTypeRegistry<FlowerType> {
        protected FlowerTypeRegistry() {
            super(FlowerType.class, "flower_type");
        }

        @Override
        public Optional<FlowerType> detectTypeFromBlock(Block block, ResourceLocation blockId) {
            // Main method which will detect the existence of a BlockType entry. Here you can check the existence
            // of other blocks (i.e., double flowers or do some id checks on the block itself)
            if (block instanceof FlowerBlock f) {
                return Optional.of(new FlowerType(f));
            }
            return Optional.empty();
        }

        @Override
        public FlowerType getDefaultType() {
            return POPPY;
        }

        public static final FlowerType POPPY = new FlowerType((FlowerBlock) Blocks.POPPY);
    }

    public static class FlowerType extends BlockType {
        public final FlowerBlock shortFlower;

        FlowerType(FlowerBlock flower) {
            super(Utils.getID(flower));
            this.shortFlower = flower;
        }

        @Override
        protected void initializeChildrenBlocks() {
            var doubleFlower = findRelatedEntry("double", BuiltInRegistries.BLOCK);
            if (doubleFlower != null) this.addChild("double_flower", doubleFlower);
            this.addChild("flower", shortFlower);
        }

        @Override
        public String getTranslationKey() {
            return "flower_type." + this.getNamespace() + "." + this.getTypeName();
        }

        @Override
        protected void initializeChildrenItems() {
        }

        @Override
        public ItemLike mainChild() {
            return shortFlower;
        }
    }

    public static void setup() {
        // Showing off some other usage
        FlowerType dandelionType = BlockSetAPI.getBlockTypeOf(Blocks.DANDELION, FlowerType.class);

        // Gets a child of a block type
        if (dandelionType != null) {
            Block compressedDandelion = dandelionType.getBlockOfThis("moonlight:compressed_flower");

            // Change a block of a type into another
            Block compressedPoppy = BlockSetAPI.changeBlockType(compressedDandelion, dandelionType, FlowerTypeRegistry.POPPY);
        }
    }

    private static void registerFlowerTypeBlocks(Registrator<Block> event, Collection<FlowerType> flowerTypes) {
        // Register here your dynamic blocks that depend on loaded flower types
        for (FlowerType type : flowerTypes) {
            // The final id will be something like "moonlight:compressed_poppy_block"
            ResourceLocation id = Moonlight.res(type.getVariantId("block", "compressed"));
            Block block = new Block(BlockBehaviour.Properties.of());
            event.register(id, block);
            // Remember to add your block to the type with a unique key;
            // Note that you just need one for either block or item
            type.addChild("moonlight:compressed_flower", block);
        }
    }


}
