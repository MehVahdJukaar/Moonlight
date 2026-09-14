package net.mehvahdjukaar.moonlight.api.platform;

import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Helper that helps hook configs and tab operations
 */
public class TabAdderHelper {

    private static final Predicate<String> NOTHING_ENABLED = f -> false;

    private RegHelper.ItemToTabEvent event;
    private final Predicate<String> isFeatureEnabled;

    public TabAdderHelper(RegHelper.ItemToTabEvent event, ModConfigHolder configs) {
        this(event, configs::isFeatureEnabled);
    }

    public TabAdderHelper(RegHelper.ItemToTabEvent event, Predicate<String> isFeatureEnabled) {
        this.event = event;
        this.isFeatureEnabled = isFeatureEnabled;
    }

    /**
     * Appends everything to this tab instead of next to its vanilla neighbour, dropping duplicates.
     * Null is a no op, for a tab you only register when a config asks for one.
     */
    public TabAdderHelper intoSingleTab(@Nullable RegSupplier<CreativeModeTab> tab) {
        return intoSingleTab(tab == null ? null : tab.getKey());
    }

    public TabAdderHelper intoSingleTab(@Nullable ResourceKey<CreativeModeTab> tab) {
        if (tab != null) this.event = new SingleTabRedirect(this.event, tab);
        return this;
    }

    public TabAdderHelper when(boolean condition) {
        return condition ? this : new TabAdderHelper(event, NOTHING_ENABLED);
    }

    public RegHelper.ItemToTabEvent event() {
        return event;
    }

    public static boolean anyItemTagExists(String... tags) {
        for (var t : tags) {
            if (BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(t))).isPresent()) {
                return true;
            }
        }
        return false;
    }

    public void before(TagKey<Item> target, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        before(i -> i.is(target), tab, feature, items);
    }

    public void before(ItemLike target, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        before(i -> i.is(target.asItem()), tab, feature, items);
    }

    /** No op if no mod registered that id. */
    public void before(String targetId, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        find(targetId).ifPresent(target -> before(target, tab, feature, items));
    }

    public void before(Predicate<ItemStack> target, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        if (isFeatureEnabled.test(feature)) unwrap(items, stacks -> event.addBefore(tab, target, stacks));
    }

    public void after(TagKey<Item> target, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        after(i -> i.is(target), tab, feature, items);
    }

    public void after(ItemLike target, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        after(i -> i.is(target.asItem()), tab, feature, items);
    }

    /** No op if no mod registered that id. */
    public void after(String targetId, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        find(targetId).ifPresent(target -> after(target, tab, feature, items));
    }

    public void after(Predicate<ItemStack> target, ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        if (isFeatureEnabled.test(feature)) unwrap(items, stacks -> event.addAfter(tab, target, stacks));
    }

    public void add(ResourceKey<CreativeModeTab> tab, String feature, Supplier<?>... items) {
        if (isFeatureEnabled.test(feature)) unwrap(items, stacks -> event.add(tab, stacks));
    }

    private static Optional<Item> find(String itemId) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId));
    }

    private static void unwrap(Supplier<?>[] items, Consumer<ItemStack[]> action) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Supplier<?> s : items) {
            flatten(s.get(), stacks);
        }
        if (!stacks.isEmpty()) action.accept(stacks.toArray(ItemStack[]::new));
    }

    private static void flatten(Object o, List<ItemStack> out) {
        if (o instanceof ItemStack stack) {
            out.add(stack);
        } else if (o instanceof ItemLike item) {
            ItemStack stack = item.asItem().getDefaultInstance();
            if (stack.isEmpty()) {
                throw new IllegalStateException("Tried to add empty item " + item + " to a creative tab. " +
                        "Something probably called asItem before items were registered");
            }
            out.add(stack);
        } else if (o instanceof Collection<?> c) {
            for (Object i : c) flatten(i, out);
        } else if (o instanceof Object[] a) {
            for (Object i : a) flatten(i, out);
        } else {
            throw new IllegalArgumentException("Don't know how to add " + o + " to a creative tab");
        }
    }

    private record SingleTabRedirect(RegHelper.ItemToTabEvent parent, ResourceKey<CreativeModeTab> target,
                                     List<ItemStack> alreadyAdded) implements RegHelper.ItemToTabEvent {

        SingleTabRedirect(RegHelper.ItemToTabEvent parent, ResourceKey<CreativeModeTab> target) {
            this(parent, target, new ArrayList<>());
        }

        @Override
        public CreativeModeTab.ItemDisplayParameters getParameters() {
            return parent.getParameters();
        }

        @Override
        public CreativeModeTab getTab() {
            return parent.getTab();
        }

        @Override
        public void addItems(ResourceKey<CreativeModeTab> tab, @Nullable Predicate<ItemStack> anchor,
                             boolean after, List<ItemStack> items) {
            List<ItemStack> unseen = new ArrayList<>();
            for (ItemStack stack : items) {
                if (isNew(stack)) unseen.add(stack);
            }
            if (!unseen.isEmpty()) parent.addItems(target, null, true, unseen);
        }

        @Override
        public void remove(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> condition) {
            parent.remove(tab, condition);
        }

        private boolean isNew(ItemStack stack) {
            for (ItemStack s : alreadyAdded) {
                if (ItemStack.isSameItemSameComponents(s, stack)) return false;
            }
            alreadyAdded.add(stack);
            return true;
        }
    }
}
