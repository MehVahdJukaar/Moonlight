package net.mehvahdjukaar.moonlight.core.set;

import com.google.common.base.Stopwatch;
import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
public class BlockSetInternal {

    //Frick mod loading is multithreaded, so we need to beware of concurrent access
    private static final Queue<Runnable> FINDER_ADDER = new ArrayDeque<>();
    private static final Queue<Runnable> REMOVER_ADDER = new ArrayDeque<>();

    private static final Map<Class<? extends BlockType>, BlockTypeRegistry<?>> REGISTRIES_BY_CLASS = new LinkedHashMap<>();
    private static final MapRegistry<BlockTypeRegistry<?>> REGISTRIES_BY_NAME = new MapRegistry<>("block_set_registry");
    private static final List<BlockTypeRegistry<?>> ORDERED_REGISTRIES = new ArrayList<>();

    public static void initializeBlockSets() {
        Stopwatch sw = Stopwatch.createStarted();
        if (hasFilledBlockSets()) throw new UnsupportedOperationException("block sets have already bee initialized");
        FINDER_ADDER.forEach(Runnable::run);
        FINDER_ADDER.clear();
        //remove not wanted ones
        REMOVER_ADDER.forEach(Runnable::run);
        REMOVER_ADDER.clear();

        var regs = getRegistries();
        regs.forEach(BlockTypeRegistry::buildAll);
        regs.forEach(BlockTypeRegistry::finalizeAndFreeze);


        Moonlight.LOGGER.info("Initialized block sets in {}ms", sw.elapsed().toMillis());
    }

    @ExpectPlatform
    protected static boolean hasFilledBlockSets() {
        throw new AssertionError();
    }

    public synchronized static <T extends BlockType> void registerBlockSetDefinition(BlockTypeRegistry<T> typeRegistry) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block set definition %s after registry events", typeRegistry));
        }
        REGISTRIES_BY_CLASS.put(typeRegistry.getType(), typeRegistry);
        REGISTRIES_BY_NAME.register(typeRegistry.typeName(), typeRegistry);
    }

    //TODO: remove
    @Deprecated
    public synchronized static <T extends BlockType> void addBlockTypeFinder(Class<T> type, BlockType.SetFinder<T> blockFinder) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block %s finder %s after registry events", type, blockFinder));
        }
        FINDER_ADDER.add(() -> {
            BlockTypeRegistry<T> container = getBlockSetReg(type);
            container.addFinder(blockFinder);
        });
    }

    //TODO:remove. use one in block set itself
    @Deprecated
    public synchronized static <T extends BlockType> void addBlockTypeRemover(Class<T> type, ResourceLocation id) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to remove block type %s for type %s after registry events", id, type));
        }
        REMOVER_ADDER.add(() -> {
            BlockTypeRegistry<T> container = getBlockSetReg(type);
            container.addRemover(id);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockType> BlockTypeRegistry<T> getBlockSetReg(Class<T> type) {
        return (BlockTypeRegistry<T>) REGISTRIES_BY_CLASS.get(type);
    }

    @EventCalled
    public static void addTranslations(AfterLanguageLoadEvent event) {
        BlockSetAPI.getRegistries().forEach(r -> r.addTypeTranslations(event));
    }

    @ExpectPlatform
    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType,
            Registry<E> registry) {
        throw new AssertionError();
    }

    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {
        addDynamicRegistration(registrationFunction, blockType, BuiltInRegistries.BLOCK);
    }

    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Item, T> registrationFunction, Class<T> blockType) {
        addDynamicRegistration(registrationFunction, blockType, BuiltInRegistries.ITEM);
    }


    //gets registries in order
    public static Collection<BlockTypeRegistry<?>> getRegistries() {
        if (ORDERED_REGISTRIES.isEmpty()) {
            synchronized (ORDERED_REGISTRIES) {
                Comparator<BlockTypeRegistry<?>> comparator = Comparator.comparingInt(BlockTypeRegistry::priority);
                // Sort the registries based on their priority. Higher priority comes first.
                ORDERED_REGISTRIES.addAll(REGISTRIES_BY_NAME.getValues()
                        .stream()
                        .sorted(comparator.reversed())
                        .toList());
            }
        }
        return ORDERED_REGISTRIES;
    }

    @Nullable
    public static <T extends BlockType> BlockTypeRegistry<T> getRegistry(Class<T> typeClass) {
        return (BlockTypeRegistry<T>) REGISTRIES_BY_CLASS.get(typeClass);
    }

    public static BlockTypeRegistry<?> getRegistry(String name) {
        return REGISTRIES_BY_NAME.getValue(name);
    }

    @Nullable
    public static <T extends BlockType> T getBlockTypeOf(ItemLike itemLike, Class<T> typeClass) {
        BlockTypeRegistry<T> r = getRegistry(typeClass);
        if (r != null) {
            return r.getBlockTypeOf(itemLike);
        }
        return null;
    }

    public static Codec<BlockTypeRegistry<?>> getRegistriesCodec() {
        return REGISTRIES_BY_NAME;
    }
}
