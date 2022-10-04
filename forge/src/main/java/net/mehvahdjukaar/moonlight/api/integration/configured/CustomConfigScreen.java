package net.mehvahdjukaar.moonlight.api.integration.configured;


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.configured.api.IConfigEntry;
import com.mrcrayfish.configured.api.IConfigValue;
import com.mrcrayfish.configured.api.IModConfig;
import com.mrcrayfish.configured.api.ValueEntry;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.impl.forge.ForgeConfig;
import com.mrcrayfish.configured.impl.forge.ForgeFolderEntry;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
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
    public CustomConfigScreen(CustomConfigSelectScreen parent, IModConfig config) {
        this(parent.getModId(), parent.getMainIcon(), parent.getBackgroundTexture(), parent.getTitle(), parent, config);
    }

    public CustomConfigScreen(CustomConfigSelectScreen parent, ModConfig config) {
        this(parent.getModId(), parent.getMainIcon(), parent.getBackgroundTexture(), parent.getTitle(), parent, config);
    }

    public CustomConfigScreen(String modId, ItemStack mainIcon, ResourceLocation background, Component title,
                              Screen parent, ModConfig config) {
        this(modId, mainIcon, background, title, parent, new ForgeConfig(config));

    }

    //needed for custom title
    public CustomConfigScreen(String modId, ItemStack mainIcon, ResourceLocation background, Component title,
                              Screen parent, IModConfig config) {
        super(parent, title, config, background);
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        int titleWidth = this.font.width(this.title) + 35;
        this.itemRenderer.renderAndDecorateFakeItem(mainIcon, (this.width / 2) + titleWidth / 2 - 17, 2);
        this.itemRenderer.renderAndDecorateFakeItem(mainIcon, (this.width / 2) - titleWidth / 2, 2);
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
            ForgeFolderEntry found = null;
            for (IConfigEntry e : folderEntry.getChildren()) {
                if (e instanceof ForgeFolderEntry f) {
                    String n = Component.literal(ConfigScreen.createLabel(f.getEntryName())).getString();
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
            Moonlight.LOGGER.error("error",ignored);
        }

        return null;
    }

    public abstract CustomConfigScreen createSubScreen(Component title);

    private class FolderWrapper extends FolderItem {

        private final ItemStack icon;
        protected final Button button;

        private FolderWrapper(ForgeFolderEntry folderEntry, String label) {
            super(folderEntry);
            //make new button I can access
            this.button = new Button(10, 5, 44, 20, (Component.literal(label)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE), (onPress) -> {
                Component newTitle = CustomConfigScreen.this.title.plainCopy().append(" > " + label);
                var sc = createSubScreen(newTitle);
                //hax
                try {
                    FOLDER_ENTRY.set(sc, folderEntry);
                } catch (Exception ignored) {
                }
                CustomConfigScreen.this.minecraft.setScreen(sc);
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
                return displayItem ? new BooleanWrapperItem(holder) : new BooleanWrapper(holder);
            }
        } catch (Exception ignored) {
            Moonlight.LOGGER.error("error");
        }
        return null;
    }

    private class BooleanWrapperItem extends BooleanWrapper {

        private final ItemStack item;

        public BooleanWrapperItem(IConfigValue<Boolean> holder) {
            super(holder);

            this.item = getIcon(label.getString().toLowerCase(Locale.ROOT));
            this.iconOffset = 7;
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            boolean on = this.holder.get();
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

        public BooleanWrapper(IConfigValue<Boolean> holder) {
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

            int u = this.holder.get() ? ICON_WIDTH : 0;

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
