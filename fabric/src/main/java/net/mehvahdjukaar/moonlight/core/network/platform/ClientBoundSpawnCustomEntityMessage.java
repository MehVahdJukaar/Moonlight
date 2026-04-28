package net.mehvahdjukaar.moonlight.core.network.platform;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.mehvahdjukaar.moonlight.api.entity.IExtraClientSpawnData;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

public class ClientBoundSpawnCustomEntityMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSpawnCustomEntityMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_spawn_entity"), ClientBoundSpawnCustomEntityMessage::new);

    private final Entity entity;
    private final int typeId;
    private final int entityId;
    private final UUID uuid;
    private final double posX;
    private final double posY;
    private final double posZ;
    private final byte pitch;
    private final byte yaw;
    private final byte headYaw;
    private final int velX;
    private final int velY;
    private final int velZ;
    private final RegistryFriendlyByteBuf extraBuf;

    public ClientBoundSpawnCustomEntityMessage(Entity e) {
        this.entity = e;
        this.typeId = BuiltInRegistries.ENTITY_TYPE.getId(e.getType());
        this.entityId = e.getId();
        this.uuid = e.getUUID();
        this.posX = e.getX();
        this.posY = e.getY();
        this.posZ = e.getZ();
        this.pitch = (byte) Mth.floor(e.getXRot() * 256.0F / 360.0F);
        this.yaw = (byte) Mth.floor(e.getYRot() * 256.0F / 360.0F);
        this.headYaw = (byte) ((int) (e.getYHeadRot() * 256.0F / 360.0F));
        Vec3 vec3d = e.getDeltaMovement();
        double d1 = Mth.clamp(vec3d.x, -3.9, 3.9);
        double d2 = Mth.clamp(vec3d.y, -3.9, 3.9);
        double d3 = Mth.clamp(vec3d.z, -3.9, 3.9);
        this.velX = (int) (d1 * 8000.0);
        this.velY = (int) (d2 * 8000.0);
        this.velZ = (int) (d3 * 8000.0);
        RegistryAccess ra = Objects.requireNonNull(PlatHelper.getCurrentServer()).registryAccess();
        this.extraBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), ra);
        if (this.entity instanceof IExtraClientSpawnData spawnData) {
            spawnData.writeSpawnData(extraBuf);
        }
    }

    public ClientBoundSpawnCustomEntityMessage(RegistryFriendlyByteBuf buf) {
        this.entity = null;
        this.typeId = buf.readVarInt();
        this.entityId = buf.readInt();
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.pitch = buf.readByte();
        this.yaw = buf.readByte();
        this.headYaw = buf.readByte();
        this.velX = buf.readShort();
        this.velY = buf.readShort();
        this.velZ = buf.readShort();
        this.extraBuf = new RegistryFriendlyByteBuf(buf.copy(), buf.registryAccess());
        buf.clear();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.typeId);
        buf.writeInt(this.entityId);
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeByte(this.pitch);
        buf.writeByte(this.yaw);
        buf.writeByte(this.headYaw);
        buf.writeShort(this.velX);
        buf.writeShort(this.velY);
        buf.writeShort(this.velZ);
        buf.writeBytes(this.extraBuf);
    }

    @Override
    public void handle(Context context) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.byId(this.typeId);

        Level world = context.getPlayer().level();
        Entity e = type.create(world);
        if (e != null) {
            e.syncPacketPositionCodec(this.posX, this.posY, this.posZ);
            e.absMoveTo(this.posX, this.posY, this.posZ, (this.yaw * 360) / 256.0F, (this.pitch * 360) / 256.0F);
            e.setYHeadRot((this.headYaw * 360) / 256.0F);
            e.setYBodyRot((this.headYaw * 360) / 256.0F);
            e.setId(this.entityId);
            e.setUUID(this.uuid);
            Objects.requireNonNull(ClientLevel.class);

            clientSideStuff(world, e);

            e.lerpMotion(this.velX / 8000.0, this.velY / 8000.0, this.velZ / 8000.0);
            if (e instanceof IExtraClientSpawnData spawnData) {
                spawnData.readSpawnData(this.extraBuf);
            }
        }
        this.extraBuf.clear();
    }

    @ClientOnly
    private void clientSideStuff(Level world, Entity e) {
        ((ClientLevel) world).addEntity(e);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
