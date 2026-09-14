package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SearchBoxWidget extends EditBox {

    public static final int WIDTH = 110;
    public static final int HEIGHT = 14;
    private static final int ICON = 12;
    public static final int ICON_SPACE = ICON + 2;

    public SearchBoxWidget(Font font, int x, int y, String query, Consumer<String> onQuery) {
        super(font, x, y, WIDTH, HEIGHT, Component.translatable("gui.moonlight.config.search"));
        setHint(getMessage().copy().withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        setValue(query);
        setResponder(onQuery);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MoonlightIcons.SEARCH, getX() - ICON_SPACE, getY() + (HEIGHT - ICON) / 2, ICON, ICON);
    }
}
