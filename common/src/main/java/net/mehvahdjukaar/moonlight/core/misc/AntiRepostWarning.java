package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AntiRepostWarning {

    public static void run() {
        if (PlatHelper.isDev()) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Set<String> reposted = Moonlight.getDependents().stream().filter(AntiRepostWarning::isFileNameSus).collect(Collectors.toSet());

        try {
            for (var m : reposted) {
                String url = PlatHelper.getModPageUrl(m);
                if (url == null) url ="https://curseforge.com/minecraft/mc-mods";
                MutableComponent link = Component.translatable("message.moonlight.anti_repost_link");
                String modName = PlatHelper.getModName(m);
                MutableComponent name = Component.literal(modName).withStyle(ChatFormatting.BOLD);

                ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
                link.setStyle(link.getStyle().withClickEvent(click).withUnderlined(true)
                        .withColor(TextColor.fromLegacyFormat(ChatFormatting.GOLD)));

                player.displayClientMessage(Component.translatable("message.moonlight.anti_repost", name, link), false);

            }
        } catch (Exception ignored) {
        }
    }

    private static boolean isFileNameSus(String mod) {
        var path = PlatHelper.getModFilePath(mod);
        if (path == null || path.getFileName() == null) {
            Moonlight.LOGGER.warn("Failed to get file path of mod {}: {}", mod, path);
        } else {
            String fileName = path.getFileName().toString();
            if (fileName.contains(".jar")) {
                return fileName.contains("-Mod-") || fileName.endsWith("-tw");
            }
        }
        return false;
    }


}
