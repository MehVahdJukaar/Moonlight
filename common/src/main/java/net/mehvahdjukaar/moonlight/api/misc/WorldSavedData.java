package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncWorldDataMessage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public abstract class WorldSavedData extends SavedData {

    @Override
    public final CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var codec = (Codec) this.getType().getCodec();
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        var dataResult = codec.encode(this, ops, new CompoundTag());

        CompoundTag inner = (CompoundTag) dataResult.getOrThrow();

        tag.put(this.getType().getName(), inner);

        return tag;
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
    }

    //call when you want to sync to clients
    public void sync() {
        if (this.getType().isSyncable()) {
            NetworkHelper.sendToAllClientPlayers(new ClientBoundSyncWorldDataMessage<>(this));
        }
    }

    public abstract WorldSavedDataType<? extends WorldSavedData> getType();

    public void onReassigned(Level level) {

    }
}
