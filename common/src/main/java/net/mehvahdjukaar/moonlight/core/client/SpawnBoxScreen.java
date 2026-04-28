package net.mehvahdjukaar.moonlight.core.client;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ServerBoundUpdateBoxBlockTileMessage;
import net.mehvahdjukaar.moonlight.core.worldgen.SpawnBoxBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SpawnBoxScreen extends Screen {
    private static final Component POSITION_LABEL = Component.translatable("structure_block.position");
    private static final Component SIZE_LABEL = Component.translatable("message.moonlight.spawn_box_block.size");
    private static final Component SHOW_BOUNDING_BOX_LABEL = Component.translatable("structure_block.show_boundingbox");
    private static final Component FINAL_STATE_LABEL = Component.translatable("jigsaw_block.final_state");

    private EditBox nameEdit;
    private EditBox posXEdit;
    private EditBox posYEdit;
    private EditBox posZEdit;
    private EditBox sizeXEdit;
    private EditBox sizeYEdit;
    private EditBox sizeZEdit;
    private CycleButton<Boolean> toggleBoundingBox;
    private final SpawnBoxBlockEntity tile;
    private EditBox finalStateEdit;

    public SpawnBoxScreen(SpawnBoxBlockEntity tile) {
        super(Component.translatable(MoonlightRegistry.SPAWN_BOX_BLOCK.get().getDescriptionId()));
        this.tile = tile;
    }

    public static void open(SpawnBoxBlockEntity spawnBoxBlockEntity) {
        Minecraft.getInstance().setScreen(new SpawnBoxScreen(spawnBoxBlockEntity));
    }

    private void save() {
        if (this.sendToServer()) {
            this.minecraft.setScreen(null);
        }
    }

    private void onCancel() {
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.save()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.toggleBoundingBox = this.addRenderableWidget(
                CycleButton.onOffBuilder(this.tile.getShowBoundingBox())
                        .displayOnlyValue()
                        .create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_LABEL,
                                (cycleButton, boolean_) -> this.tile.setShowBoundingBox(boolean_))
        );
        this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, Component.translatable("structure_block.structure_name")) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return isValidCharacterForName(this.getValue(), codePoint, this.getCursorPosition()) &&
                        super.charTyped(codePoint, modifiers);
            }
        };
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.tile.getTargetName());
        this.addRenderableWidget(this.nameEdit);
        BlockPos blockPos = this.tile.getBoxOffset();
        this.posXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.position.x"));
        this.posXEdit.setMaxLength(15);
        this.posXEdit.setValue(Integer.toString(blockPos.getX()));
        this.addRenderableWidget(this.posXEdit);
        this.posYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.position.y"));
        this.posYEdit.setMaxLength(15);
        this.posYEdit.setValue(Integer.toString(blockPos.getY()));
        this.addRenderableWidget(this.posYEdit);
        this.posZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.position.z"));
        this.posZEdit.setMaxLength(15);
        this.posZEdit.setValue(Integer.toString(blockPos.getZ()));
        this.addRenderableWidget(this.posZEdit);
        Vec3i vec3i = this.tile.getSize();
        this.sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.size.x"));
        this.sizeXEdit.setMaxLength(15);
        this.sizeXEdit.setValue(Integer.toString(vec3i.getX()));
        this.addRenderableWidget(this.sizeXEdit);
        this.sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.size.y"));
        this.sizeYEdit.setMaxLength(15);
        this.sizeYEdit.setValue(Integer.toString(vec3i.getY()));
        this.addRenderableWidget(this.sizeYEdit);
        this.sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 120, 80, 20, Component.translatable("structure_block.size.z"));
        this.sizeZEdit.setMaxLength(15);
        this.sizeZEdit.setValue(Integer.toString(vec3i.getZ()));
        this.addRenderableWidget(this.sizeZEdit);

        this.finalStateEdit = new EditBox(this.font, this.width / 2 - 153, 160, 300, 20, FINAL_STATE_LABEL);
        this.finalStateEdit.setMaxLength(256);
        this.finalStateEdit.setValue(this.tile.getFinalState());
        this.addRenderableWidget(this.finalStateEdit);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String string = this.nameEdit.getValue();
        String string2 = this.posXEdit.getValue();
        String string3 = this.posYEdit.getValue();
        String string4 = this.posZEdit.getValue();
        String string5 = this.sizeXEdit.getValue();
        String string6 = this.sizeYEdit.getValue();
        String string7 = this.sizeZEdit.getValue();
        this.init(minecraft, width, height);
        this.nameEdit.setValue(string);
        this.posXEdit.setValue(string2);
        this.posYEdit.setValue(string3);
        this.posZEdit.setValue(string4);
        this.sizeXEdit.setValue(string5);
        this.sizeYEdit.setValue(string6);
        this.sizeZEdit.setValue(string7);
    }

    private boolean sendToServer() {
        BlockPos pos = new BlockPos(
                this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue())
        );
        Vec3i size = new Vec3i(
                this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue())
        );
        NetworkHelper.sendToServer(new ServerBoundUpdateBoxBlockTileMessage(
                this.tile.getBlockPos(),
                pos, size, this.nameEdit.getValue(), this.toggleBoundingBox.getValue(),
                this.finalStateEdit.getValue()
        ));

        return true;
    }

    private int parseCoordinate(String coordinate) {
        try {
            return Integer.parseInt(coordinate);
        } catch (NumberFormatException var3) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode != 257 && keyCode != 335) {
            return false;
        } else {
            this.save();
            return true;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);

        guiGraphics.drawString(this.font, POSITION_LABEL, this.width / 2 - 153, 70, 10526880);

        guiGraphics.drawString(this.font, SIZE_LABEL, this.width / 2 - 153, 110, 10526880);

        guiGraphics.drawString(this.font, SHOW_BOUNDING_BOX_LABEL, this.width / 2 + 154 - this.font.width(SHOW_BOUNDING_BOX_LABEL), 70, 10526880);

        guiGraphics.drawString(this.font, FINAL_STATE_LABEL, this.width / 2 - 153, 150, 10526880);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
