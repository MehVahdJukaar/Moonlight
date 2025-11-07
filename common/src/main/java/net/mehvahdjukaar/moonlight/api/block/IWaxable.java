package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundParticleAroundBlockMessage;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IWaxable {

    boolean isWaxed();

    void setWaxed(boolean b);

    //callable on both sides
    default ItemInteractionResult tryWaxingWithItem(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (stack.getItem() instanceof HoneycombItem) {
            if (isWaxed()) {
                level.playSound(player, pos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
                return ItemInteractionResult.FAIL;
            }
            level.playSound(player, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS);

            stack.consume(1, player);
            this.setWaxed(true);

            //server logic. stuff should be sent my packets here
            if (player instanceof ServerPlayer serverPlayer) {

                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                NetworkHelper.sendToAllClientPlayersInParticleRange(serverPlayer.serverLevel(), pos,
                        new ClientBoundParticleAroundBlockMessage(pos, ClientBoundParticleAroundBlockMessage.Kind.WAX_ON));
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
