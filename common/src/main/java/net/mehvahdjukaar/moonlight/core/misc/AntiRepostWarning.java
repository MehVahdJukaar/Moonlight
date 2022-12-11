package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose of this is to throw a warning when jars with weird file names are detected, usually signaling being downloaded from 9Minecraft and alike
 */
public class AntiRepostWarning {

    private static final Set<String> MODS = new HashSet<>();

    public static void addMod(String id) {
        if(!Objects.equals(id, "minecraft")) {
            MODS.add(id);
        }
    }

    public static void run() {
        if (PlatformHelper.isDev()) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Set<String> reposted = MODS.stream().filter(AntiRepostWarning::isFileNameSus).collect(Collectors.toSet());

        try {
            for (var m : reposted) {
                String url = PlatformHelper.getModPageUrl(m);
                if (url != null) {
                    MutableComponent link = Component.translatable("message.moonlight.anti_repost_link");
                    String modName = PlatformHelper.getModName(m);
                    MutableComponent name = Component.literal(modName).withStyle(ChatFormatting.BOLD);

                    ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
                    link.setStyle(link.getStyle().withClickEvent(click).withUnderlined(true)
                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.GOLD)));

                    player.displayClientMessage(Component.translatable("message.moonlight.anti_repost",name, link), false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean isFileNameSus(String mod) {
        //Take that 9Minecraft
        String fileName = PlatformHelper.getModFilePath(mod).getFileName().toString();
        if (fileName.contains(".jar")) {
            return fileName.contains("-Mod-") || fileName.endsWith("-tw");
        }
        return false;
    }


}
