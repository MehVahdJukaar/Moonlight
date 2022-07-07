package net.mehvahdjukaar.moonlight.api.set.wood;

import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.client.language.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class WoodTypeRegistry extends BlockTypeRegistry<WoodType> {

    public static WoodType OAK_TYPE = new WoodType(new ResourceLocation("oak"), Blocks.OAK_PLANKS, Blocks.OAK_LOG);

    public static WoodTypeRegistry INSTANCE;

    public static Collection<WoodType> getTypes() {
        return INSTANCE.getValues();
    }

    @Nullable
    public static WoodType getValue(ResourceLocation name) {
        return INSTANCE.get(name);
    }

    public static WoodType fromNBT(String name) {
        return INSTANCE.getFromNBT(name);
    }

    //instance stuff

    public WoodTypeRegistry() {
        super(WoodType.class, "wood_type");
        INSTANCE = this;
    }

    @Override
    public WoodType getDefaultType() {
        return OAK_TYPE;
    }

    //returns if this block is the base plank block
    @Override
    public Optional<WoodType> detectTypeFromBlock(Block baseBlock) {
        ResourceLocation baseRes = Utils.getID(baseBlock);
        String name = null;
        String path = baseRes.getPath();
        //needs to contain planks in its name
        if (path.endsWith("_planks")) {
            name = path.substring(0, path.length() - "_planks".length());
        } else if (path.startsWith("planks_")) {
            name = path.substring("planks_".length());
        } else if (path.endsWith("_plank")) {
            name = path.substring(0, path.length() - "_plank".length());
        } else if (path.startsWith("plank_")) {
            name = path.substring("plank_".length());
        }
        if (name != null && !baseRes.getNamespace().equals("securitycraft")) {
            BlockState state = baseBlock.defaultBlockState();
            //cant check if the block is a full one so I do this. Adding some checks here
            if (state.getProperties().size() <= 2 && !(baseBlock instanceof SlabBlock)) {
                //needs to use wood sound type
                //if (state.getSoundType() == SoundType.WOOD) { //wood from tcon has diff sounds
                Material mat = state.getMaterial();
                //and have correct material
                if (mat == Material.WOOD || mat == Material.NETHER_WOOD) {
                    //we do not allow "/" in the wood name
                    name = name.replace("/", "_");
                    ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), name);
                    Block logBlock = findLog(id);
                    if (logBlock != null) {
                        return Optional.of(new WoodType(id, baseBlock, logBlock));
                    }
                }
            }
            //}
        }
        return Optional.empty();
    }

    @Nullable
    private static Block findLog(ResourceLocation id) {
        ResourceLocation[] test = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_log"),
                new ResourceLocation(id.getNamespace(), "log_" + id.getPath()),
                new ResourceLocation(id.getPath() + "_log"),
                new ResourceLocation("log_" + id.getPath()),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_stem"),
                new ResourceLocation(id.getNamespace(), "stem_" + id.getPath()),
                new ResourceLocation(id.getPath() + "_stem"),
                new ResourceLocation("stem_" + id.getPath())
        };
        Block temp = null;
        for (var r : test) {
            if (Registry.BLOCK.containsKey(r)) {
                temp = Registry.BLOCK.get(r);
                break;
            }
        }
        return temp;
    }


    @Override
    public void addTypeTranslations(AfterLanguageLoadEvent language) {
        getValues().forEach((w) -> {
            if (language.isDefault()) language.addEntry(w.getTranslationKey(), w.getReadableName());
        });
    }
}
