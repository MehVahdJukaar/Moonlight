package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundParticleAroundBlockMessage;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public interface IGlowable {

    boolean isGlowing();

    void setGlowing(boolean b);

    //callable on both sides
    default InteractionResult tryGlowingWithItem(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (stack.is(Items.GLOW_INK_SAC)) {
            if (isGlowing()) {
                level.playSound(player, pos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
                return InteractionResult.FAIL;
            }
            level.playSound(player, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS);

            stack.consume(1, player);
            this.setGlowing(true);

            //server logic. stuff should be sent by packets here
            if (player instanceof ServerPlayer serverPlayer) {

                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                NetworkHelper.sendToAllClientPlayersInParticleRange(serverPlayer.level(), pos,
                        new ClientBoundParticleAroundBlockMessage(pos, ClientBoundParticleAroundBlockMessage.Kind.GLOW_ON));
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
