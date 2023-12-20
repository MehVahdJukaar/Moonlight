package net.mehvahdjukaar.moonlight.api.platform.configs.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Main config screen
 */
public class FabricConfigListScreen extends Screen {

    protected final Screen parent;
    protected final ConfigSpec[] configs;
    protected final ResourceLocation background;
    private final ItemStack mainIcon;
    private final String modId;
    private final String modURL;

    protected ConfigList list;

    public FabricConfigListScreen(String modId, ItemStack mainIcon, Component displayName, @Nullable ResourceLocation background,
                                  Screen parent,
                                  ConfigSpec... specs) {
        super(displayName);
        this.parent = parent;
        this.configs = specs;
        this.background = background;
        this.mainIcon = mainIcon;
        this.modId = modId;
        this.modURL = FabricLoader.getInstance().getModContainer(modId).get().getMetadata().getContact().get("homepage").orElse(null);
    }

    @Override
    protected void init() {
        this.list = new ConfigList(this.minecraft, this.width, this.height, 32, 40,
                this.configs);
        this.addRenderableWidget(this.list);

        this.addExtraButtons();
    }

    protected void addExtraButtons() {
        this.addRenderableWidget(Button.builder(
                        CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
    }

    @Override
    public void removed() {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);

        if (modURL != null && isMouseWithin((this.width / 2) - 90, 2 + 6, 180, 16 + 2, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, this.font.split(Component.translatable("gui.moonlight.open_mod_page", this.modId), 200), mouseX, mouseY);
        }
        int titleWidth = this.font.width(this.title) + 35;
        graphics.renderFakeItem(this.mainIcon, (this.width / 2) + titleWidth / 2 - 17, 2 + 8);
        graphics.renderFakeItem(this.mainIcon, (this.width / 2) - titleWidth / 2, 2 + 8);
    }

    private boolean isMouseWithin(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (modURL != null && isMouseWithin((this.width / 2) - 90, 2 + 6, 180, 16 + 2, (int) mouseX, (int) mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, modURL));
            this.handleComponentClicked(style);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    protected class ConfigList extends ContainerObjectSelectionList<ConfigButton> {

        public ConfigList(Minecraft minecraft, int width, int height, int y0, int itemHeight, ConfigSpec... specs) {
            super(minecraft, width, height, y0,  itemHeight);
            this.centerListVertically = true;
            for (var s : specs) {
                this.addEntry(new ConfigButton(s, this.width, this.getRowWidth()));
            }
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 32;
        }

        /*
        @Override
        protected int getRowTop(int index) {
            if (!this.centerListVertically) return super.getRowTop(index);
            return (y1 - y0) / 2 - (this.children().size() * itemHeight) / 2 +
                    this.y0 + 4 - (int) this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
        }*/

        //TODO: readd
        /*
        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(graphics);

            var background = FabricConfigListScreen.this.background;

            int i = this.getScrollbarPosition();
            int j = i + 6;
            //this.hovered = this.isMouseOver((double)mouseX, (double)mouseY) ? this.getEntryAtPosition((double)mouseX, (double)mouseY) : null;
            if (true) {
                RenderSystem.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
                graphics.blit(background, this.x0, this.y0, this.x1, (this.y1 + (int) this.getScrollAmount()), this.x1 - this.x0, this.y1 - this.y0, 32, 32);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }

            int k = this.getRowLeft();
            int l = this.y0 + 4 - (int) this.getScrollAmount();
            this.enableScissor(graphics);
            if (true) {
                this.renderHeader(graphics, k, l);
            }

            this.renderList(graphics, mouseX, mouseY, partialTick);
            graphics.disableScissor();
            if (true) {
                RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
                graphics.blit(background, this.x0, 0, 0.0F, 0.0F, this.width, this.y0, 32, 32);
                graphics.blit(background, this.x0, this.y1, 0.0F, this.y1, this.width, this.height - this.y1, 32, 32);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.fillGradient(this.x0, this.y0, this.x1, this.y0 + 4, -16777216, 0);
                graphics.fillGradient(this.x0, this.y1 - 4, this.x1, this.y1, 0, -16777216);
            }

            int m = this.getMaxScroll();
            if (m > 0) {
                int n = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
                n = Mth.clamp(n, 32, this.y1 - this.y0 - 8);
                int o = (int) this.getScrollAmount() * (this.y1 - this.y0 - n) / m + this.y0;
                if (o < this.y0) {
                    o = this.y0;
                }

                graphics.fill(i, this.y0, j, this.y1, -16777216);
                graphics.fill(i, o, j, o + n, -8355712);
                graphics.fill(i, o, j - 1, o + n - 1, -4144960);
            }

            this.renderDecorations(graphics, mouseX, mouseY);
            RenderSystem.disableBlend();
        }*/
    }

    protected class ConfigButton extends ContainerObjectSelectionList.Entry<ConfigButton> {

        private final List<AbstractWidget> children;

        private ConfigButton(AbstractWidget widget) {
            this.children = List.of(widget);
        }

        protected ConfigButton(ConfigSpec spec, int width, int buttonWidth) {
            this(Button.builder(Component.literal(spec.getFileName()), b ->
                    Minecraft.getInstance().setScreen(spec.makeScreen(FabricConfigListScreen.this, FabricConfigListScreen.this.background))
            ).bounds(width / 2 - buttonWidth / 2, 0, buttonWidth, 20).build());
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            this.children.forEach((button) -> {
                button.setY(top);
                button.render(graphics, mouseX, mouseY, partialTick);
            });
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }

}