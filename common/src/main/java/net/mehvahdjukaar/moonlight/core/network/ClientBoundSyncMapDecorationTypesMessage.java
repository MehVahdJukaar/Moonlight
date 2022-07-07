package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.SimpleDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//after data load
public class ClientBoundSyncMapDecorationTypesMessage implements Message {
    private final Collection<SimpleDecorationType> simpleTypes;

    public ClientBoundSyncMapDecorationTypesMessage(Collection<SimpleDecorationType> simpleTypes) {
        this.simpleTypes = simpleTypes;
    }

    public ClientBoundSyncMapDecorationTypesMessage(FriendlyByteBuf pBuffer) {
        int size = pBuffer.readVarInt();
        simpleTypes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            var r = SimpleDecorationType.CODEC.parse(NbtOps.INSTANCE, pBuffer.readAnySizeNbt());
            r.result().ifPresent(simpleTypes::add);
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

        List<CompoundTag> encoded = new ArrayList<>();
        for (SimpleDecorationType f : this.simpleTypes) {
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

    @Override
    public void handle(ChannelHandler.Context context) {
        if (context.getDirection() == ChannelHandler.NetworkDir.PLAY_TO_CLIENT) {
            MapDecorationRegistry.DATA_DRIVEN_REGISTRY.acceptClientTypes(this.simpleTypes);

            //TODO: remove from here
            //SoftFluidRegistry.postInitClient();
        }
    }

}
