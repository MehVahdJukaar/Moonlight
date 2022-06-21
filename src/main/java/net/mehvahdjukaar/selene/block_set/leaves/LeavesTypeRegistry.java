package net.mehvahdjukaar.selene.block_set.leaves;

import net.mehvahdjukaar.selene.block_set.BlockTypeRegistry;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.selene.client.language.AfterLanguageLoadEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class LeavesTypeRegistry extends BlockTypeRegistry<LeavesType> {

    public static LeavesType OAK_TYPE = new LeavesType(new ResourceLocation("oak"), Blocks.OAK_LEAVES, WoodTypeRegistry.OAK_TYPE);

    public static LeavesTypeRegistry INSTANCE;

    public static Map<ResourceLocation, LeavesType> getTypes(){
        return INSTANCE.getValues();
    }

    @Nullable
    public static LeavesType getValue(ResourceLocation name) {
        return INSTANCE.get(name);
    }

    public static LeavesType fromNBT(String name) {
        return INSTANCE.getValues().getOrDefault(new ResourceLocation(name), OAK_TYPE);
    }
    
    public LeavesTypeRegistry() {
        super(LeavesType.class, "leaves_type");
        INSTANCE = this;
    }
    
    @Override
    public LeavesType getDefaultType() {
        return OAK_TYPE;
    }

    //returns if this block is the base plank block
    @Override
    public Optional<LeavesType> detectTypeFromBlock(@NotNull Block baseBlock) {
        ResourceLocation baseRes = baseBlock.getRegistryName();
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

                    return Optional.of(new LeavesType(id, baseBlock, null));
                }
            }
            //}
        }
        return Optional.empty();
    }

    @Override
    public void buildAll() {
        if (!frozen) {
            for (var v : WoodTypeRegistry.getTypes().values()) {
                Block leaves = v.getBlockOfThis("leaves");
                if (leaves != null) {
                    this.registerBlockType(new LeavesType(v.id, leaves, v));
                }
            }
        }
        super.buildAll();
    }


    @Override
    public void addTypeTranslations(AfterLanguageLoadEvent language) {
        this.getValues().forEach((r, w) -> {
            if (language.isDefault()) language.addEntry(w.getTranslationKey(), w.getReadableName());
        });
    }
}
