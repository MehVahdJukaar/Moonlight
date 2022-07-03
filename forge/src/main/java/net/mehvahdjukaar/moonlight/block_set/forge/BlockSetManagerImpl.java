package net.mehvahdjukaar.moonlight.block_set.forge;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.block_set.BlockSetManager;
import net.mehvahdjukaar.moonlight.block_set.BlockType;
import net.mehvahdjukaar.moonlight.misc.Registrator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BlockSetManagerImpl {

    //maps containing mod ids and block and items runnable. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<String, Pair<
            List<Runnable>, //block registration function
            List<Consumer<Registrator<Item>>> //item registration function
            >>
            LATE_REGISTRATION_QUEUE = new ConcurrentHashMap<>();

    private static boolean hasFilledBlockSets = false;

    static class ForgeRegistrator<T extends IForgeRegistryEntry<T>> implements Registrator<T> {
        private final IForgeRegistry<T> reg;

        ForgeRegistrator(IForgeRegistry<T> registry) {
            this.reg = registry;
        }

        @Override
        public void register(ResourceLocation name, T instance) {
            instance.setRegistryName(name);
            reg.register(instance);
        }
    }

    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockSetManager.BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {

        Consumer<RegistryEvent.Register<Block>> eventConsumer;

        Pair<List<Runnable>, List<Consumer<Registrator<Item>>>> registrationQueues = getOrAddQueue();

        //if block makes a function that just adds the bus and runnable to the queue whenever reg block is fired
        eventConsumer = e -> {

            //actual runnable which will registers the blocks
            Runnable lateRegistration = () -> {

                IForgeRegistry<Block> registry = e.getRegistry();
                if (registry instanceof ForgeRegistry fr) {
                    boolean frozen = fr.isLocked();
                    fr.unfreeze();
                    registrationFunction.accept(new ForgeRegistrator<>(registry), BlockSetManager.getBlockSet(blockType).getValues());
                    if (frozen) fr.freeze();
                }

            };
            //when this reg block event fires we only add a runnable to the queue
            registrationQueues.getFirst().add(lateRegistration);

        };
        //registering block event to the bus
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(EventPriority.HIGHEST, eventConsumer);
    }

    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockSetManager.BlockTypeRegistryCallback<Item, T> registrationFunction, Class<T> blockType) {

        Pair<List<Runnable>, List<Consumer<Registrator<Item>>>> registrationQueues = getOrAddQueue();
        //items just get added to the queue. they will already be called with the correct event
        Consumer<Registrator<Item>> itemEvent = e ->
                registrationFunction.accept(e, BlockSetManager.getBlockSet(blockType).getValues());

        registrationQueues.getSecond().add(itemEvent);
    }

    @NotNull
    private static Pair<List<Runnable>, List<Consumer<Registrator<Item>>>> getOrAddQueue() {
        //this is horrible. worst shit ever
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        return LATE_REGISTRATION_QUEUE.computeIfAbsent(modId, s -> {
            //if absent we register its registration callback
            bus.addListener(EventPriority.HIGHEST, BlockSetManagerImpl::registerLateBlockAndItems);
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        });
    }


    //shittiest code ever lol
    protected static void registerLateBlockAndItems(RegistryEvent.Register<Item> event) {
        //fires for items

        //when the first registration function is called we find all block types
        if (!hasFilledBlockSets) {
            BlockSetManager.initializeBlockSets();
            hasFilledBlockSets = true;
        }
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        var registrationQueues =
                LATE_REGISTRATION_QUEUE.get(modId);
        if (registrationQueues != null) {
            IForgeRegistry<Item> fr = event.getRegistry();
            //register blocks
            var blockQueue = registrationQueues.getFirst();
            blockQueue.forEach(Runnable::run);
            //registers items
            var itemQueue = registrationQueues.getSecond();
            itemQueue.forEach(q -> q.accept(new ForgeRegistrator<>(fr)));
        }
        //clears stuff that's been execured. not really needed but just to be safe its here
        LATE_REGISTRATION_QUEUE.remove(modId);
    }

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }


}
