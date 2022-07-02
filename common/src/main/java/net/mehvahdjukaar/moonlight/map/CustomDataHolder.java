package net.mehvahdjukaar.moonlight.map;

import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

//TODO: this wasn't really needed... could have done with simply a simple mixin since after all I'm only using it for 1 boolean value
public record CustomDataHolder<T>(ResourceLocation id,
                                  Function<CompoundTag, T> load,
                                  BiConsumer<CompoundTag, T> save,
                                  PropertyDispatch.TriFunction<MapItemSavedData, Entity, T, Boolean> onItemUpdate,
                                  PropertyDispatch.TriFunction<MapItemSavedData, ItemStack, T, Component> onItemTooltip) {

    @Nullable
    public Instance<T> create(CompoundTag tag) {
        T v = this.load.apply(tag);
        if (v == null) return null;
        return new Instance<>(v, this);
    }
    @Nullable
    public Instance<T> createFromBuffer(FriendlyByteBuf buffer){
        return this.create(buffer.readNbt());
    }

    public static class Instance<T> {

        private T object;
        private final CustomDataHolder<T> type;

        private Instance(T value, @Nonnull CustomDataHolder<T> type) {
            this.object = value;
            this.type = type;
        }
        public CustomDataHolder<T> getType(){
            return type;
        }

        public void save(CompoundTag tag) {
            this.type.save.accept(tag, this.object);
        }

        public boolean onUpdate(MapItemSavedData data, Entity entity) {
            return this.type.onItemUpdate.apply(data, entity, this.object);
        }

        @Nullable
        public Component getTooltip(MapItemSavedData data, ItemStack stack) {
            return this.type.onItemTooltip.apply(data, stack, this.object);
        }

        public void set(T value) {
            this.object = value;
        }

        public void saveToBuffer(FriendlyByteBuf buffer) {
            CompoundTag tag = new CompoundTag();
            this.save(tag);
            buffer.writeNbt(tag);
        }
    }

}
