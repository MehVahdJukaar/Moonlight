package net.mehvahdjukaar.moonlight.api.integration.configureed;


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

//credits to MrCrayfish's Configured Mod
public abstract class CustomConfigScreen extends ConfigScreen {

    @Nullable
    private static final Field BUTTON_ON_PRESS = findFieldOrNull(Button.class, "onPress");
    @Nullable
    private static final Field FOLDER_ENTRY = findFieldOrNull(ConfigScreen.class, "folderEntry");
    @Nullable
    private static final Method SAVE_CONFIG = findMethodOrNull(ConfigScreen.class, "saveConfig");
    @Nullable
    private static final Field CONFIG_VALUE_HOLDER = findFieldOrNull(ConfigItem.class, "holder");
    @Nullable
    private static final Field BOOLEAN_ITEM_BUTTON = findFieldOrNull(BooleanItem.class, "button");

    private final ResourceLocation background;
    private final String modId;

    private final Map<String, ItemStack> icons = new HashMap<>();
    public final ItemStack mainIcon;


    @Nullable
    static Method findMethodOrNull(Class<?> c, String methodName) {
        Method field = null;
        try {
            field = ObfuscationReflectionHelper.findMethod(c, methodName);
        } catch (Exception ignored) {
        }
        return field;
    }

    @Nullable
    static Field findFieldOrNull(Class<?> c, String fieldName) {
        Field field = null;
        try {
            field = ObfuscationReflectionHelper.findField(c, fieldName);
        } catch (Exception ignored) {
        }
        return field;
    }

    private CustomConfigScreen(String modId, ItemStack mainIcon, ResourceLocation background, Component title,
                               Screen parent, ModConfig config,
                               FolderEntry folderEntry) {
        this(modId, mainIcon, background, title, parent, config);
        //hax
        try {
            FOLDER_ENTRY.set(this, folderEntry);
        } catch (Exception ignored) {
        }
    }


    //needed for custom title
    public CustomConfigScreen(String modId, ItemStack mainIcon, ResourceLocation background, Component title,
                              Screen parent, ModConfig config) {
        super(parent, title, config, background);
        this.background = background;
        this.modId = modId;
        this.mainIcon = mainIcon;
    }

    private ItemStack getIcon(String name) {
        if (!icons.containsKey(name)) {
            String formatted = name.toLowerCase(Locale.ROOT).replace(" ", "_");
            var item = Registry.ITEM.getOptional(new ResourceLocation(modId, formatted));
            item.ifPresent(value -> addIcon(name, value.asItem().getDefaultInstance()));
        }
        return icons.getOrDefault(name, mainIcon);
    }

    private void addIcon(String s, ItemStack i) {
        icons.put(s, i);
    }

    @Override
    protected void init() {
        super.init();

        //replace list with new custom entries
        boolean reg = this.hasFancyBooleans() && !this.folderEntry.isRoot();

        this.list.replaceEntries(replaceItems(this.list.children(), reg));
        Collection<Item> temp = replaceItems(this.entries, reg);
        this.entries = new ArrayList<>(temp);

        //overrides save button
        if (this.saveButton != null && SAVE_CONFIG != null && BUTTON_ON_PRESS != null) {
            try {
                Button.OnPress press = this::saveButtonAction;
                BUTTON_ON_PRESS.set(this.saveButton, press);
            } catch (Exception ignored) {
            }
        }
    }

    public abstract boolean hasFancyBooleans();

    private Collection<Item> replaceItems(Collection<Item> originals, boolean fancyBooleans) {
        ArrayList<Item> newList = new ArrayList<>();
        for (Item c : originals) {
            if (c instanceof FolderItem f) {
                FolderWrapper wrapper = wrapFolderItem(f);
                if (wrapper != null) {
                    newList.add(wrapper);
                    continue;
                }
            } else if (c instanceof BooleanItem b) {
                BooleanWrapper wrapper = wrapBooleanItem(b, fancyBooleans);
                if (wrapper != null) {
                    newList.add(wrapper);
                    continue;
                }
            }
            newList.add(c);
        }
        return newList;
    }

    //sync configs to server when saving
    private void saveButtonAction(Button button) {
        if (this.config != null) {
            try {
                SAVE_CONFIG.invoke(this);
            } catch (Exception ignored) {
            }

            if (this.isChanged(this.folderEntry)) {
                this.onSave();
            }
        }
        this.minecraft.setScreen(this.parent);
    }

    public abstract void onSave();

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, mouseX, mouseY)) {
            this.renderTooltip(matrixStack, this.font.split(Component.translatable("supplementaries.gui.info"), 200), mouseX, mouseY);
        }
        int titleWidth = this.font.width(this.title) + 35;
        this.itemRenderer.renderAndDecorateFakeItem(mainIcon, (this.width / 2) + titleWidth / 2 - 17, 2);
        this.itemRenderer.renderAndDecorateFakeItem(mainIcon, (this.width / 2) - titleWidth / 2, 2);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ScreenUtil.isMouseWithin((this.width / 2) - 90, 2, 180, 16, (int) mouseX, (int) mouseY)) {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/supplementaries"));
            this.handleComponentClicked(style);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    private int ticks = 0;

    @Override
    public void tick() {
        super.tick();
        ticks++;
    }

    @Nullable
    public FolderWrapper wrapFolderItem(FolderItem old) {
        final FolderEntry folderEntry = CustomConfigScreen.this.folderEntry;

        try {
            String oldName = old.getLabel();
            //find correct folder
            FolderEntry found = null;
            for (IEntry e : folderEntry.getEntries()) {
                if (e instanceof FolderEntry f) {
                    String n = Component.literal(ConfigScreen.createLabel(f.getLabel())).getString();
                    if (n.equals(oldName)) {
                        found = f;
                        break;
                    }
                }
            }
            if (found != null) {
                return new FolderWrapper(found, oldName);
            }
        } catch (Exception ignored) {
            int aa = 1;
        }
        return null;
    }

    public abstract CustomConfigScreen createSubScreen(Component title);

    private class FolderWrapper extends FolderItem {

        private final ItemStack icon;
        protected final Button button;

        private FolderWrapper(FolderEntry folderEntry, String label) {
            super(folderEntry);
            //make new button I can access
            this.button = new Button(10, 5, 44, 20, (Component.literal(label)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE), (onPress) -> {
                Component newTitle = CustomConfigScreen.this.title.plainCopy().append(" > " + label);
                CustomConfigScreen.this.minecraft.setScreen(createSubScreen(title));
            });
            this.icon = getIcon(label.toLowerCase(Locale.ROOT));
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.button);
        }

        private int ticks = 0;
        private int lastTick = 1;

        @Override
        public void render(PoseStack matrixStack, int x, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean selected, float partialTicks) {


            if (lastTick < CustomConfigScreen.this.ticks) {
                ticks = Math.max(0, ticks + (selected ? 1 : -2)) % (36);
            }

            this.lastTick = CustomConfigScreen.this.ticks;


            this.button.x = left - 1;
            this.button.y = top;
            this.button.setWidth(width);
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            int center = this.button.x + width / 2;

            ItemRenderer renderer = CustomConfigScreen.this.itemRenderer;

            float p = (float) (Math.PI / 180f);

            RenderUtil.renderGuiItemRelative(this.icon, center + 90 - 17, top + 2, renderer, (s, r) -> {
                if (ticks != 0) {
                    if (r) {
                        s.mulPose(Vector3f.YP.rotation(((ticks + partialTicks) * p * 10f)));

                    } else {
                        float scale = 1 + 0.1f * Mth.sin(((ticks + partialTicks) * p * 20));
                        s.scale(scale, scale, scale);
                    }
                }
            });

            RenderUtil.renderGuiItemRelative(this.icon, center - 90, top + 2, renderer, (s, r) -> {
                if (ticks != 0) {
                    if (r) {
                        s.mulPose(Vector3f.YP.rotation((ticks + partialTicks) * p * 10f));

                    } else {
                        float scale = 1 + 0.1f * Mth.sin((ticks + partialTicks) * p * 20);
                        s.scale(scale, scale, scale);
                    }
                }
            });


        }

    }

    @Nullable
    public BooleanWrapper wrapBooleanItem(BooleanItem old, boolean displayItem) {
        final FolderEntry folderEntry = CustomConfigScreen.this.folderEntry;
        try {
            ValueHolder<Boolean> holder = (ValueHolder<Boolean>) CONFIG_VALUE_HOLDER.get(old);

            //find correct folder
            ValueEntry found = null;
            for (IEntry e : folderEntry.getEntries()) {
                if (e instanceof ValueEntry value) {
                    if (holder == value.getHolder()) found = value;
                }
            }
            if (found != null) {
                return displayItem ? new BooleanWrapperItem(holder) : new BooleanWrapper(holder);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private class BooleanWrapperItem extends BooleanWrapper {

        private final ItemStack item;

        public BooleanWrapperItem(ValueHolder<Boolean> holder) {
            super(holder);

            this.item = getIcon(label.getString().toLowerCase(Locale.ROOT));
            this.iconOffset = 7;
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            boolean on = this.holder.getValue();
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);

            int light = LightTexture.FULL_BRIGHT;
            if (!on) {

                //int sky = LightTexture.sky(light);
                //int block = 0;//LightTexture.block(light);
                light = 0;//LightTexture.pack(block, sky);
            }
            int center = (int) (this.button.x + this.button.getWidth() / 2f);
            ItemRenderer renderer = CustomConfigScreen.this.itemRenderer;

            RenderUtil.renderGuiItemRelative(this.item, center - 8 - iconOffset, top + 2, renderer, (a, b) -> {
            }, light, OverlayTexture.NO_OVERLAY);
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(Component.literal(""));
        }
    }

    private class BooleanWrapper extends BooleanItem {
        private static final int ICON_WIDTH = 12;
        protected Button button;
        protected boolean active = false;
        protected int iconOffset = 0;

        public BooleanWrapper(ValueHolder<Boolean> holder) {
            super(holder);
            try {
                button = (Button) BOOLEAN_ITEM_BUTTON.get(this);
            } catch (Exception ignored) {
            }
            button.setMessage(Component.literal(""));
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            this.button.setMessage(Component.literal(""));

            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);


            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, CustomConfigSelectScreen.MISC_ICONS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            int iconX = iconOffset + (int) (this.button.x + Math.ceil((this.button.getWidth() - ICON_WIDTH) / 2f));
            int iconY = (int) (this.button.y + Math.ceil(((this.button.getHeight() - ICON_WIDTH) / 2f)));

            int u = this.holder.getValue() ? ICON_WIDTH : 0;

            blit(poseStack, iconX, iconY, this.button.getBlitOffset(), (float) u, (float) 0, ICON_WIDTH, ICON_WIDTH, 64, 64);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(Component.literal(""));
        }
    }


}






    /*
    public class StringColorWrapper extends ConfigScreen.ConfigItem<String> {
        private final FocusedEditBox textField;

        public StringColorWrapper(ConfigScreen.ValueHolder<String> holder) {
            super(holder);
            this.textField = new FocusedEditBox(CustomConfigScreen.this.font, 0, 0, 44, 18, this.label);
            this.textField.setValue((holder.getValue()));
            this.textField.setResponder((s) -> {
                try {
                    if (holder.valueSpec.spawnParticleOnBoundingBox(s)) {
                        this.textField.setTextColor(14737632);
                        holder.setValue(s);
                        CustomConfigScreen.this.updateButtons();
                    } else {
                        this.textField.setTextColor(16711680);
                    }
                } catch (Exception var5) {
                    this.textField.setTextColor(16711680);
                }

            });
            this.eventListeners.add(this.textField);
        }

        public void render(PoseStack poseStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(poseStack, index, top, left, width, p_230432_6_, mouseX, mouseY, hovered, partialTicks);
            this.textField.x = left + width - 68;
            this.textField.y = top + 1;
            this.textField.render(poseStack, mouseX, mouseY, partialTicks);
        }

        public void onResetValue() {
            this.textField.setValue((this.holder.getValue()));
        }
    }
    */
