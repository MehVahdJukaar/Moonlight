package net.mehvahdjukaar.moonlight.api.fluids;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.codec.LenientListCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;


public class FluidContainerList implements Iterable<FluidContainerList.Category> {

    public static final Codec<FluidContainerList> CODEC = LenientListCodec.of(Category.CODEC)
            .xmap(FluidContainerList::new, FluidContainerList::getCategories);
    private final Map<Item, Category> emptyToFilledMap = new IdentityHashMap<>();

    public FluidContainerList(List<Category> categoryList) {
        categoryList.forEach(this::addCategory);
    }

    public FluidContainerList() {

    }

    private void addCategory(Category newCategory) {
        if (!newCategory.isEmpty()) {
            if (emptyToFilledMap.containsKey(newCategory.emptyContainer)) {
                Category c = emptyToFilledMap.get(newCategory.emptyContainer);
                if (c.containerCapacity == newCategory.containerCapacity) {
                    c.filled.addAll(newCategory.filled);
                }
            } else {
                emptyToFilledMap.put(newCategory.emptyContainer, newCategory);
            }
        }
    }

    public Optional<Item> getEmpty(Item filledContainer) {
        for (var e : this.emptyToFilledMap.entrySet()) {
            if (e.getValue().getFilledItems().contains(filledContainer)) return Optional.of(e.getKey());
        }
        return Optional.empty();
    }

    public Optional<Item> getFilled(Item emptyContainer) {
        Category c = this.emptyToFilledMap.get(emptyContainer);
        if (c != null) return c.getFirstFilled();
        return Optional.empty();
    }

    public Optional<Category> getCategoryFromEmpty(Item emptyContainer) {
        return Optional.ofNullable(this.emptyToFilledMap.get(emptyContainer));
    }

    public Optional<Category> getCategoryFromFilled(Item filledContainer) {
        return this.getEmpty(filledContainer).map(this.emptyToFilledMap::get);
    }

    public Collection<Item> getPossibleFilled() {
        List<Item> list = new ArrayList<>();
        this.emptyToFilledMap.values().forEach(c -> list.addAll(c.filled));
        return list;
    }

    public Collection<Item> getPossibleEmpty() {
        return this.emptyToFilledMap.keySet();
    }

    public List<Category> getCategories() {
        return List.copyOf(this.emptyToFilledMap.values());
    }


    @Override
    public @NotNull Iterator<Category> iterator() {
        return this.emptyToFilledMap.values().iterator();
    }

    protected void merge(FluidContainerList other) {
        other.emptyToFilledMap.values().forEach(this::addCategory);
    }

    protected void add(Item empty, Item filled, int amount) {
        var c = this.emptyToFilledMap.computeIfAbsent(empty, i -> new Category(i, amount));
        c.addItem(filled);
    }

    protected void add(Item empty, Item filled, int amount, SoundEvent fillSound, SoundEvent emptySound) {
        var c = this.emptyToFilledMap.computeIfAbsent(empty, i -> new Category(i, amount));
        c.addItem(filled);
        if (c.fillSound == null) c.fillSound = fillSound;
        if (c.emptySound == null) c.emptySound = emptySound;
    }


    public static class Category {

        private static final Supplier<Category> EMPTY = Suppliers.memoize(() ->
                new Category(BuiltInRegistries.ITEM.getValue(BuiltInRegistries.ITEM.getDefaultKey()), 1));

        public static final Codec<Category> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("empty").forGetter(c -> c.emptyContainer),
                SoftFluid.Capacity.INT_CODEC.fieldOf("capacity").forGetter(Category::getCapacity),
                BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("filled").forGetter(c -> c.filled),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("fill_sound")
                        .forGetter(c -> Optional.ofNullable(c.getFillSound())),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("empty_sound")
                        .forGetter(c -> Optional.ofNullable(c.getEmptySound()))
        ).apply(instance, Category::decode));

        private final Item emptyContainer;
        private final int containerCapacity;
        private SoundEvent fillSound;
        private SoundEvent emptySound;
        private final List<Item> filled = new ArrayList<>();


        private Category(Item emptyContainer, int capacity, @Nullable SoundEvent fillSound, @Nullable SoundEvent emptySound) {
            this.emptyContainer = emptyContainer;
            this.containerCapacity = capacity;
            this.fillSound = fillSound;
            this.emptySound = emptySound;
        }

        private Category(Item emptyContainer, int capacity) {
            this(emptyContainer, capacity, null, null);
        }

        private static Category decode(Item empty, int capacity, List<Item> filled) {
            return decode(empty, capacity, filled, Optional.empty(), Optional.empty());
        }

        private static Category decode(Item empty, int capacity, List<Item> filled,
                                       Optional<SoundEvent> fillSound, Optional<SoundEvent> emptySound) {
            var category = new Category(empty, capacity, fillSound.orElse(null), emptySound.orElse(null));
            filled.forEach(category::addItem);
            if (category.isEmpty()) return EMPTY.get();
            return category;
        }

        public Item getEmptyContainer() {
            return emptyContainer;
        }


        /**
         * @return amount of liquid contained in this item in bottles
         */
        public int getCapacity() {
            return containerCapacity;
        }

        private void addItem(Item i) {
            // cant build a stack here, item components arent bound yet while registries load
            if (i != Items.AIR && !filled.contains(i)) filled.add(i);
        }

        public SoundEvent getFillSound() {
            return fillSound == null ? SoundEvents.BOTTLE_FILL : fillSound;
        }

        public SoundEvent getEmptySound() {
            return emptySound == null ? SoundEvents.BOTTLE_EMPTY : emptySound;
        }

        public List<Item> getFilledItems() {
            return filled;
        }

        public boolean isEmpty() {
            return this.filled.isEmpty();
        }

        public Optional<Item> getFirstFilled() {
            return this.filled.stream().findFirst();
        }
    }
}
