package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BlockStateStatsCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext dispatcher) {
        return Commands.literal("blockstate_stats")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_OWNERS))
                .executes(new BlockStateStatsCommand());
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        Registry<Block> registry = BuiltInRegistries.BLOCK;

        // Prepare statistics
        Map<String, Integer> modBlockCounts = new HashMap<>();
        Map<String, Integer> modBlockStateCounts = new HashMap<>();
        List<Block> blocksList = new ArrayList<>();
        int totalBlocks = 0;
        int totalBlockStates = 0;

        for (Block block : registry) {
            totalBlocks++;
            blocksList.add(block);

            // Get mod ID
            String modId = block.builtInRegistryHolder().key().location().getNamespace();

            // Count blocks per mod
            modBlockCounts.put(modId, modBlockCounts.getOrDefault(modId, 0) + 1);

            // Count blockstates per mod
            int blockStateCount = block.getStateDefinition().getPossibleStates().size();
            totalBlockStates += blockStateCount;
            modBlockStateCounts.put(modId, modBlockStateCounts.getOrDefault(modId, 0) + blockStateCount);
        }

        double averageBlockStates = totalBlockStates / (double) totalBlocks;

        // Optional: save detailed info to a file
        Path outputPath = PlatHelper.getGamePath().resolve("blockstate_stats.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            // --- Basic Info ---
            writer.write("=== Minecraft Blockstate Statistics ===\n");
            writer.write(String.format("Total blocks: %d%n", totalBlocks));
            writer.write(String.format("Total blockstates: %d%n", totalBlockStates));
            writer.write(String.format("Average blockstates per block: %.2f%n%n", averageBlockStates));

            // --- Blocks per mod (sorted) ---
            writer.write("--- Blocks per Mod ---\n");
            modBlockCounts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // descending
                    .forEach(entry -> {
                        try {
                            String mod = entry.getKey();
                            writer.write(String.format("%s: %d blocks, %d blockstates%n",
                                    mod, modBlockCounts.get(mod), modBlockStateCounts.get(mod)));
                        } catch (IOException ignored) {
                        }
                    });

            writer.write("\n");

            // --- Blocks with above-average blockstates (detailed) ---
            writer.write("--- Blocks with More Than Average Blockstates ---\n");
            blocksList.stream()
                    .map(block -> new AbstractMap.SimpleEntry<>(block, block.getStateDefinition().getPossibleStates().size()))
                    .filter(entry -> entry.getValue() > averageBlockStates)
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) // descending
                    .forEach(entry -> {
                        try {
                            String blockId = BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString();
                            int stateCount = entry.getValue();
                            writer.write(String.format("%s: %d blockstates%n", blockId, stateCount));
                        } catch (IOException ignored) {
                        }
                    });

        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to write blockstate statistics to file: {}", e.getMessage());
        }


        // Send summary to player
        String totalBlockStateString = "" + totalBlockStates;
        String totalBlocksString = "" + totalBlocks;

        context.getSource().sendSuccess(() -> {

            // Append clickable file path
            MutableComponent clickablePath = Component.literal(" [" + outputPath.toString() + "]")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.AQUA) // optional color
                    );
            // Click action not allow on dedicated servers as client cannot click link to a server's file path.
            if (PlatHelper.isIntegratedServer()) {
                clickablePath.withStyle((style) -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, outputPath.toString())));
            }


            // Base message with totals
            MutableComponent message = Component.translatable(
                    "commands.moonlight.blockstate_stats",
                    totalBlocksString,
                    totalBlockStateString,
                    clickablePath
            );


            return message.append(clickablePath);
        }, false);

        return 0;
    }
}