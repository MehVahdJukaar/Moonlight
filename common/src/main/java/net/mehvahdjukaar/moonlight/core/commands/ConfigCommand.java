package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundOpenConfigScreenMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Set;

/** Opens the config screens on the caller's own client: the mods list, or one mod's config. */
public class ConfigCommand {

    private static final String MOD_ARG = "mod_id";

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("config")
                .executes(ctx -> open(ctx, ""))
                .then(Commands.argument(MOD_ARG, StringArgumentType.word())
                        // every installed mod: whether one actually has a screen is a client side question, so the
                        // client answers it when the packet lands rather than the server guessing here
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(suggestedMods(), builder))
                        .executes(ctx -> open(ctx, StringArgumentType.getString(ctx, MOD_ARG))));
    }

    private static int open(CommandContext<CommandSourceStack> ctx, String modId) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0; // console: nothing to open a screen on
        NetworkHelper.sendToClientPlayer(player, new ClientBoundOpenConfigScreenMessage(modId));
        return 1;
    }

    /** Installed mods minus the ones that are really the platform: the game, the JVM, the loader and its api modules. */
    private static List<String> suggestedMods() {
        return PlatHelper.getInstalledMods().stream()
                .filter(id -> !NOT_MODS.contains(id) && !id.startsWith("fabric-"))
                .toList();
    }

    private static final Set<String> NOT_MODS = Set.of(
            "minecraft", "java", "fabric", "fabricloader", "forge", "neoforge", "mixinextras");
}
