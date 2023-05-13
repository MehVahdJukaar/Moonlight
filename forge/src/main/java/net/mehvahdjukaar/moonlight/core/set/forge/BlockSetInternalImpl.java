package net.mehvahdjukaar.moonlight.core.set.forge;

import net.mehvahdjukaar.moonlight.api.block.ModLiquidBlock;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//spaghetti code. Do not touch, it just works
public class BlockSetInternalImpl {

    //maps containing mod ids and block and items runnable. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<String, List<Runnable>> LATE_REGISTRATION_QUEUE = new ConcurrentHashMap<>();

    private static boolean hasFilledBlockSets = false;

    static{
        //if loaded registers post item init
        Consumer<RegisterEvent> eventConsumer = e->{
            if(e.getRegistryKey().equals(ForgeRegistries.ENCHANTMENTS.getRegistryKey())){
                BlockSetInternal.getRegistries().forEach(BlockTypeRegistry::onItemInit);
            }
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    //aaaa
    public static  <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType, Registry<E> registry) {
        if(registry == BuiltInRegistries.BLOCK) {
            addDynamicBlockRegistration((BlockSetAPI.BlockTypeRegistryCallback<Block, T>) registrationFunction, blockType);
        }else if(registry == BuiltInRegistries.FLUID || registry == BuiltInRegistries.SOUND_EVENT){
            throw new IllegalArgumentException("Fluid and Sound Events registry not supported here");
        } else{
            //other entries
            RegHelper.registerInBatch(registry, e-> registrationFunction.accept(e, BlockSetAPI.getBlockSet(blockType).getValues()));
        }
    }


    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {

        Consumer<RegisterEvent> eventConsumer;

        List<Runnable> registrationQueues = getOrAddQueue();

        //if block makes a function that just adds the bus and runnable to the queue whenever reg block is fired
        eventConsumer = e -> {
            if (e.getRegistryKey().equals(ForgeRegistries.BLOCKS.getRegistryKey())) {
                //actual runnable which will registers the blocks
                Runnable lateRegistration = () -> {

                    IForgeRegistry<Block> registry = e.getForgeRegistry();
                    if (registry instanceof ForgeRegistry<?> fr) {
                        boolean frozen = fr.isLocked();
                        fr.unfreeze();
                        registrationFunction.accept(registry::register, BlockSetAPI.getBlockSet(blockType).getValues());
                        if (frozen) fr.freeze();
                    }
                };
                //when this reg block event fires we only add a runnable to the queue
                registrationQueues.add(lateRegistration);
            }
        };
        //registering block event to the bus
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(EventPriority.HIGHEST, eventConsumer);
    }

    @NotNull
    private static List<Runnable> getOrAddQueue() {
        //this is horrible. worst shit ever
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        return LATE_REGISTRATION_QUEUE.computeIfAbsent(modId, s -> {
            //if absent we register its registration callback
            bus.addListener(EventPriority.HIGHEST, BlockSetInternalImpl::registerLateBlockAndItems);
            return new ArrayList<>();
        });
    }


    //shittiest code ever lol
    protected static void registerLateBlockAndItems(RegisterEvent event) {
        //fires right after blocks
        if (!event.getRegistryKey().equals(ForgeRegistries.ATTRIBUTES.getRegistryKey())) return;
        //when the first registration function is called we find all block types
        if (!hasFilledBlockSets) {
            BlockSetInternal.initializeBlockSets();
            hasFilledBlockSets = true;
        }
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        var registrationQueues = LATE_REGISTRATION_QUEUE.get(modId);

        if (registrationQueues != null) {
            //register blocks
            registrationQueues.forEach(Runnable::run);
        }
        //clears stuff that's been executed. not really needed but just to be safe its here
        LATE_REGISTRATION_QUEUE.remove(modId);
    }

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }



}
