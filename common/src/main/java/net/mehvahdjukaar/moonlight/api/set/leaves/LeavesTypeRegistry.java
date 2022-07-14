package net.mehvahdjukaar.moonlight.api.set.leaves;

import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class LeavesTypeRegistry extends BlockTypeRegistry<LeavesType> {

    public static final LeavesTypeRegistry INSTANCE = new LeavesTypeRegistry();

    public static final LeavesType OAK_TYPE = new LeavesType(new ResourceLocation("oak"), Blocks.OAK_LEAVES);

    public static Collection<LeavesType> getTypes() {
        return INSTANCE.getValues();
    }

    @Nullable
    public static LeavesType getValue(ResourceLocation name) {
        return INSTANCE.get(name);
    }

    public static LeavesType fromNBT(String name) {
        return INSTANCE.getFromNBT(name);
    }

    public LeavesTypeRegistry() {
        super(LeavesType.class, "leaves_type");
    }

    @Override
    public LeavesType getDefaultType() {
        return OAK_TYPE;
    }

    //returns if this block is the base plank block
    @Override
    public Optional<LeavesType> detectTypeFromBlock(@NotNull Block baseBlock) {
        ResourceLocation baseRes = Utils.getID(baseBlock);
        String name = null;
        String path = baseRes.getPath();
        //needs to contain planks in its name
        if (path.endsWith("_leaves")) {
            name = path.substring(0, path.length() - "_leaves".length());
        } else if (path.startsWith("leaves_")) {
            name = path.substring("leaves_".length());
        }
        if (name != null && !baseRes.getNamespace().equals("securitycraft")) {
            if (baseBlock instanceof LeavesBlock) {
                BlockState state = baseBlock.defaultBlockState();
                Material mat = state.getMaterial();
                //and have correct material
                if (mat == Material.LEAVES) {
                    ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), name);

                    return Optional.of(new LeavesType(id, baseBlock));
                }
            }
            //}
        }
        return Optional.empty();
    }

    @Override
    public void addTypeTranslations(AfterLanguageLoadEvent language) {
        this.getValues().forEach((w) -> {
            if (language.isDefault()) language.addEntry(w.getTranslationKey(), w.getReadableName());
        });
    }
}
