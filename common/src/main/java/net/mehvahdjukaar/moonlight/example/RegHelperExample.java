package net.mehvahdjukaar.moonlight.example;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Map;
import java.util.function.Supplier;

// RegHelper is the main utility class for anything registry related
public class RegHelperExample {

    // call this on mod init. use to add callbacks for special events
    public static void init(){
        //these callbacks pretty much just wrap forge and fabric events
        RegHelper.addItemsToTabsRegistration(RegHelperExample::registerItemsToTabs);
        RegHelper.addLootTableInjects(RegHelperExample::registerLootInjects);
    }

    public static Supplier<HoneycombItem> CUSTOM_HONEYCOMB = RegHelper.registerItem(
            Moonlight.res("custom_comb"), () -> new HoneycombItem(new Item.Properties())
    );

    public static Supplier<Block> STURDY_STONE_BRICKS = RegHelper.registerBlockWithItem(
            Moonlight.res("sturdy_stone_bricks"), ()-> new Block(BlockBehaviour.Properties.of())
    );

    // generic entry registration. Just like Registry.register calls
    public static Supplier<GameEvent> CUSTOM_PIECE = RegHelper.register(
            Moonlight.res("custom_event"), ()->new GameEvent("custom_event", 2),
            Registries.GAME_EVENT
    );

    // register items to tabs
    private static void registerItemsToTabs(RegHelper.ItemToTabEvent event) {
        // adds our stone after all blocks tagged as stone bricks
        event.addBefore(CreativeModeTabs.BUILDING_BLOCKS,
                stack -> stack.is(ItemTags.STONE_BRICKS),
                STURDY_STONE_BRICKS.get());
    }

    // adds diamond loot to stone block
    private static void registerLootInjects(RegHelper.LootInjectEvent event) {
        if(event.getTable().equals(new ResourceLocation("stone"))){
            event.addTableReference(new ResourceLocation("diamond"));
        }
    }

}
