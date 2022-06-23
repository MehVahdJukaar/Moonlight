package net.mehvahdjukaar.moonlight.network;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.map.type.SimpleDecorationType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

//after data load
public class ClientBoundSyncMapDecorationTypesPacket {
    private final Collection<SimpleDecorationType> simpleTypes;

    public ClientBoundSyncMapDecorationTypesPacket(Collection<SimpleDecorationType> simpleTypes) {
        this.simpleTypes = simpleTypes;
    }

    public ClientBoundSyncMapDecorationTypesPacket(FriendlyByteBuf pBuffer) {
        int size = pBuffer.readVarInt();
        simpleTypes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            var r = SimpleDecorationType.CODEC.parse(NbtOps.INSTANCE, pBuffer.readAnySizeNbt());
            r.result().ifPresent(simpleTypes::add);
        }
    }

    public static void buffer(ClientBoundSyncMapDecorationTypesPacket message, FriendlyByteBuf buffer) {

        List<CompoundTag> encoded = new ArrayList<>();
        for (SimpleDecorationType f : message.simpleTypes) {
            try {
                var r = SimpleDecorationType.CODEC.encodeStart(NbtOps.INSTANCE, f).resultOrPartial(
                        e -> Moonlight.LOGGER.error("Failed encoding Simple Map Decoration {} : {}", f, e)
                );
                encoded.add(((CompoundTag) r.get()));
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed encoding Soft Fluid {} : {}", f, e);
            }
        }
        buffer.writeVarInt(encoded.size());
        encoded.forEach(buffer::writeNbt);
    }

    public static void handler(ClientBoundSyncMapDecorationTypesPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                MapDecorationRegistry.DATA_DRIVEN_REGISTRY.acceptClientTypes(message.simpleTypes);

                //TODO: remove from here
                SoftFluidRegistry.postInitClient();
            }
        });
        context.setPacketHandled(true);
    }

}
