package net.mehvahdjukaar.selene.example.map;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.Objects;

public class ExampleMarker extends MapBlockMarker<CustomDecoration> {
    //additional data to be stored
    @Nullable
    private Component name;

    public ExampleMarker() {
        super(ExampleReg.EXAMPLE_DECORATION_TYPE);
    }

    public ExampleMarker(BlockPos pos, Component name) {
        super(ExampleReg.EXAMPLE_DECORATION_TYPE,pos);
        this.name = name;
    }

    @Override
    public CompoundTag saveToNBT(CompoundTag compound) {
        super.saveToNBT(compound);
        if (this.name != null) {
            compound.putString("Name", Component.Serializer.toJson(this.name));
        }
        return compound;
    }

    //get a marker from nbt
    public void loadFromNBT(CompoundTag compound){
        super.loadFromNBT(compound);
        this.name = compound.contains("Name") ? Component.Serializer.fromJson(compound.getString("Name")) : null;
    }

    //get a marker from world
    @Nullable
    public static ExampleMarker getFromWorld(BlockGetter world, BlockPos pos){
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SignBlockEntity sign) {
            Component name = sign.getMessage(0, false);
            return new ExampleMarker(pos,name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CustomDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new CustomDecoration(this.getType(),mapX,mapY,rot,name);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            ExampleMarker marker = (ExampleMarker)other;
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
        if(other instanceof ExampleMarker){
            return !Objects.equals(this.name,((ExampleMarker) other).name);
        }
        return false;
    }

    @Override
    public void updateDecoration(CustomDecoration old) {
        old.setDisplayName(this.name);
    }
}
