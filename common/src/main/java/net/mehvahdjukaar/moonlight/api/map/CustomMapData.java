package net.mehvahdjukaar.moonlight.api.map;

import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;


public interface CustomMapData<H extends CustomMapData.DirtyCounter> {


    record Type<T extends CustomMapData<?>>(ResourceLocation id, Supplier<T> factory) {

        @SuppressWarnings("unchecked")
        @NotNull
        public T get(MapItemSavedData mapData) {
            return (T) ((ExpandedMapData) mapData).getCustomData().get(this.id);
        }

    }

    Type<?> getType();

    default boolean persistOnCopyOrLock(){
        return true;
    }

    default boolean onItemUpdate(MapItemSavedData data, Entity entity) {
        return false;
    }

    @Nullable
    default Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
        return null;
    }

    H createDirtyCounter();

    void load(CompoundTag tag);

    void loadUpdateTag(CompoundTag tag);

    void save(CompoundTag tag);

    void saveToUpdateTag(CompoundTag tag, H dirtyCounter);

    default void setDirty(MapItemSavedData data, Consumer<H> dirtySetter) {
        Type<?> type = this.getType();
        ((ExpandedMapData) data).setCustomDataDirty(type, dirtySetter);
    }

    class SimpleDirtyCounter implements DirtyCounter {
        private boolean dirty = true;

        public void markDirty() {
            this.dirty = true;
        }

        public boolean isDirty() {
            return dirty;
        }

        @Override
        public void clearDirty() {
            dirty = false;
        }
    }

    interface DirtyCounter {

        boolean isDirty();

        void clearDirty();
    }
}

