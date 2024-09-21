package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;


public interface CustomMapData<C extends CustomMapData.DirtyCounter, P> {


    record Type<P, T extends CustomMapData<?, P>>(ResourceLocation id, Supplier<T> factory,
                                                  StreamCodec<RegistryFriendlyByteBuf, P> patchCodec) {

        public static final Codec<Type<?, ?>> CODEC = MapDataInternal.CUSTOM_MAP_DATA_TYPES;
        public static final StreamCodec<FriendlyByteBuf, Type<?, ?>> STREAM_CODEC = MapDataInternal.CUSTOM_MAP_DATA_TYPES.getStreamCodec();

        @SuppressWarnings("unchecked")
        @NotNull
        public T get(MapItemSavedData mapData) {
            return (T) ((ExpandedMapData) mapData).ml$getCustomData().get(this.id);
        }

    }

    Type<P, ?> getType();

    default boolean persistOnCopyOrLock() {
        return true;
    }

    default boolean onItemUpdate(MapItemSavedData data, Entity entity) {
        return false;
    }

    @Nullable
    default Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
        return null;
    }

    C createDirtyCounter();

    void load(CompoundTag tag, HolderLookup.Provider lookup);

    void save(CompoundTag tag, HolderLookup.Provider lookup);

    P createUpdatePatch(C dirtyCounter);

    void applyUpdatePatch(P patch);

    default void setDirty(MapItemSavedData data, Consumer<C> dirtySetter) {
        Type<P, ?> type = this.getType();
        ((ExpandedMapData) data).ml$setCustomDataDirty(type, dirtySetter);
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

    // what's send via packet. Wraps the patch with its type to be able to decode it later
    record DirtyDataPatch<P, D extends CustomMapData<?, P>>(CustomMapData.Type<P, D> type, P patch) {
        public static final StreamCodec<RegistryFriendlyByteBuf, DirtyDataPatch<?, ?>> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, DirtyDataPatch<?, ?> dirtyData) {
                Type.STREAM_CODEC.encode(buf, dirtyData.type);
                encodeTyped(buf, dirtyData);
            }

            private static <P> void encodeTyped(RegistryFriendlyByteBuf buf, DirtyDataPatch<P, ?> dirtyData) {
                dirtyData.type.patchCodec().encode(buf, dirtyData.patch);
            }

            @Override
            public DirtyDataPatch<?, ?> decode(RegistryFriendlyByteBuf buf) {
                CustomMapData.Type<?, ?> type = Type.STREAM_CODEC.decode(buf);
                return decodeTyped(buf, type);
            }

            private static <P, D extends CustomMapData<?, P>> DirtyDataPatch<P, D> decodeTyped(RegistryFriendlyByteBuf buf, Type<P, D> type) {
                P decode = type.patchCodec().decode(buf);
                return new DirtyDataPatch<>(type, decode);
            }
        };

        public void apply(Map<Type<?, ?>, CustomMapData<?, ?>> customData) {
            CustomMapData<?, P> data = (CustomMapData<?, P>) customData.get(this.type);
            data.applyUpdatePatch(this.patch);
        }
    }

}

