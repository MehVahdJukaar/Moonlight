package net.mehvahdjukaar.moonlight.core.client.config;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.Icon;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;
import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

// sticks to last hovered row after mouse leaves list
public class DescriptionPanel extends AbstractWidget {

    public record Content(Component title, @Nullable ResourceLocation image, @Nullable Component description,
                          List<GutterHints.Hint> hints) {
    }

    //only needed to cache the image size
    private static final Map<ResourceLocation, Optional<Icon>> IMAGES = new HashMap<>();

    private final Font font;
    private final ConfigRowList list;
    @Nullable
    private ConfigRow shownRow;
    @Nullable
    private Content shown;
    private double scroll;
    private int maxScroll;

    public DescriptionPanel(Font font, ConfigRowList list, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.list = list;
    }

    public void show(Content content) {
        this.shown = content;
        this.shownRow = null;
        scroll = 0;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ConfigRow hovered = list.getHovered(mouseX, mouseY);
        Content content = (hovered == null) ? null : hovered.panelContent();
        if (content != null) {
            if (hovered != shownRow) scroll = 0;
            shownRow = hovered;
            shown = content;
        }
        int bottom = getBottom();
        GuiHelper.renderListBackground(graphics, getX(), getY(), bottom, width, 0);
        GuiHelper.renderVerticalSeparator(graphics, getX(), getY(), bottom);
        GuiHelper.renderFooterSeparator(graphics, getX(), bottom, width);
        if (shown == null) {
            return;
        }

        int textX = getX() + PANE_PAD;
        int textWidth = width - 2 * PANE_PAD;
        int y = getY() + PANE_PAD - (int) scroll;

        graphics.enableScissor(getX(), getY(), getRight(), bottom);
        y = drawLines(graphics, font.split(shown.title(), textWidth), textX, y, TEXT);

        Icon image = (shown.image() == null) ? null : getImage(shown.image());
        if (image != null) {
            int imageHeight = Math.round(textWidth * image.height() / (float) image.width());
            GuiHelper.renderModIcon(graphics, image, textX, y + GAP, textWidth, imageHeight);
            y += GAP + imageHeight;
        }
        if (shown.description() != null) {
            y = drawLines(graphics, font.split(shown.description(), textWidth), textX, y + GAP, DESCRIPTION);
        }
        for (GutterHints.Hint hint : shown.hints()) {
            y += GAP;
            graphics.blitSprite(hint.icon(), textX, y, 8, 8);
            y = drawLines(graphics, font.split(hint.text(), textWidth - 11), textX + 11, y, TEXT);
        }
        graphics.disableScissor();

        int contentHeight = y + (int) scroll - getY() + PANE_PAD;
        maxScroll = Math.max(0, contentHeight - height);
        scroll = Math.min(scroll, maxScroll);

        GuiHelper.renderScrollbar(graphics, getY(), bottom, getRight(), scroll, maxScroll);
    }

    @Nullable
    public static Icon getImage(ResourceLocation texture) {
        return IMAGES.computeIfAbsent(texture, t -> {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(t);

            if (resource.isEmpty()) {
                Moonlight.LOGGER.warn("Missing config image {}", t);
                return Optional.empty();
            }
            try (InputStream stream = resource.get().open(); NativeImage img = NativeImage.read(stream)) {
                return Optional.of(new Icon(t, img.getWidth(), img.getHeight()));
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to read config image {}", t, e);
                return Optional.empty();
            }
        }).orElse(null);
    }


    private int drawLines(GuiGraphics graphics, List<FormattedCharSequence> lines, int x, int y, int color) {
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, x, y, color);
            y += font.lineHeight;
        }
        return y;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll == 0) return false;
        scroll = Mth.clamp(scroll - scrollY * 2 * font.lineHeight, 0, maxScroll);
        return true;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
