package net.mehvahdjukaar.selene.network;

import net.mehvahdjukaar.selene.Moonlight;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ClientBoundSyncFluidsPacket {
    private final Collection<SoftFluid> fluids;

    public ClientBoundSyncFluidsPacket(Collection<SoftFluid> fluids) {
        this.fluids = fluids;
    }

    public ClientBoundSyncFluidsPacket(FriendlyByteBuf pBuffer) {
        int size = pBuffer.readVarInt();
        fluids = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            var r = SoftFluid.CODEC.parse(NbtOps.INSTANCE, pBuffer.readAnySizeNbt());
            r.result().ifPresent(fluids::add);
        }
    }

    public static void buffer(ClientBoundSyncFluidsPacket message, FriendlyByteBuf buffer) {

        List<CompoundTag> encoded = new ArrayList<>();
        for (SoftFluid f : message.fluids) {
            try {
                var r = SoftFluid.CODEC.encodeStart(NbtOps.INSTANCE, f).resultOrPartial(
                        e -> Moonlight.LOGGER.error("Failed encoding Soft Fluid {} : {}", f, e)
                );
                encoded.add(((CompoundTag) r.get()));
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed encoding Soft Fluid {} : {}", f, e);
            }
        }
        buffer.writeVarInt(encoded.size());
        encoded.forEach(buffer::writeNbt);
    }

    public static void handler(ClientBoundSyncFluidsPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                SoftFluidRegistry.acceptClientFluids(message);
            }
        });
        context.setPacketHandled(true);
    }

    public Collection<SoftFluid> getFluids() {
        return fluids;
    }
}
