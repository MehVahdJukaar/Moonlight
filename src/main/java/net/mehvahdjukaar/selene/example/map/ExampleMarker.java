package net.mehvahdjukaar.selene.example.map;

import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

public class ExampleMarker extends MapWorldMarker<CustomDecoration> {
    //additional data to be stored
    @Nullable
    private ITextComponent name;

    public ExampleMarker() {
        super(ExampleReg.EXAMPLE_DECORATION_TYPE);
    }

    public ExampleMarker(BlockPos pos, ITextComponent name) {
        this();
        this.setPos(pos);
        this.name = name;
    }

    @Override
    public CompoundNBT saveToNBT(CompoundNBT compound) {
        super.saveToNBT(compound);
        if (this.name != null) {
            compound.putString("Name", ITextComponent.Serializer.toJson(this.name));
        }
        return compound;
    }

    //get a marker from nbt
    public void loadFromNBT(CompoundNBT compound){
        super.loadFromNBT(compound);
        this.name = compound.contains("Name") ? ITextComponent.Serializer.fromJson(compound.getString("Name")) : null;
    }

    //get a marker from world
    @Nullable
    public static ExampleMarker getFromWorld(IBlockReader world, BlockPos pos){
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SignTileEntity) {
            SignTileEntity sign = ((SignTileEntity) tileentity);
            ITextComponent name = sign.getMessage(0);
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
    public boolean shouldUpdate(MapWorldMarker<?> other) {
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
