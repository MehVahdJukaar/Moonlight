//
// Source code recreated from a .class file by Quiltflower
//

package net.mehvahdjukaar.moonlight.core.fake_player;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class FakeServerPlayer extends ServerPlayer {
    // Map of all active fake player usernames to their entities
    // automatically gets cleaned when level is unloaded as key won't be in use anymore
    private static final WeakHashMap<ServerLevel, Map<GameProfile, FakeServerPlayer>> FAKE_PLAYERS = new WeakHashMap<>();

    public FakeServerPlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile) {
        super(minecraftServer, serverLevel, gameProfile);
        this.connection = new FakeServerPlayer.FakePlayerNetHandler(level.getServer(), this);
    }

    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */
    public static FakeServerPlayer get(ServerLevel level, GameProfile username) {
        return FAKE_PLAYERS.computeIfAbsent(level, l-> new HashMap<>())
                .computeIfAbsent(username, u -> new FakeServerPlayer(level.getServer(), level, username));
    }

    public static void unloadLevel(Level level) {
        for (var v : FAKE_PLAYERS.keySet()) {
            if (v == level) FAKE_PLAYERS.remove(v);
        }
    }

    private final EntityDimensions dimensions = EntityDimensions.fixed(0, 0);



    @Override
    public Vec3 position() {
        return new Vec3(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public BlockPos blockPosition() {
        return new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
    }

    @Override
    public void setXRot(float pXRot) {
        super.setXRot(pXRot);
        this.xRotO = pXRot;
    }

    @Override
    public void setYRot(float pYRot) {
        super.setYRot(pYRot);
        this.yRotO = pYRot;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return dimensions;
    }

    @Override
    public void displayClientMessage(Component chatComponent, boolean actionBar) {
    }

    @Override
    public void awardStat(Stat stat, int increment) {
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean canHarmPlayer(Player other) {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
    }

    @Override
    public void tick() {
    }

    @Override
    public void updateOptions(ServerboundClientInformationPacket packet) {
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return PlatHelper.getCurrentServer();
    }

    public static void init(){
        FakePlayerNetHandler.init();
    }

    @ParametersAreNonnullByDefault
    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
            super(server, DUMMY_CONNECTION, player);
        }

        public static void init() {
        }

        @Override
        public void tick() {
        }

        @Override
        public void resetPosition() {
        }

        @Override
        public void disconnect(Component textComponent) {
        }

        @Override
        public void handlePlayerInput(ServerboundPlayerInputPacket packet) {
        }

        @Override
        public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {
        }

        @Override
        public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {
        }

        @Override
        public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {
        }

        @Override
        public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {
        }

        @Override
        public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {
        }

        @Override
        public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {
        }

        @Override
        public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {
        }

        @Override
        public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {
        }

        @Override

        public void handlePickItem(ServerboundPickItemPacket packet) {
        }

        @Override
        public void handleRenameItem(ServerboundRenameItemPacket packet) {
        }

        @Override
        public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {
        }

        @Override
        public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {
        }

        @Override
        public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {
        }

        @Override
        public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {
        }

        @Override
        public void handleSelectTrade(ServerboundSelectTradePacket packet) {
        }

        @Override
        public void handleEditBook(ServerboundEditBookPacket packet) {
        }

        @Override
        public void handleEntityTagQuery(ServerboundEntityTagQuery packet) {
        }

        @Override
        public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) {
        }

        @Override
        public void handleMovePlayer(ServerboundMovePlayerPacket packet) {
        }

        @Override
        public void teleport(double x, double y, double z, float yaw, float pitch) {
        }

        @Override
        public void teleport(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet) {
            super.teleport(x, y, z, yaw, pitch, relativeSet);
        }

        @Override
        public void handlePlayerAction(ServerboundPlayerActionPacket packet) {
        }

        @Override
        public void handleUseItemOn(ServerboundUseItemOnPacket packet) {
        }

        @Override
        public void handleUseItem(ServerboundUseItemPacket packet) {
        }

        @Override
        public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {
        }

        @Override
        public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {
        }

        @Override
        public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {
        }

        @Override
        public void onDisconnect(Component reason) {
        }

        @Override
        public void send(Packet<?> packet) {
        }

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
        }

        @Override
        public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {
        }

        @Override
        public void handleChat(ServerboundChatPacket packet) {
        }

        @Override
        public void handleAnimate(ServerboundSwingPacket packet) {
        }

        @Override
        public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {
        }

        @Override
        public void handleInteract(ServerboundInteractPacket packet) {
        }

        @Override
        public void handleClientCommand(ServerboundClientCommandPacket packet) {
        }

        @Override
        public void handleContainerClose(ServerboundContainerClosePacket packet) {
        }

        @Override
        public void handleContainerClick(ServerboundContainerClickPacket packet) {
        }

        @Override
        public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {
        }

        @Override
        public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {
        }

        @Override
        public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {
        }

        @Override
        public void handleSignUpdate(ServerboundSignUpdatePacket packet) {
        }

        @Override
        public void handleKeepAlive(ServerboundKeepAlivePacket packet) {
        }

        @Override
        public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {
        }

        @Override
        public void handleClientInformation(ServerboundClientInformationPacket packet) {
        }

        @Override
        public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        }

        @Override
        public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {
        }

        @Override
        public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {
        }
    }
}
