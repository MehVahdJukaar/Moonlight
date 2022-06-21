package net.mehvahdjukaar.selene.block_set;

import net.mehvahdjukaar.selene.impl.blocks.VerticalSlabBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistryHelper {

    public enum VariantType {
        BLOCK(Block::new),
        SLAB(SlabBlock::new),
        VERTICAL_SLAB(VerticalSlabBlock::new),
        WALL(WallBlock::new),
        STAIRS(StairBlock::new);
        private final BiFunction<Supplier<BlockState>, BlockBehaviour.Properties, Block> constructor;

        VariantType(BiFunction<Supplier<BlockState>, BlockBehaviour.Properties, Block> constructor) {
            this.constructor = constructor;
        }

        VariantType(Function<BlockBehaviour.Properties, Block> constructor) {
            this.constructor = (b, p) -> constructor.apply(p);
        }

        private Block create(Block parent) {
            return this.constructor.apply(parent::defaultBlockState, BlockBehaviour.Properties.copy(parent));
        }
    }

    /**
     * Utility to register a full block set
     *
     * @param blockRegistry block registry
     * @param itemRegistry  item registry
     * @param baseName      base block name
     * @param parentBlock   base block
     * @return registry object map
     */
    public static EnumMap<VariantType, RegistryObject<Block>> registerFullBlockSet(
            DeferredRegister<Block> blockRegistry, DeferredRegister<Item> itemRegistry,
            String baseName, Block parentBlock, boolean isHidden) {

        EnumMap<VariantType, RegistryObject<Block>> map = new EnumMap<>(VariantType.class);
        for (VariantType type : VariantType.values()) {
            String name = baseName;
            if (!type.equals(VariantType.BLOCK)) name += "_" + type.name().toLowerCase(Locale.ROOT);
            RegistryObject<Block> block = blockRegistry.register(name, () -> type.create(parentBlock));
            CreativeModeTab tab = switch (type) {
                case VERTICAL_SLAB -> !isHidden && ModList.get().isLoaded("quark") ? CreativeModeTab.TAB_BUILDING_BLOCKS : null;
                case WALL -> !isHidden ? CreativeModeTab.TAB_DECORATIONS : null;
                default -> !isHidden ? CreativeModeTab.TAB_BUILDING_BLOCKS : null;
            };
            itemRegistry.register(block.getId().getPath(),
                    () -> new BlockItem(block.get(), (new Item.Properties()).tab(tab)));
            map.put(type, block);
        }
        return map;
    }
}
