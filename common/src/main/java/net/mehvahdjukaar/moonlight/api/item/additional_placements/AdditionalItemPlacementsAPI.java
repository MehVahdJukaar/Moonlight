package net.mehvahdjukaar.moonlight.api.item.additional_placements;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AdditionalItemPlacementsAPI {

    private static boolean isAfterRegistration = false;
    private static WeakReference<Map<Block, Item>> blockToItemsMap = new WeakReference<>(null);

    private static final List<Consumer<Event>> registrationListeners = new ArrayList<>();

    public static void addRegistration(Consumer<Event> eventConsumer){
        Moonlight.assertInitPhase();
        registrationListeners.add(eventConsumer);
    }

    @Nullable
    public static AdditionalItemPlacement getBehavior(Item item) {
        return ((IExtendedItem) item).moonlight$getAdditionalBehavior();
    }

    public static boolean hasBehavior(Item item) {
        return getBehavior(item) != null;
    }

    private static void attemptRegistering() {
        Map<Block, Item> map = blockToItemsMap.get();
        if (map != null) {

            Map<Item, AdditionalItemPlacement> placements = new HashMap<>();
            for (Item item : BuiltInRegistries.ITEM) {
                Event ev = new Event() {
                    @Override
                    public Item getTarget() {
                        return item;
                    }

                    @Override
                    public void register(AdditionalItemPlacement instance) {
                        placements.put(item, instance);
                    }
                };
                for (var l : registrationListeners) {
                    l.accept(ev);
                }
            }

            for (var entry : placements.entrySet()) {
                AdditionalItemPlacement placement = entry.getValue();
                Item item = entry.getKey();
                Block placedBlock = placement.getPlacedBlock();

                if (item != null && placedBlock != null) {
                    if (item != Items.AIR && placedBlock != Blocks.AIR) {
                        ((IExtendedItem) item).moonlight$addAdditionalBehavior(placement);
                        if (!map.containsKey(placedBlock)) map.put(placedBlock, item);
                    } else {
                        throw new AssertionError("Attempted to register an Additional behavior to invalid blocks or items: " + placedBlock + ", " + item);
                    }
                }
            }
            registrationListeners.clear();
        }
    }

    //needed as all items have to be registered before we can add them to maps. ALso better to do this asap
    @ApiStatus.Internal
    public static void afterItemReg() {
        if (blockToItemsMap.get() == null) {
            if (PlatHelper.isDev()) {
                throw new AssertionError("Block to items map was null");
            }
        }
        //after all registry objects are created we register our stuff
        attemptRegistering();
    }

    //called just once when registry callbacks fire for items. once since we just have 1 item that we use to call this.
    static void onRegistryCallback(Map<Block, Item> pBlockToItemMap) {
        blockToItemsMap = new WeakReference<>(pBlockToItemMap);
        if (isAfterRegistration) {
            //if we are here it means we are in sync phase where maps are re constructured
            attemptRegistering();
            blockToItemsMap.clear();
        }
        isAfterRegistration = true;
    }

    public interface Event {

        Item getTarget();

        void register(AdditionalItemPlacement instance);

        // Registers default instance to make simple block placement behavior
        default void registerSimple(Block toPlace) {
            register(new AdditionalItemPlacement(toPlace));
        }
    }

}
