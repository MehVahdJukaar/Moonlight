package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.IconButton;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton;
import net.mehvahdjukaar.moonlight.api.client.gui.ModIcons;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

public class MoonlightConfigSelectScreen extends Screen {

    private final String modId;
    private final Screen parent;
    @Nullable
    private final ResourceLocation background;
    private final List<ModConfigHolder> holders;

    private ConfigOptionList list;

    public MoonlightConfigSelectScreen(String modId, Screen parent, @Nullable ResourceLocation background) {
        super(Component.literal(LangBuilder.getReadableName(modId)));
        this.modId = modId;
        this.parent = parent;
        this.background = background;
        this.holders = configsOf(modId);
    }

    /** All configs registered for a mod that can show a screen, ordered common → client. */
    private static List<ModConfigHolder> configsOf(String modId) {
        return ModConfigHolder.getTrackedSpecs().stream()
                .filter(h -> h.getModId().equals(modId))
                .sorted(Comparator.comparingInt(h -> h.getConfigType().ordinal()))
                .toList();
    }

    /**
     * Builds the right screen for a mod's config button: the single config directly if there's only one, the
     * select list if there are several, or null if the mod has none.
     */
    @Nullable
    public static Screen create(String modId, Screen parent, @Nullable ResourceLocation background) {
        List<ModConfigHolder> holders = configsOf(modId);
        if (holders.isEmpty()) return null;
        if (holders.size() == 1) return holders.getFirst().makeScreen(parent, background);
        return new MoonlightConfigSelectScreen(modId, parent, background);
    }

    @Override
    protected void init() {
        this.list = new ConfigOptionList(this.minecraft, this.width, this.height - HEADER - FOOTER, HEADER, SELECT_ITEM_HEIGHT);
        List<ConfigListRow> rows = new ArrayList<>();
        for (ModConfigHolder h : holders) {
            Component label = Component.literal(LangBuilder.getReadableName(h.getId().getPath()));
            Component tooltip = Component.literal(h.getFileName());
            rows.add(new ConfigHolderRow(label, tooltip, configFileIcon(h.getConfigType()),
                    () -> this.minecraft.setScreen(h.makeScreen(this, background))));
        }
        this.list.setRows(rows);
        this.addRenderableWidget(this.list);

        // bottom bar: Back flanked by the author's media links (matches the Configured integration screen)
        MediaButton.addAuthorMediaButtons(this, this::addRenderableWidget,
                this.width / 2, this.height - 28, 22, modId, this::onClose);
        // bottom-left: icon-only jump to the mods hub grid
        IconButton modsButton = new IconButton(8, this.height - 28, 20, 20, Component.empty(), CONFIG_ICON, 16, 16,
                b -> this.minecraft.setScreen(new ModsTilesScreen(this, background))).borderless();
        modsButton.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.mods_button")));
        this.addRenderableWidget(modsButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        // header chrome in the background layer, behind the widgets (the list draws only its footer separator)
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.width, HEADER);

        // the mod's own icon, tucked just left of the centered title
        ModIcons.Icon icon = ModIcons.get(modId);
        if (icon != null) {
            int size = 16;
            int iconX = this.width / 2 - this.font.width(this.title) / 2 - size - 4;
            int iconY = (HEADER - size) / 2;
            graphics.blit(icon.texture(), iconX, iconY, size, size, 0f, 0f, icon.width(), icon.height(), icon.width(), icon.height());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        // row tooltip on top of everything
        ConfigListRow hovered = this.list.getHovered(mouseX, mouseY);
        if (hovered != null) {
            Component tooltip = hovered.getTooltip(mouseX, mouseY);
            if (tooltip != null) {
                graphics.renderTooltip(this.font, this.font.split(tooltip, 220), mouseX, mouseY);
            }
        }
    }
}
