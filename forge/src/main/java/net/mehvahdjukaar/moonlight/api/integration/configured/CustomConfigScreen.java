package net.mehvahdjukaar.moonlight.api.integration.configured;


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrcrayfish.configured.api.IConfigEntry;
import com.mrcrayfish.configured.api.IConfigValue;
import com.mrcrayfish.configured.api.IModConfig;
import com.mrcrayfish.configured.api.ValueEntry;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.impl.forge.ForgeConfig;
import com.mrcrayfish.configured.impl.forge.ForgeValue;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.forge.ConfigSpecWrapper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
//TODO: add back
/*
//credits to MrCrayfish's Configured Mod
//this is just a more customized version of Configured default config screen with some extra icons and such
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

    protected final String modId;
    protected final Map<String, ItemStack> icons = new HashMap<>();
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

    //shorthand
    protected CustomConfigScreen(CustomConfigSelectScreen parent, IModConfig config) {
        this(parent.getModId(), parent.getMainIcon(), parent.getBackgroundTexture(), parent.getTitle(), parent, config);
    }

    protected CustomConfigScreen(CustomConfigSelectScreen parent, ModConfig config) {
        this(parent.getModId(), parent.getMainIcon(), parent.getBackgroundTexture(), parent.getTitle(), parent, config);
    }

    protected CustomConfigScreen(String modId, ItemStack mainIcon, ResourceLocation background, Component title,
                                 Screen parent, ModConfig config) {
        this(modId, mainIcon, background, title, parent, new ForgeConfig(config));

    }

    //needed for custom title
    protected CustomConfigScreen(String modId, ItemStack mainIcon, ResourceLocation background, Component title,
                                 Screen parent, IModConfig config) {
        super(parent, title, config, CustomConfigSelectScreen.ensureNotNull(background));
        this.modId = modId;
        this.mainIcon = mainIcon;
    }

    @Override
    protected void constructEntries(List<Item> entries) {
        super.constructEntries(entries);
        List<Item> copy = new ArrayList<>(entries);
        entries.clear();
        ListIterator<Item> iter = copy.listIterator();
        while (iter.hasNext()) {
            var e = iter.next();
            if (e.getLabel().toLowerCase(Locale.ROOT).equals(getEnabledKeyword())) {
                iter.remove();
                entries.add(e);
            }
        }
        entries.addAll(copy);
    }

    public ItemStack getIcon(String... path) {
        String last = path[path.length - 1];
        if (path.length > 1 && last.equals(getEnabledKeyword())) {
            last = path[path.length - 2];
        }
        last = last.toLowerCase(Locale.ROOT).replace("_", " ");
        if (!icons.containsKey(last)) {
            String formatted = last.toLowerCase(Locale.ROOT).replace(" ", "_");
            var item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(modId, formatted));
            String finalLast = last;
            item.ifPresent(value -> addIcon(finalLast, value.asItem().getDefaultInstance()));
        }
        return icons.getOrDefault(last, ItemStack.EMPTY);
    }

    private void addIcon(String s, ItemStack i) {
        icons.put(s, i);
    }

    @Override
    protected void init() {
        super.init();

        this.list.replaceEntries(replaceItems(this.list.children()));
        Collection<Item> temp = replaceItems(this.entries);
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

    private Collection<Item> replaceItems(Collection<Item> originals) {
        ArrayList<Item> newList = new ArrayList<>();
        for (Item c : originals) {
            if (c instanceof FolderItem f) {
                FolderWrapper wrapper = wrapFolderItem(f);
                if (wrapper != null) {
                    newList.add(wrapper);
                    continue;
                }
            } else if (c instanceof BooleanItem b) {
                BooleanWrapper wrapper = wrapBooleanItem(b);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        Lighting.setupFor3DItems();
        int titleWidth = this.font.width(this.title) + 35;
        graphics.renderFakeItem(mainIcon, (this.width / 2) + titleWidth / 2 - 17, 2);
        graphics.renderFakeItem(mainIcon, (this.width / 2) - titleWidth / 2, 2);
    }

    private int ticks = 0;

    @Override
    public void tick() {
        super.tick();
        ticks++;
    }

    @Nullable
    public FolderWrapper wrapFolderItem(FolderItem old) {
        try {
            String oldName = old.getLabel();
            //find correct folder
            IConfigEntry found = null;
            for (IConfigEntry e : folderEntry.getChildren()) {
                if (!(e instanceof ValueEntry)) {
                    String n = Component.literal(ConfigScreen.createLabel(e.getEntryName())).getString();
                    if (n.equals(oldName)) {
                        found = e;
                        break;
                    }
                }
            }
            if (found != null) {
                return new FolderWrapper(found, oldName);
            }
        } catch (Exception ignored) {
            Moonlight.LOGGER.error("error", ignored);
        }

        return null;
    }

    public abstract CustomConfigScreen createSubScreen(Component title);

    public String getEnabledKeyword() {
        return "enabled";
    }

    //ugly
    public List<ConfigSpec> getCustomSpecs() {
        return List.of();
    }


    private class FolderWrapper extends FolderItem {

        private final ItemStack icon;
        protected final Button button;
        protected boolean light;

        private int ticks = 0;
        private int lastTick = 1;

        private FolderWrapper(IConfigEntry folderEntry, String label) {
            super(folderEntry);
            //make new button I can access
            this.button = Button.builder(Component.literal(label).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE),
                            (onPress) -> {
                                Component newTitle = CustomConfigScreen.this.title.plainCopy().append(" > " + label);
                                var sc = createSubScreen(newTitle);
                                //hax
                                try {
                                    FOLDER_ENTRY.set(sc, folderEntry);
                                } catch (Exception ignored) {
                                }
                                CustomConfigScreen.this.minecraft.setScreen(sc);
                            })
                    .bounds(10, 5, 44, 20)
                    .build();

            var i = getIcon(label.toLowerCase(Locale.ROOT));
            this.icon = i.isEmpty() ? mainIcon : i;
            this.light = getFolderEnabledValue(folderEntry);
        }

        private boolean getFolderEnabledValue(IConfigEntry entry) {
            for (var c : entry.getChildren()) {
                IConfigValue<?> value = c.getValue();
                if (value != null && value.getName().equals(getEnabledKeyword())) {
                    Object object = value.get();
                    if (object instanceof Boolean b) {
                        return b;
                    }
                }
            }
            return true;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.button);
        }

        @Override
        public void render(GuiGraphics graphics, int x, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTicks) {

            int light = this.light ? LightTexture.FULL_BRIGHT : 0;

            if (lastTick < CustomConfigScreen.this.ticks) {
                ticks = Math.max(0, ticks + (hovered ? 1 : -2)) % (36);
            }

            this.lastTick = CustomConfigScreen.this.ticks;


            this.button.setX(left - 1);
            this.button.setY(top);
            this.button.setWidth(width);
            this.button.render(graphics, mouseX, mouseY, partialTicks);

            int center = this.button.getX() + width / 2;

            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

            RenderUtil.renderGuiItemRelative(graphics.pose(), this.icon, center + 95 - 17, top + 2, renderer,
                    (s, m) -> rotateItem(ticks, partialTicks, s, m), light, OverlayTexture.NO_OVERLAY);

            RenderUtil.renderGuiItemRelative(graphics.pose(), this.icon, center - 95, top + 2, renderer,
                    (s, m) -> rotateItem(ticks, partialTicks, s, m), light, OverlayTexture.NO_OVERLAY);

        }

    }

    @Nullable
    public BooleanWrapper wrapBooleanItem(BooleanItem old) {
        try {
            IConfigValue<Boolean> holder = (IConfigValue<Boolean>) CONFIG_VALUE_HOLDER.get(old);

            //find correct folder
            ValueEntry found = null;
            for (IConfigEntry e : folderEntry.getChildren()) {
                if (e instanceof ValueEntry value) {
                    if (holder == value.getValue()) found = value;
                }
            }
            if (found != null) {
                var path = ((ForgeValue<Boolean>) holder).configValue.getPath().toArray(String[]::new);
                ItemStack icon = getIcon(path);
                return new BooleanWrapper(holder, icon);
            }
        } catch (Exception ignored) {
            Moonlight.LOGGER.error("error");
        }
        return null;
    }


    private class BooleanWrapper extends BooleanItem {
        private static final int ICON_SIZE = 12;

        private final ItemStack item;
        protected final int iconOffset;
        protected final boolean needsGameRestart;
        protected boolean doesNeedsGameRestart = false;

        protected Button button;
        private int ticks = 0;
        private int lastTick = 1;

        public BooleanWrapper(IConfigValue<Boolean> holder, ItemStack item) {
            super(holder);

            try {
                button = (Button) BOOLEAN_ITEM_BUTTON.get(this);
            } catch (Exception ignored) {
            }
            button.setMessage(Component.literal(""));

            this.needsGameRestart = hackyCheckIfValueNeedsGameRestart(holder);
            this.item = item;
            this.iconOffset = item.isEmpty() ? 0 : 7;
        }

        public BooleanWrapper(IConfigValue<Boolean> holder) {
            this(holder, ItemStack.EMPTY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            var r = super.mouseClicked(mouseX, mouseY, button);
            this.doesNeedsGameRestart = !this.doesNeedsGameRestart;
            return r;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            this.button.setMessage(Component.literal(""));
            super.render(graphics, index, top, left, width, height, mouseX, mouseY, hovered, partialTicks);
            //hovered concerns the entire entry not just the button
            hovered = this.button.isMouseOver(mouseX, mouseY);
            if (lastTick < CustomConfigScreen.this.ticks) {
                this.ticks = Math.max(0, ticks + (hovered ? 1 : -2)) % (36);
                if (!hovered && this.ticks > 17) this.ticks %= 18;
            }

            this.lastTick = CustomConfigScreen.this.ticks;

            //world restart stuff for forge values
            if (doesNeedsGameRestart) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.blit(IconButton.ICONS, left - 18, top + 5, 11, 11, 51.0F, 22.0F, 11, 11, 64, 64);
                if (MthUtils.isWithinRectangle(left - 18, top + 5, 11, 11, mouseX, mouseY)) {
                    String translationKey = "configured.gui.requires_game_restart";
                    int outline = -1438090048;
                    CustomConfigScreen.this.setActiveTooltip(Component.translatable(translationKey), outline);
                }
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            int iconX = iconOffset + (int) (this.button.getX() + Math.ceil((this.button.getWidth() - ICON_SIZE) / 2f));
            int iconY = (int) (this.button.getY() + Math.ceil(((this.button.getHeight() - ICON_SIZE) / 2f)));

            boolean on = this.holder.get();

            int u = on ? ICON_SIZE : 0;

            graphics.blit(CustomConfigSelectScreen.MISC_ICONS, iconX, iconY, 0, u, 0, ICON_SIZE, ICON_SIZE, 64, 64);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);

            if (!item.isEmpty()) {
                int light = on ? LightTexture.FULL_BRIGHT : 0;
                int center = (int) (this.button.getX() + this.button.getWidth() / 2f);
                ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

                RenderUtil.renderGuiItemRelative(graphics.pose(), this.item, center - 8 - iconOffset, top + 2, renderer,
                        (s, m) -> rotateItem(ticks, partialTicks, s, m), light, OverlayTexture.NO_OVERLAY);
            }
        }

        private boolean hackyCheckIfValueNeedsGameRestart(IConfigValue<Boolean> value) {
            for (var v : CustomConfigScreen.this.getCustomSpecs()) {
                if (v.getFileName().equals(CustomConfigScreen.this.config.getFileName())) {
                    return ((ConfigSpecWrapper) v).requiresGameRestart(((ForgeValue<Boolean>) value).configValue);
                }
            }
            return false;
        }

        @Override
        public void onResetValue() {
            this.button.setMessage(Component.literal(""));
        }
    }


    private static void rotateItem(int ticks, float partialTicks, PoseStack s, BakedModel m) {
        if (ticks != 0) {
            if (m.usesBlockLight()) {
                s.mulPose(Axis.YP.rotation((ticks + partialTicks) * Mth.DEG_TO_RAD * 10f));

            } else {
                float scale = 1 + 0.1f * Mth.sin((ticks + partialTicks) * Mth.DEG_TO_RAD * 20);
                s.scale(scale, scale, scale);
            }
        }
    }
}
*/


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
