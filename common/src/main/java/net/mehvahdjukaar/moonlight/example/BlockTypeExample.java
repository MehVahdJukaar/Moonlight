package net.mehvahdjukaar.moonlight.example;

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

public class BlockTypeExample {

    // call this on mod init
    public static void init() {
        BlockSetAPI.registerBlockSetDefinition(new FlowerTypeRegistry());
        BlockSetAPI.addDynamicBlockRegistration(BlockTypeExample::registerFlowerTypeBlocks, FlowerType.class);
    }

    private static void registerFlowerTypeBlocks(Registrator<Block> event, Collection<FlowerType> flowerTypes) {
        // register here your dynamic blocks that depend on loaded flower types
        for(var t : flowerTypes){
            // final id will be something like "moonlight:compressed_poppy_block"
            ResourceLocation id = Moonlight.res(t.getVariantId("block", "compressed"));
            Block block = new Block(BlockBehaviour.Properties.of());
            event.register(id, block);
            // remember to add your block to the type. Note that you just need one for either block or item
            t.addChild("moonlight:compressed_block", block);
        }
    }

    // registry class which will hold our block types
    public static class FlowerTypeRegistry extends BlockTypeRegistry<FlowerType> {
        protected FlowerTypeRegistry() {
            super(FlowerType.class, "flower_type");
        }

        @Override
        public Optional<FlowerType> detectTypeFromBlock(Block block, ResourceLocation blockId) {
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

    // block type class. this one represents a block type which will be generated for each flower
    // meant so you could add things like "flowery planks and such"
    public static class FlowerType extends BlockType {
        public final FlowerBlock flower;

        FlowerType(FlowerBlock flower) {
            super(Utils.getID(flower));
            this.flower = flower;
        }

        @Override
        public String getTranslationKey() {
            return "flower_type." + this.getNamespace() + "." + this.getTypeName();
        }

        @Override
        protected void initializeChildrenBlocks() {
            var doubleFlower = findRelatedEntry("double", BuiltInRegistries.BLOCK);
            if (doubleFlower != null) this.addChild("double_flower", doubleFlower);
            this.addChild("flower", flower);
        }

        @Override
        protected void initializeChildrenItems() {
        }

        @Override
        public ItemLike mainChild() {
            return flower;
        }
    }

}
