package net.mehvahdjukaar.selene.map.markers;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Objects;

//see mapWorldMarker for javadocs
public abstract class NamedMapWorldMarker<D extends CustomDecoration> extends MapWorldMarker<D> {

    //additional data to be stored
    @Nullable
    public Component name;

    public NamedMapWorldMarker(CustomDecorationType<D, ?> type) {
        super(type);
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
            NamedMapWorldMarker<?> marker = (NamedMapWorldMarker<?>)other;
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
    public boolean shouldUpdate(MapWorldMarker<?> other) {
        if(other instanceof NamedMapWorldMarker){
            return !Objects.equals(this.name,((NamedMapWorldMarker<?>) other).name);
        }
        return false;
    }

    @Override
    public void updateDecoration(CustomDecoration old) {
        old.setDisplayName(this.name);
    }
}
