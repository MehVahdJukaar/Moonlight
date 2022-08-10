package net.mehvahdjukaar.moonlight.api.integration.configureed;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.screen.ModConfigSelectionScreen;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import com.mrcrayfish.configured.util.ConfigHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.forge.ConfigSpecWrapper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;

public class CustomConfigSelectScreen extends ModConfigSelectionScreen {

    public static final ResourceLocation MISC_ICONS = Moonlight.res("textures/gui/misc_icons.png");

    private static final Field FILE_ITEM_BUTTON = CustomConfigScreen.findFieldOrNull(FileItem.class, "modifyButton");
    private static final Field FILE_ITEM_CONFIG = CustomConfigScreen.findFieldOrNull(FileItem.class, "config");

    private final BiFunction<CustomConfigSelectScreen, ModConfig, CustomConfigScreen> configScreenFactory;
    private final ItemStack mainIcon;
    private final String modId;
    private final String modURL;


    public CustomConfigSelectScreen(String modId, ItemStack mainIcon, String displayName, ResourceLocation background,
                                    Screen parent,
                                    BiFunction<CustomConfigSelectScreen, ModConfig, CustomConfigScreen> configScreenFactory,
                                    ConfigSpec... specs) {
        this(modId, mainIcon, displayName, background, parent, configScreenFactory, createConfigMap(specs));
    }

    public CustomConfigSelectScreen(String modId, ItemStack mainIcon, String displayName, ResourceLocation background,
                                    Screen parent,
                                    BiFunction<CustomConfigSelectScreen, ModConfig, CustomConfigScreen> configScreenFactory,
                                    Map<ModConfig.Type, Set<ModConfig>> configMap) {
        super(parent, displayName, background, configMap);
        this.configScreenFactory = configScreenFactory;
        this.mainIcon = mainIcon;
        this.modId = modId;
        ModContainer container = ModList.get().getModContainerById(modId).get();
        this.modURL = container.getModInfo().getModURL().map(URL::getPath).orElse(null);
    }

    /**
     * Registers this custom config screen
     */
    public static void registerConfigScreenScreen(String modId, BiFunction<Minecraft, Screen, Screen> screenSelectFactory) {
        ModContainer container = ModList.get().getModContainerById(modId).get();
        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory(screenSelectFactory));
    }

    private static Map<ModConfig.Type, Set<ModConfig>> createConfigMap(ConfigSpec... specs) {
        Map<ModConfig.Type, Set<ModConfig>> modConfigMap = new HashMap<>();
        for (var s : specs) {
            var set = modConfigMap.computeIfAbsent(((ConfigSpecWrapper) s).getModConfigType(), a -> new HashSet<>());
            set.add(((ConfigSpecWrapper) s).getModConfig());
        }
        return modConfigMap;
    }

    @Override
    protected void constructEntries(List<Item> entries) {
        super.constructEntries(entries);

        for (Item i : entries) {
            if (i instanceof FileItem item) {
                try {
                    FILE_ITEM_BUTTON.setAccessible(true);
                    FILE_ITEM_CONFIG.setAccessible(true);
                    FILE_ITEM_BUTTON.set(i, createModifyButton((ModConfig) FILE_ITEM_CONFIG.get(item)));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    private Button createModifyButton(ModConfig config) {
        String langKey = "configured.gui.modify";
        return new IconButton(0, 0, 33, 0, 60, Component.translatable(langKey),
                (onPress) -> Minecraft.getInstance().setScreen(configScreenFactory.apply(CustomConfigSelectScreen.this, config)),
                (button, matrixStack, mouseX, mouseY) -> {
                    if (button.isHoveredOrFocused()) {
                        if (ConfigScreen.isPlayingGame() && !ConfigHelper.isConfiguredInstalledOnServer()) {
                            CustomConfigSelectScreen.this.renderTooltip(matrixStack, this.font.split(Component.translatable("configured.gui.not_installed"),
                                    Math.max(CustomConfigSelectScreen.this.width / 2 - 43, 170)), mouseX, mouseY);
                        }
                    }
                });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        int titleWidth = this.font.width(this.title) + 35;
        this.itemRenderer.renderAndDecorateFakeItem(mainIcon, (this.width / 2) + titleWidth / 2 - 17, 2);
        this.itemRenderer.renderAndDecorateFakeItem(mainIcon, (this.width / 2) - titleWidth / 2, 2);

        if (this.modURL != null && ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, mouseX, mouseY)) {
            this.renderTooltip(poseStack, this.font.split(Component.translatable("gui.moonlight.configured", this.modId), 200), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.modURL != null && ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, (int) mouseX, (int) mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, this.modURL));
            this.handleComponentClicked(style);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    protected void init() {
        super.init();
        Button found = null;
        for (var c : this.children()) {
            if (c instanceof Button button) {
                if (button.getWidth() == 150) found = button;
            }
        }
        if (found != null) this.removeWidget(found);

        int y = this.height - 29;
        int centerX = this.width / 2;

        this.addRenderableWidget(new Button(centerX - 45, y, 90, 20, CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.parent)));


        /*
        LinkButton patreon = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22, y, 3, 1,
                "https://www.patreon.com/user?u=53696377", "Support me on Patreon :D");

        LinkButton kofi = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22 * 2, y, 2, 2,
                "https://ko-fi.com/mehvahdjukaar", "Donate a Coffe");

        LinkButton curseforge = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22 * 3, y, 1, 2,
                "https://www.curseforge.com/minecraft/mc-mods/supplementaries", "CurseForge Page");

        LinkButton github = LinkButton.create(ICONS_TEXTURES, this, centerX - 45 - 22 * 4, y, 0, 2,
                "https://github.com/MehVahdJukaar/Supplementaries/wiki", "Mod Wiki");


        LinkButton discord = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2, y, 1, 1,
                "https://discord.com/invite/qdKRTDf8Cv", "Mod Discord");

        LinkButton youtube = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2 + 22, y, 0, 1,
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s", "Youtube Channel");

        LinkButton twitter = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2 + 22 * 2, y, 2, 1,
                "https://twitter.com/Supplementariez?s=09", "Twitter Page");

        LinkButton akliz = LinkButton.create(ICONS_TEXTURES, this, centerX + 45 + 2 + 22 * 3, y, 3, 2,
                "https://www.akliz.net/supplementaries", "Need a server? Get one with Akliz");


        this.addRenderableWidget(kofi);
        this.addRenderableWidget(akliz);
        this.addRenderableWidget(patreon);
        this.addRenderableWidget(curseforge);
        this.addRenderableWidget(discord);
        this.addRenderableWidget(youtube);
        this.addRenderableWidget(github);
        this.addRenderableWidget(twitter);

         */
    }

}
