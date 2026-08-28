package net.mehvahdjukaar.moonlight.api.map.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** Render state of one custom map decoration, filled in by a MapDecorationRenderer during extract. */
public class MLDecorationRenderState {

    @Nullable
    public TextureAtlasSprite sprite;
    public byte x;
    public byte y;
    public byte rot;
    public int color = -1;
    public boolean outline;
    public boolean renderOnFrame = true;
    @Nullable
    public Component name;
}
