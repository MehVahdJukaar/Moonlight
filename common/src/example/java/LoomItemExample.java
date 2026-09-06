import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.function.Supplier;

// Add loom supported items
public class LoomItemExample extends Item implements ILoomItem {

    public LoomItemExample(Properties properties) {
        super(properties);
    }

    @Override
    public DyeColor getLoomBaseColor(ItemStack stack) {
        return DyeColor.WHITE;
    }

    @Override
    public Identifier getLoomSlotIcon() {
        return Identifier.fromNamespaceAndPath("my_mod", "item/gui_slots/empty_slot_flag");
    }

    @Override
    public Supplier<LoomItemRenderer> getLoomRenderer() {
        return () -> Renderer.INSTANCE;
    }

    // client only. skip this whole thing if the vanilla hanging banner is fine for you
    private static class Renderer implements LoomItemRenderer {

        private static final Renderer INSTANCE = new Renderer();

        // called every frame the screen is up. patterns is null when theres nothing to preview yet
        @Override
        public boolean render(GuiGraphics graphics, ItemStack bannerSlotStack, ItemStack result,
                              BannerPatternLayers patterns, int leftPos, int topPos, float partialTicks) {
            if (patterns == null) return false;
            //vanilla hangs its flag around 139, 52 from the gui corner
            graphics.renderItem(result, leftPos + 131, topPos + 44);
            return true; //drew it ourselves, dont run the vanilla one on top
        }

        // one of the 16 little buttons in the pattern grid. box is 5 by 10 at x + 4, y + 2
        @Override
        public boolean renderPatternIcon(GuiGraphics graphics, ItemStack bannerSlotStack,
                                         Holder<BannerPattern> pattern, int x, int y) {
            return false; //false keeps the vanilla white on gray flags
        }
    }
}
