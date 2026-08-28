package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

@FunctionalInterface
public interface Registrator<T> {

    void register(Identifier name, T instance);

    default void register(String name, T instance) {
        register(Identifier.parse(name), instance);
    }

    // blocks need their key before creation, hence the factory
    default void registerBlock(Identifier name, Function<BlockBehaviour.Properties, ? extends T> factory,
                               BlockBehaviour.Properties properties) {
        register(name, factory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, name))));
    }

    default void registerItem(Identifier name, Function<Item.Properties, ? extends T> factory,
                              Item.Properties properties) {
        register(name, factory.apply(properties.setId(ResourceKey.create(Registries.ITEM, name))));
    }

    default void registerItem(Identifier name, Function<Item.Properties, ? extends T> factory) {
        registerItem(name, factory, new Item.Properties());
    }
}
