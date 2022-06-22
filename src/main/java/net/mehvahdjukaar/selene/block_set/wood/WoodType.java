package net.mehvahdjukaar.selene.block_set.wood;

import net.mehvahdjukaar.selene.Moonlight;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.mehvahdjukaar.selene.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class WoodType extends BlockType {

    public final Material material;
    public final Block planks;
    public final Block log;

    @Nullable
    private final net.minecraft.world.level.block.state.properties.WoodType vanillaType;

    protected WoodType(ResourceLocation id, Block baseBlock, Block logBlock) {
        super(id);
        this.planks = baseBlock;
        this.log = logBlock;
        this.material = baseBlock.defaultBlockState().getMaterial();

        String i = id.getNamespace().equals("minecraft") ? "" : id.getNamespace() + "/" + id.getPath();
        var o = net.minecraft.world.level.block.state.properties.WoodType.values().filter(v -> v.name().equals(i)).findAny();
        this.vanillaType = o.orElse(null);
    }

    @Nullable
    protected Block findLogRelatedBlock(String append, String postpend) {
        String post = postpend.isEmpty() ? "" : "_" + postpend;
        var id = this.getId();
        String log = Utils.getID(this.log).getPath();
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), log + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + log + post),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + id.getPath() + post)
        };
        Block found = null;
        for (var r : targets) {
            if (ForgeRegistries.BLOCKS.containsKey(r)) {
                found = ForgeRegistries.BLOCKS.getValue(r);
                break;
            }
        }
        return found;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public ItemLike mainChild() {
        return planks;
    }

    @Nullable
    public net.minecraft.world.level.block.state.properties.WoodType toVanilla() {
        return this.vanillaType;
    }

    /**
     * Use this to get the texture path of a wood type
     *
     * @return something like minecraft/oak
     */
    public String getTexturePath() {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return this.getTypeName();
        return this.getNamespace() + "/" + this.getTypeName();
    }

    public boolean canBurn() {
        return this.material.isFlammable();
    }

    public MaterialColor getColor() {
        return this.material.getColor();
    }

    @Override
    public String getTranslationKey() {
        return "wood_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    protected void initializeVanillaChildren() {
        this.addChild("planks", this.planks);
        this.addChild("log", this.log);
        this.addChild("leaves", this.findRelatedEntry("leaves", ForgeRegistries.BLOCKS));
        this.addChild("stripped_log", this.findLogRelatedBlock("stripped", "log"));
        this.addChild("stripped_wood", this.findLogRelatedBlock("stripped", "wood"));
        this.addChild("wood", this.findRelatedEntry("wood", ForgeRegistries.BLOCKS));
        this.addChild("slab", this.findRelatedEntry("slab", ForgeRegistries.BLOCKS));
        this.addChild("stairs", this.findRelatedEntry("stairs", ForgeRegistries.BLOCKS));
        this.addChild("fence", this.findRelatedEntry("fence", ForgeRegistries.BLOCKS));
        this.addChild("fence_gate", this.findRelatedEntry("fence_gate", ForgeRegistries.BLOCKS));
        this.addChild("door", this.findRelatedEntry("door", ForgeRegistries.BLOCKS));
        this.addChild("trapdoor", this.findRelatedEntry("trapdoor", ForgeRegistries.BLOCKS));
        this.addChild("button", this.findRelatedEntry("button", ForgeRegistries.BLOCKS));
        this.addChild("pressure_plate", this.findRelatedEntry("pressure_plate", ForgeRegistries.BLOCKS));
        this.addChild("boat", this.findRelatedEntry("boat", ForgeRegistries.ITEMS));
        this.addChild("sign", this.findRelatedEntry("sign", ForgeRegistries.ITEMS));
    }

    public static class Finder extends SetFinder<WoodType> {

        private final Map<String, ResourceLocation> childNames = new HashMap<>();
        private final Supplier<Block> planksFinder;
        private final Supplier<Block> logFinder;
        private final ResourceLocation id;

        public Finder(ResourceLocation id, Supplier<Block> planks, Supplier<Block> log) {
            this.id = id;
            this.planksFinder = planks;
            this.logFinder = log;
        }

        public static Finder simple(String modId, String woodTypeName, String planksName, String logName) {
            return simple(new ResourceLocation(modId, woodTypeName), new ResourceLocation(modId, planksName), new ResourceLocation(modId, logName));
        }

        public static Finder simple(ResourceLocation woodTypeName, ResourceLocation planksName, ResourceLocation logName) {
            return new Finder(woodTypeName,
                    () -> ForgeRegistries.BLOCKS.getValue(planksName),
                    () -> ForgeRegistries.BLOCKS.getValue(logName));
        }

        public void addChild(String childType, String childName) {
            addChild(childType, new ResourceLocation(id.getNamespace(), childName));
        }

        public void addChild(String childType, ResourceLocation childName) {
            this.childNames.put(childType, childName);
        }

        @ApiStatus.Internal
        @Override
        public Optional<WoodType> get() {
            if (ModList.get().isLoaded(id.getNamespace())) {
                try {
                    Block plank = planksFinder.get();
                    Block log = logFinder.get();
                    var d = ForgeRegistries.BLOCKS.getValue(ForgeRegistries.BLOCKS.getDefaultKey());
                    if (plank != d && log != d && plank != null && log != null) {
                        var w = new WoodType(id, plank, log);
                        childNames.forEach((key, value) -> w.addChild(key, ForgeRegistries.BLOCKS.getValue(value)));
                        return Optional.of(w);
                    }
                } catch (Exception ignored) {
                }
                Moonlight.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }
    }


}
