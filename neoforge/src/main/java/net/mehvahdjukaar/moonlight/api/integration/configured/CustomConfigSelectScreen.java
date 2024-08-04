package net.mehvahdjukaar.moonlight.api.integration.configured;


import com.mojang.blaze3d.platform.Lighting;
import com.mrcrayfish.configured.api.ConfigType;
import com.mrcrayfish.configured.client.screen.ModConfigSelectionScreen;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.impl.neoforge.NeoForgeConfig;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.neoforge.ForgeConfigHolder;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CustomConfigSelectScreen extends ModConfigSelectionScreen {

    public static final ResourceLocation MISC_ICONS = Moonlight.res("textures/gui/misc_icons.png");

    private static final Field FILE_ITEM_BUTTON = CustomConfigScreen.findFieldOrNull(FileItem.class, "modifyButton");
    private static final Field FILE_ITEM_CONFIG = CustomConfigScreen.findFieldOrNull(FileItem.class, "config");

    private final BiFunction<CustomConfigSelectScreen, com.mrcrayfish.configured.api.IModConfig, CustomConfigScreen> configScreenFactory;
    private final ItemStack mainIcon;
    private final String modId;
    private final String modURL;

    public CustomConfigSelectScreen(String modId, ItemStack mainIcon, String displayName,
                                    Screen parent,
                                    BiFunction<CustomConfigSelectScreen, com.mrcrayfish.configured.api.IModConfig, CustomConfigScreen> configScreenFactory,
                                    ModConfigHolder... specs) {
        this(modId, mainIcon, displayName, parent, configScreenFactory, createConfigMap(specs));
    }

    public CustomConfigSelectScreen(String modId, ItemStack mainIcon, String displayName,
                                    Screen parent,
                                    BiFunction<CustomConfigSelectScreen, com.mrcrayfish.configured.api.IModConfig, CustomConfigScreen> configScreenFactory,
                                    Map<ConfigType, Set<com.mrcrayfish.configured.api.IModConfig>> configMap) {
        super(parent, Component.literal(displayName), configMap);
        this.configScreenFactory = configScreenFactory;
        this.mainIcon = mainIcon;
        this.modId = modId;
        ModContainer container = ModList.get().getModContainerById(modId).get();
        this.modURL = container.getModInfo().getModURL().map(URL::getPath).orElse(null);
    }

    public static ResourceLocation ensureNotNull(ResourceLocation background) {
        return background == null ? ResourceLocation.parse("minecraft:textures/gui/options_background.png") : background;
    }

    public ItemStack getMainIcon() {
        return mainIcon;
    }

    public String getModId() {
        return modId;
    }

    public static void registerConfigScreen(String modId, Function<Screen, CustomConfigSelectScreen> screenSelectFactory) {
        ModContainer container = ModList.get().getModContainerById(modId).get();
        container.registerExtensionPoint(IConfigScreenFactory.class, (a, s) -> screenSelectFactory.apply(s));
    }

    private static Map<ConfigType, Set<com.mrcrayfish.configured.api.IModConfig>> createConfigMap(ModConfigHolder... specs) {
        Map<ConfigType, Set<com.mrcrayfish.configured.api.IModConfig>> modConfigMap = new EnumMap<>(ConfigType.class);
        for (var ss : specs) {
            ForgeConfigHolder s = (ForgeConfigHolder) ss;
            ModConfig modConfig = s.getModConfig();
            var forgeConfig = new NeoForgeConfig(modConfig);
            var set = modConfigMap.computeIfAbsent(
                    forgeConfig.getType(), a -> new HashSet<>());
            set.add(forgeConfig);
        }
        return modConfigMap;
    }

    private static ConfigType getType(ForgeConfigHolder s) {
        var t = s.getConfigType();
        if (t == net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType.CLIENT) return ConfigType.CLIENT;
        if (t == net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType.COMMON) return ConfigType.UNIVERSAL;
        //else if(t == net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType.COMMON)return s.isSynced() ? ConfigType.SERVER_SYNC : ConfigType.SERVER;
        return ConfigType.UNIVERSAL;
    }

    @Override
    protected void constructEntries(List<Item> entries) {
        super.constructEntries(entries);

        for (Item i : entries) {
            if (i instanceof FileItem item) {
                try {
                    FILE_ITEM_BUTTON.setAccessible(true);
                    FILE_ITEM_CONFIG.setAccessible(true);
                    FILE_ITEM_BUTTON.set(i, createModifyButton((com.mrcrayfish.configured.api.IModConfig) FILE_ITEM_CONFIG.get(item)));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    private Button createModifyButton(com.mrcrayfish.configured.api.IModConfig config) {
        String langKey = "configured.gui.modify";
        return new IconButton(0, 0, 33, 0, 60, Component.translatable(langKey),
                (onPress) -> Minecraft.getInstance().setScreen(configScreenFactory.apply(CustomConfigSelectScreen.this,
                        config)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        Lighting.setupFor3DItems();
        int titleWidth = this.font.width(this.title) + 35;
        graphics.renderFakeItem(mainIcon, (this.width / 2) + titleWidth / 2 - 17, 2);
        graphics.renderFakeItem(mainIcon, (this.width / 2) - titleWidth / 2, 2);

        if (this.modURL != null && MthUtils.isWithinRectangle((this.width / 2) - 90, 2, 180, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, this.font.split(Component.translatable("gui.moonlight.open_mod_page", this.modId), 200), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.modURL != null && MthUtils.isWithinRectangle((this.width / 2) - 90, 2, 180, 16, (int) mouseX, (int) mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, this.modURL));
            this.handleComponentClicked(style);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

}
