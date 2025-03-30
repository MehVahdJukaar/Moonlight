package net.mehvahdjukaar.moonlight.api.item.additional_placements;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public class AdditionalItemPlacementsAPI {

    /**
     * Adds a behavior to an existing block. can be called at any time but ideally before registration. Less ideally during mod setup
     */
    @Deprecated(forRemoval = true)
    public static void addRegistration(Consumer<Event> eventConsumer) {
        Moonlight.assertInitPhase();
        PlatHelper.addCommonSetup(() -> eventConsumer.accept(AdditionalItemPlacementsAPI::registerPlacement));
    }

    //since these need to be called on client too we MUST allow it to be called at any time so we don't need to reply on our own packet and can run on another mod will
    /***
     * Call in mod setup. Adds a placement to an item
     */
    public static void registerPlacement(Item target, AdditionalItemPlacement placement) {
        IExtendedItem ei = (IExtendedItem) target;
        AdditionalItemPlacement old = ei.moonlight$getAdditionalBehavior();
        if (old != null) {
            unregisterPlacement(target);
            Moonlight.LOGGER.warn("Overriding existing additional placement behavior for item {}, placement {}", target, old);
        }
        ei.moonlight$setAdditionalBehavior(placement);
        Block placedBlock = placement.getPlacedBlock();
        if (target == Items.AIR || placedBlock == Blocks.AIR) {
            throw new AssertionError("Invalid item or block for additional placement: block = " + placedBlock + ", item = " + target);
        }

        Item.BY_BLOCK.put(placedBlock, target);
        placedBlock.item = null;
    }

    //shorthand
    public static void registerSimplePlacement(Item target, Block toPlace) {
        registerPlacement(target, new AdditionalItemPlacement(toPlace));
    }

    public static void unregisterPlacement(Item target) {
        IExtendedItem ei = (IExtendedItem) target;
        AdditionalItemPlacement old = ei.moonlight$getAdditionalBehavior();
        if (old != null) {
            ei.moonlight$setAdditionalBehavior(null);
            Block placedBlock = old.getPlacedBlock();
            if (placedBlock.item == target) {
                Item.BY_BLOCK.remove(placedBlock);
                placedBlock.item = null;
            }
        }
    }

    @Nullable
    public static AdditionalItemPlacement getBehavior(Item item) {
        return ((IExtendedItem) item).moonlight$getAdditionalBehavior();
    }

    public static boolean hasBehavior(Item item) {
        return getBehavior(item) != null;
    }

    @Deprecated(forRemoval = true)
    public interface Event {

        void register(Item target, AdditionalItemPlacement instance);

        // Registers default instance to make simple block placement behavior
        default void registerSimple(Item target, Block toPlace) {
            register(target, new AdditionalItemPlacement(toPlace));
        }
    }

}
