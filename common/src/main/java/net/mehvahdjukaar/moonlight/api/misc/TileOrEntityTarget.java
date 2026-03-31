package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.moonlight.api.block.IEntityCarry;
import net.mehvahdjukaar.moonlight.api.entity.ITileEntityCarry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TileOrEntityTarget {

    private final Either<BlockPos, Integer> posOrEntityId;

    private TileOrEntityTarget(Either<BlockPos, Integer> either) {
        this.posOrEntityId = either;
    }


    //bad code
    public static TileOrEntityTarget orThrow(Object object) {
        if (object instanceof BlockEntity be) {
            return TileOrEntityTarget.of(be);
        } else if (object instanceof Entity entity) {
            return TileOrEntityTarget.of(entity);
        } else {
            throw new IllegalArgumentException("Object must be a BlockEntity or Entity");
        }
    }

    public static TileOrEntityTarget of(BlockEntity be) {
        return new TileOrEntityTarget(Either.left(be.getBlockPos()));
    }

    public static TileOrEntityTarget of(Entity entity) {
        return new TileOrEntityTarget(Either.right(entity.getId()));
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
            return new TileOrEntityTarget(Either.left(buf.readBlockPos()));
        } else {
            return new TileOrEntityTarget(Either.right(buf.readVarInt()));
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

    public <T extends BlockEntity> T getBlockEntityOrThrow(Level level, BlockEntityType<T> type) {
        if (this.posOrEntityId.left().isPresent()) {
            var be = type.getBlockEntity(level, this.posOrEntityId.left().get());
            if (be != null) {
                return be;
            }
        }
        throw new IllegalStateException("No BlockEntity found at " + this.posOrEntityId.left().orElse(null));
    }

    public BlockEntity findTileOrContainedTile(Level level) {
        return this.map(level, be -> be, e ->
                e instanceof ITileEntityCarry tc ? tc.getCarriedTileEntity() : null);
    }

    public Entity findEntityOrContainedEntity(Level level) {
        return this.map(level, be -> be instanceof IEntityCarry ec ? ec.getCarriedEntity() : null,
                e -> e);
    }

    public <T extends Entity> T getEntityOrThrow(Level level, EntityType<T> type) {
        if (this.posOrEntityId.right().isPresent()) {
            var entity = level.getEntity(this.posOrEntityId.right().get());
            if (entity != null && entity.getType() == type) {
                return (T) (entity);
            }
        }
        throw new IllegalStateException("No Entity found with ID " + this.posOrEntityId.right().orElse(null));
    }

    @Nullable
    public <T> T map(Level level, Function<BlockEntity, T> a, Function<Entity, T> b) {
        if (this.posOrEntityId.left().isPresent()) {
            var be = level.getBlockEntity(this.posOrEntityId.left().get());
            if (be != null) return a.apply(be);
        } else {
            var entity = level.getEntity(this.posOrEntityId.right().get());
            if (entity != null) return b.apply(entity);
        }
        return null;
    }


    @Nullable
    public BlockPos getPos() {
        return this.posOrEntityId.left().orElse(null);
    }

    @Nullable
    public Integer getEntityId() {
        return this.posOrEntityId.right().orElse(null);
    }

    @Override
    public String toString() {
        return "TileOrEntityTarget{" +
                "posOrEntityId=" + posOrEntityId +
                '}';
    }
}
