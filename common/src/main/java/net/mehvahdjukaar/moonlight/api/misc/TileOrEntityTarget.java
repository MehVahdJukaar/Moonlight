package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TileOrEntityTarget {

    private final Either<BlockPos, Integer> posOrEntityId;

    public TileOrEntityTarget(BlockPos pos) {
        this.posOrEntityId = Either.left(pos);
    }

    public TileOrEntityTarget(int entityId) {
        this.posOrEntityId = Either.right(entityId);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(posOrEntityId.left().isPresent());
        if (posOrEntityId.left().isPresent()) {
            buf.writeBlockPos(posOrEntityId.left().get());
        } else {
            buf.writeVarInt(posOrEntityId.right().get());
        }
    }

    public static TileOrEntityTarget read(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return new TileOrEntityTarget(buf.readBlockPos());
        } else {
            return new TileOrEntityTarget(buf.readVarInt());
        }
    }

    @Nullable
    public Object getTarget(Level level) {
        if (this.posOrEntityId.left().isPresent()) {
            BlockPos pos = this.posOrEntityId.left().get();
            var be = level.getBlockEntity(pos);
            if (be != null) return be;
            return level.getBlockState(pos);
        } else {
            return level.getEntity(this.posOrEntityId.right().get());
        }
    }


    @Nullable
    public BlockPos getPos() {
        return this.posOrEntityId.left().orElse(null);
    }

    @Nullable
    public Integer getEntityId() {
        return this.posOrEntityId.right().orElse(null);
    }
}
