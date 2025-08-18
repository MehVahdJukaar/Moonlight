package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.base.Predicates;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Predicate;

//hand not empty, existing item, any item
public interface InvPlacer {
    boolean place(ItemStack stack, Inventory inventory, Player player);

    default InvPlacer or(InvPlacer other) {
        return (stack, inventory, player) -> this.place(stack, inventory, player) || other.place(stack, inventory, player);
    }


    static ExclusivePlacer exclusiveSequence() {
        return new ExclusivePlacer();
    }

    static SimplePlacer of(SlotProvider slots) {
        return of(slots, Predicates.alwaysTrue());
    }

    static SimplePlacer of(SlotProvider slots, Predicate<ItemStack> predicate) {
        return new SimplePlacer(slots, predicate);
    }


    //default impls
    static InvPlacer handOrExistingOrAnyAvoidEmptyHand(InteractionHand hand) {
        return exclusiveSequence()
                .stage(InvPlacer.handNotEmpty(hand))
                .stage(InvPlacer.EXISTING)
                .stage(InvPlacer.ANY)
                .or(InvPlacer.ANY);
    }

    static InvPlacer handOrExistingOrAny(InteractionHand hand) {
        return hand(hand).or(EXISTING).or(ANY);
    }

    static InvPlacer existingOrAny() {
        return EXISTING.or(ANY);
    }

    static SimplePlacer handNotEmpty(InteractionHand hand) {
        return hand(hand, stack -> !stack.isEmpty());
    }

    static SimplePlacer hand(InteractionHand hand) {
        return hand(hand, Predicates.alwaysTrue());
    }

    static SimplePlacer hand(InteractionHand hand, Predicate<ItemStack> predicate) {
        return of(SlotProvider.hand(hand), predicate);
    }

    static SimplePlacer slot(int slot) {
        return of(SlotProvider.single(slot));
    }

    SimplePlacer EXISTING = of(SlotProvider.ALL, stack -> !stack.isEmpty());

    SimplePlacer EMPTY = of(SlotProvider.ALL, ItemStack::isEmpty);

    SimplePlacer ANY = of(SlotProvider.ALL);

    InvPlacer DROP = (stack, inventory, player) -> {
        player.drop(stack, false);
        return true;
    };


    class ExclusivePlacer implements InvPlacer {

        private final List<Stage> stages = new ArrayList<>();
        private final Set<SlotProvider.Slot> visitedSlots = new HashSet<>();

        public ExclusivePlacer stage(SlotProvider provider, Predicate<ItemStack> predicate) {
            stage(of(provider, predicate)); //abusing existing impl...
            return this;
        }

        public ExclusivePlacer stage(SlotProvider provider) {
            return stage(provider, Predicates.alwaysTrue());
        }

        public ExclusivePlacer stage(ExclusivePlacer.Stage stage) {
            this.stages.add(stage);
            return this;
        }

        @Override
        public boolean place(ItemStack stack, Inventory inventory, Player player) {
            for (var stage : stages) {

                Iterator<SlotProvider.Slot> slots = stage.slotProvider().getSlots(inventory);
                while (slots.hasNext()) {
                    SlotProvider.Slot slot = slots.next();
                    if (visitedSlots.contains(slot)) continue; //skip already visited slots
                    Predicate<ItemStack> predicate = stage.predicate();
                    if (predicate.test(slot.getStack())) {
                        visitedSlots.add(slot);
                        if (slot.add(stack, inventory, player)) {
                            return true;
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        //decouples slot providing from slot filling so we can filter them here
        interface Stage {
            SlotProvider slotProvider();

            Predicate<ItemStack> predicate();
        }
    }

    record SimplePlacer(SlotProvider slotProvider, Predicate<ItemStack> predicate) implements InvPlacer, ExclusivePlacer.Stage {

        @Override
        public boolean place(ItemStack stack, Inventory inventory, Player player) {
            var iterator = slotProvider.getSlots(inventory);
            while (iterator.hasNext()) {
                SlotProvider.Slot slot = iterator.next();
                if (predicate.test(slot.getStack())) {
                    if (slot.add(stack, inventory, player)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}
