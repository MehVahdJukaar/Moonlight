package net.mehvahdjukaar.moonlight.api.misc;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncWorldDataMessage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public abstract class WorldSavedData extends SavedData {

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
