package net.mehvahdjukaar.moonlight.api.map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;


public interface CustomMapData {

    record Type<T extends CustomMapData>(ResourceLocation id, Function<CompoundTag, T> factory) {

        @NotNull
        public CustomMapData createFromBuffer(FriendlyByteBuf buffer) {
            return factory.apply(buffer.readNbt());
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public T get(MapItemSavedData mapData) {
            return (T) ((ExpandedMapData) mapData).getCustomData().get(this.id);
        }

        public T getOrCreate(MapItemSavedData mapData, Supplier<T> constructor) {
            return (T) ((ExpandedMapData) mapData).getCustomData().computeIfAbsent(this.id, r -> constructor.get());
        }
    }

    Type<?> getType();


    default boolean onItemUpdate(MapItemSavedData data, Entity entity) {
        return false;
    }

    @Nullable
    default Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
        return null;
    }

    void save(CompoundTag tag);

    default void saveToBuffer(FriendlyByteBuf buffer) {
        CompoundTag tag = new CompoundTag();
        this.save(tag);
        buffer.writeNbt(tag);
    }


    default void setDirty(MapItemSavedData data){
        ((ExpandedMapData) data).setCustomDataDirty();
    }

}

