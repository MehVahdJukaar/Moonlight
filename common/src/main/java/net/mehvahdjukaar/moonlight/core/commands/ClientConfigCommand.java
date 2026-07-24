package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.config.ModsTilesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ClientConfigCommand {

    private static final String MOD_ARG = "mod_id";

    private static final DynamicCommandExceptionType NO_CONFIG = new DynamicCommandExceptionType(
            modId -> Component.translatable("commands.moonlight.config.no_config", modId));

    public static <S> void register(CommandDispatcher<S> dispatcher) {
        LiteralCommandNode<S> node = dispatcher.register(
                LiteralArgumentBuilder.<S>literal(Moonlight.MOD_ID)
                        .then(configNode())
        );
        dispatcher.register(LiteralArgumentBuilder.<S>literal("mnl").redirect(node));
    }

    private static <S> LiteralArgumentBuilder<S> configNode() {
        return LiteralArgumentBuilder.<S>literal("config")
                .executes(ctx -> openScreen(new ModsTilesScreen(null, null)))
                .then(RequiredArgumentBuilder.<S, String>argument(MOD_ARG, StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                ModsTilesScreen.collectConfigurableMods(), builder))
                        .executes(ctx -> openModConfig(StringArgumentType.getString(ctx, MOD_ARG))));
    }

    private static int openModConfig(String modId) throws CommandSyntaxException {
        Screen screen = ModsTilesScreen.configScreenFor(modId, null, null);
        if (screen == null) throw NO_CONFIG.create(modId);
        return openScreen(screen);
    }

    private static int openScreen(Screen screen) {
        // deferred: the chat screen closes itself right after running the command, which would clobber a screen set here
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(screen));
        return 1;
    }
}
