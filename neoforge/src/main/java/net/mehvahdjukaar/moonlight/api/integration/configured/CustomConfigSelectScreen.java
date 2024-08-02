package net.mehvahdjukaar.moonlight.api.integration.configured;


/*
public class CustomConfigSelectScreen extends ModConfigSelectionScreen {

    public static final ResourceLocation MISC_ICONS = Moonlight.res("textures/gui/misc_icons.png");

    private static final Field FILE_ITEM_BUTTON = CustomConfigScreen.findFieldOrNull(FileItem.class, "modifyButton");
    private static final Field FILE_ITEM_CONFIG = CustomConfigScreen.findFieldOrNull(FileItem.class, "config");

    private final BiFunction<CustomConfigSelectScreen, IModConfig, CustomConfigScreen> configScreenFactory;
    private final ItemStack mainIcon;
    private final String modId;
    private final String modURL;

    public CustomConfigSelectScreen(String modId, ItemStack mainIcon, String displayName, ResourceLocation background,
                                    Screen parent,
                                    BiFunction<CustomConfigSelectScreen, IModConfig, CustomConfigScreen> configScreenFactory,
                                    ConfigSpec... specs) {
        this(modId, mainIcon, displayName, background, parent, configScreenFactory, createConfigMap(specs));
    }

    public CustomConfigSelectScreen(String modId, ItemStack mainIcon, String displayName, ResourceLocation background,
                                    Screen parent,
                                    BiFunction<CustomConfigSelectScreen, IModConfig, CustomConfigScreen> configScreenFactory,
                                    Map<ConfigType, Set<IModConfig>> configMap) {
        super(parent, Component.literal(displayName), ensureNotNull(background), configMap);
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

    @Override
    public ResourceLocation getBackgroundTexture() {
        return super.getBackgroundTexture();
    }

    public String getModId() {
        return modId;
    }

    public static void registerConfigScreen(String modId, Function<Screen, CustomConfigSelectScreen> screenSelectFactory) {
        ModContainer container = ModList.get().getModContainerById(modId).get();
        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((m, s) -> screenSelectFactory.apply(s)));
    }

    private static Map<ConfigType, Set<IModConfig>> createConfigMap(ConfigSpec... specs) {
        Map<ConfigType, Set<IModConfig>> modConfigMap = new EnumMap<>(ConfigType.class);
        for (var ss : specs) {
            ConfigSpecWrapper s = (ConfigSpecWrapper) ss;
            ModConfig modConfig = s.getModConfig();
            var forgeConfig = new ForgeConfig(modConfig, ((ConfigSpecWrapper) ss).getSpec());
            var set = modConfigMap.computeIfAbsent(
                    forgeConfig.getType(), a -> new HashSet<>());
            set.add(forgeConfig);
        }
        return modConfigMap;
    }

    private static ConfigType getType(ConfigSpecWrapper s) {
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
                    FILE_ITEM_BUTTON.set(i, createModifyButton((IModConfig) FILE_ITEM_CONFIG.get(item)));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    private Button createModifyButton(IModConfig config) {
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
*/