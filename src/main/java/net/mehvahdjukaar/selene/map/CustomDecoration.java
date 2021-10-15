package net.mehvahdjukaar.selene.map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Objects;

//default base simple decoration. this will be instanced in a map. equivalent of a tile entity
public class CustomDecoration {
    private final CustomDecorationType<?,?> type;
    private Component displayName;
    private byte x;
    private byte y;
    private byte rot;

    public CustomDecoration(CustomDecorationType<?,?> type, byte x, byte y, byte rot, @Nullable Component displayName) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.rot = rot;
        this.displayName = displayName;
    }

    public CustomDecorationType<?,?> getType() {
        return this.type;
    }

    public String getTypeId(){
        return this.type.getRegistryId();
    }

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

    public byte getRot() {
        return this.rot;
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }

    public void setRot(byte rot) {
        this.rot = rot;
    }

    public void setX(byte x) {
        this.x = x;
    }

    public void setY(byte y) {
        this.y = y;
    }

    @Nullable
    public Component getDisplayName() {
        return this.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof CustomDecoration)) {
            return false;
        } else {
            CustomDecoration mapdecoration = (CustomDecoration)obj;
            if (this.type != mapdecoration.type) {
                return false;
            } else if (this.rot != mapdecoration.rot) {
                return false;
            } else if (this.x != mapdecoration.x) {
                return false;
            } else if (this.y != mapdecoration.y) {
                return false;
            } else {
                return Objects.equals(this.displayName, mapdecoration.displayName);
            }
        }
    }

    @Override
    public int hashCode() {
        int i = this.type.getId().hashCode();
        i = 31 * i + this.x;
        i = 31 * i + this.y;
        i = 31 * i + this.rot;
        return 31 * i + Objects.hashCode(this.displayName);
    }

    /**
     * used to send decoration data to client
     * implement this if you are adding new data to this base decoration class
     * @param buffer packed buffer
     */
    public void saveToBuffer(FriendlyByteBuf buffer){
        buffer.writeByte(this.getX());
        buffer.writeByte(this.getY());
        buffer.writeByte(this.getRot() & 15);
        if (this.getDisplayName() != null) {
            buffer.writeBoolean(true);
            buffer.writeComponent(this.getDisplayName());
        } else {
            buffer.writeBoolean(false);
        }
    }

    /**
     * used to load decoration data on client. must match saveToBuffer
     * implement this if you are adding new data to this base decoration class
     * @param buffer packed buffer
     */
    public CustomDecoration(CustomDecorationType<?,?> type, FriendlyByteBuf buffer){
        this(type, buffer.readByte(), buffer.readByte(), (byte)(buffer.readByte() & 15), buffer.readBoolean() ? buffer.readComponent() : null);
    }

}

