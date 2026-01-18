package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void init() {
        RegHelper.addCommandRegistration(ModCommands::register);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {

        var node = dispatcher.register(
                Commands.literal(Moonlight.MOD_ID)
                        .then(RegistryCommand.register())
                        .then(BlockStateStatsCommand.register(context))
                        .then(IUsedToRollTheDice.register(context))
                        .then(DebugRenderersCommand.register(context))
                        .then(RandomTeleportCommand.register(context))
                        .then(ChangeDimensionCommand.register(context))
                        .then(BackCommand.register(context))
                        .then(MapMarkerCommand.register(context))
        );

        dispatcher.register(Commands.literal("mnl").redirect(node));
    }
}
