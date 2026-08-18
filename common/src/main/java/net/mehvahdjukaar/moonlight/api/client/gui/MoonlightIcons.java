package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;

public final class MoonlightIcons {

    public static final ResourceLocation YES = Moonlight.res("yes");
    public static final ResourceLocation NO = Moonlight.res("no");
    public static final ResourceLocation SAVE = Moonlight.res("save");
    public static final ResourceLocation RESET = Moonlight.res("reset");
    public static final ResourceLocation DELETE = Moonlight.res("delete");
    public static final ResourceLocation EDIT = Moonlight.res("edit");
    public static final ResourceLocation SEARCH = Moonlight.res("search");
    public static final ResourceLocation FOLDER = Moonlight.res("folder");
    public static final ResourceLocation HEART = Moonlight.res("heart");
    public static final ResourceLocation DISCOVER_MODS = Moonlight.res("discover_mods");

    // the gear, plus the paper sheets telling client, synced and common configs apart
    public static final ResourceLocation CONFIG = Moonlight.res("config");
    public static final ResourceLocation CONFIG_CLIENT = Moonlight.res("config_client");
    public static final ResourceLocation CONFIG_SERVER = Moonlight.res("config_server");
    public static final ResourceLocation CONFIG_COMMON = Moonlight.res("config_common");

    public static final ResourceLocation WORLD_RELOAD = Moonlight.res("world_reload");
    public static final ResourceLocation GAME_RESTART = Moonlight.res("game_restart");

    public static final ResourceLocation SECTION_COLLAPSED = Moonlight.res("widget/section_collapsed");
    public static final ResourceLocation SECTION_EXPANDED = Moonlight.res("widget/section_expanded");
    public static final ResourceLocation CHAT_BUBBLE_BODY = Moonlight.res("widget/chat_bubble_body");
    public static final ResourceLocation CHAT_BUBBLE_TAIL = Moonlight.res("widget/chat_bubble_tail");
}
