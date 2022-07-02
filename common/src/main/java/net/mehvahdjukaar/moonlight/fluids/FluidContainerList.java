package net.mehvahdjukaar.moonlight.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;


public class FluidContainerList {

    /*
    public static final Codec<FluidContainerList> CODEC = RecordCodecBuilder.merge((instance) -> instance.group(
            Category.CODEC.listOf().fieldOf("containers").forGetter(FluidContainerList::encodeList)
    ).apply(instance, FluidContainerList::new));
    */

    private final Map<Item, Category> emptyToFilledMap = new HashMap<>();

    public FluidContainerList() {

    }

    public FluidContainerList(List<Category> categoryList) {
        categoryList.forEach(this::addCategory);
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


    public Optional<List<Category>> encodeList() {
        return emptyToFilledMap.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(emptyToFilledMap.values()));
    }

    public Collection<Item> getPossibleFilled() {
        List<Item> list = new ArrayList<>();
        this.emptyToFilledMap.values().forEach(c -> list.addAll(c.filled));
        return list;
    }

    public Collection<Item> getPossibleEmpty() {
        return this.emptyToFilledMap.keySet();
    }

    public Collection<Category> getCategories() {
        return this.emptyToFilledMap.values();
    }

    public void merge(FluidContainerList other) {
        other.emptyToFilledMap.values().forEach(this::addCategory);
    }

    public void add(Item empty, Item filled, int amount) {
        var c = this.emptyToFilledMap.computeIfAbsent(empty, i -> new Category(i, amount));
        c.addItem(filled);
    }

    public void add(Item empty, Item filled, int amount, SoundEvent fillSound, SoundEvent emptySound) {
        var c = this.emptyToFilledMap.computeIfAbsent(empty, i -> new Category(i, amount));
        c.addItem(filled);
        c.fillSound = fillSound;
        c.emptySound = emptySound;
    }

    public static class Category {

        private static final Category EMPTY = new Category(Items.AIR, 1);

        public static final Codec<Category> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                ResourceLocation.CODEC.fieldOf("empty").forGetter(c -> Utils.getID(c.emptyContainer)),
                Codec.INT.fieldOf("capacity").forGetter(Category::getCapacity),
                ResourceLocation.CODEC.listOf().fieldOf("filled").forGetter(c -> c.filled.stream().map(Utils::getID).toList()),
                SoundEvent.CODEC.optionalFieldOf("fill_sound").forGetter(getHackyOptional(Category::getFillSound)),
                SoundEvent.CODEC.optionalFieldOf("empty_sound").forGetter(getHackyOptional(Category::getEmptySound))
        ).apply(instance, Category::decode));

        private final Item emptyContainer;
        private final int containerCapacity;
        private SoundEvent fillSound;
        private SoundEvent emptySound;
        private final List<Item> filled = new ArrayList<>();


        private Category(Item emptyContainer, int capacity, @Nullable SoundEvent fillSound, @Nullable SoundEvent emptySound) {
            this.emptyContainer = emptyContainer;
            this.containerCapacity = capacity;
            this.fillSound = fillSound == null ? SoundEvents.BOTTLE_FILL : fillSound;
            this.emptySound = emptySound == null ? SoundEvents.BOTTLE_EMPTY : emptySound;
        }

        private Category(Item emptyContainer, int capacity) {
            this(emptyContainer, capacity, null, null);
        }

        private static Category decode(ResourceLocation empty, int capacity, List<ResourceLocation> filled,
                                       Optional<SoundEvent> fillSound, Optional<SoundEvent> emptySound) {
            if (!Registry.ITEM.containsKey(empty)) return EMPTY;
            var category = new Category(Registry.ITEM.get(empty), capacity, fillSound.orElse(null), emptySound.orElse(null));

            filled.forEach(f->{
                if(f.getNamespace().contains("immersiveengineering")){
                    int aa = 1;
                }
                if(Registry.ITEM.containsKey(f)) {
                    category.addItem(Registry.ITEM.get(f));
                }
            });
            if (category.isEmpty()) return EMPTY;
            return category;
        }

        public Item getEmptyContainer() {
            return emptyContainer;
        }

        public int getCapacity() {
            return containerCapacity;
        }

        private void addItem(Item i) {
            if (i != Items.AIR) filled.add(i);
        }

        /**
         * @return amount of liquid contained in this item in bottles
         */
        public int getAmount() {
            return containerCapacity;
        }

        public SoundEvent getFillSound() {
            return fillSound;
        }

        public SoundEvent getEmptySound() {
            return emptySound;
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

    //hacky. gets an optional if the fluid value is its default one
    private static <T> Function<Category, Optional<T>> getHackyOptional(final Function<Category, T> getter) {
        return f -> {
            var value = getter.apply(f);
            var def = getter.apply(Category.EMPTY);
            return value.equals(def) ? Optional.empty() : Optional.of(value);
        };
    }
}
