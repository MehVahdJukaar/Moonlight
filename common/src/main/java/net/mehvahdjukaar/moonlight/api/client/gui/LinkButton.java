package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TextAndImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.w3c.dom.Text;

import java.util.Calendar;

public class LinkButton {

    public static final ResourceLocation MISC_ICONS = Moonlight.res("textures/gui/misc_icons.png");

    public static TextAndImageButton create(
                                    Screen parent, int x, int y, int uInd, int vInd, String url, String tooltip) {
        return create(MISC_ICONS, 64, 64, 14, 14, parent, x, y, uInd, vInd, url, tooltip);
    }

    public static TextAndImageButton create(ResourceLocation texture, int textureW, int textureH, int iconW, int iconH,
                              Screen parent, int x, int y, int uInd, int vInd, String url, String tooltip) {

        String finalUrl = getLink(url);
        Button.OnPress onPress = (op) -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl));
            parent.handleComponentClicked(style);
        };

        var button = TextAndImageButton.builder(CommonComponents.EMPTY, texture, onPress)
                .textureSize(iconW, iconH)
                .usedTextureSize(textureW, textureH)
                .texStart(uInd * iconW, vInd * iconH)
                .offset(x,y)
                .build();

        button.setTooltip(Tooltip.create(Component.literal(tooltip)));
        return button;
    }


    /*
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int contentWidth = iconW + mc.font.width(this.label);
        int iconX = (int) (this.getX() + Math.ceil((this.width - contentWidth) / 2f));
        int iconY = (int) (this.getY() + Math.ceil((this.width - iconH) / 2f));
        float brightness = this.active ? 1.0F : 0.5F;
        RenderSystem.setShaderColor(brightness, brightness, brightness, this.alpha);
        blit(poseStack, iconX, iconY, this.getBlitOffset(), this.u, this.v, iconW, iconW, textureH, textureW);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int textColor = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
        drawString(poseStack, mc.font, this.label, iconX + 14, iconY + 1, textColor);
    }*/

    private static String getLink(String original) {
        return LOL ? "https://www.youtube.com/watch?v=dQw4w9WgXcQ" : original;
    }

    private static final boolean LOL;

    static {
        Calendar calendar = Calendar.getInstance();
        LOL = calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DATE) == 1;
    }
}