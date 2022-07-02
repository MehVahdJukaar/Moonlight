package net.mehvahdjukaar.moonlight.map.markers;

import net.mehvahdjukaar.moonlight.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.map.type.IMapDecorationType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Objects;

//see mapWorldMarker for more
public abstract class NamedMapBlockMarker<D extends CustomMapDecoration> extends MapBlockMarker<D> {

    //additional data to be stored
    @Nullable
    public Component name;

    public NamedMapBlockMarker(IMapDecorationType<D, ?> type) {
        super(type);
    }

    public NamedMapBlockMarker(IMapDecorationType<D, ?> type, BlockPos pos) {
        super(type, pos);
    }

    @Override
    public CompoundTag saveToNBT(CompoundTag compound) {
        super.saveToNBT(compound);
        if (this.name != null) {
            compound.putString("Name", Component.Serializer.toJson(this.name));
        }
        return compound;
    }

    @Override
    public void loadFromNBT(CompoundTag compound){
        super.loadFromNBT(compound);
        this.name = compound.contains("Name") ? Component.Serializer.fromJson(compound.getString("Name")) : null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            NamedMapBlockMarker<?> marker = (NamedMapBlockMarker<?>)other;
            return Objects.equals(this.getPos(), marker.getPos())&& Objects.equals(this.name, marker.name);
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.getPos(), this.name);
    }

    @Override
    public boolean shouldUpdate(MapBlockMarker<?> other) {
        if(other instanceof NamedMapBlockMarker){
            return !Objects.equals(this.name,((NamedMapBlockMarker<?>) other).name);
        }
        return false;
    }

    @Override
    public void updateDecoration(CustomMapDecoration old) {
        old.setDisplayName(this.name);
    }
}
