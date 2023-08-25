package net.mehvahdjukaar.moonlight.api.set.wood;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
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
        if (this.id.getNamespace().equals("tfc")) {
            var o = BuiltInRegistries.BLOCK.getOptional(
                    new ResourceLocation(id.getNamespace(),
                            "wood/" + append + "_" + postpend+"/" + id.getPath()));
            if (o.isPresent()) return o.get();
        }

        String post = postpend.isEmpty() ? "" : "_" + postpend;
        var id = this.getId();
        String logN = Utils.getID(this.log).getPath();
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), logN + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + logN + post),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + id.getPath() + post)
        };
        Block found = null;
        for (var r : targets) {
            if (BuiltInRegistries.BLOCK.containsKey(r)) {
                found = BuiltInRegistries.BLOCK.get(r);
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
    public void initializeChildrenBlocks() {
        this.addChild("planks",(Object) this.planks);
        this.addChild("log",(Object) this.log);
        this.addChild("leaves", (Object)this.findRelatedEntry("leaves", BuiltInRegistries.BLOCK));
        this.addChild("stripped_log",(Object) this.findLogRelatedBlock("stripped", "log"));
        this.addChild("stripped_wood",(Object) this.findLogRelatedBlock("stripped", "wood"));
        this.addChild("wood",(Object) this.findRelatedEntry("wood", BuiltInRegistries.BLOCK));
        this.addChild("slab",(Object) this.findRelatedEntry("slab", BuiltInRegistries.BLOCK));
        this.addChild("stairs",(Object) this.findRelatedEntry("stairs", BuiltInRegistries.BLOCK));
        this.addChild("fence",(Object) this.findRelatedEntry("fence", BuiltInRegistries.BLOCK));
        this.addChild("fence_gate",(Object) this.findRelatedEntry("fence_gate", BuiltInRegistries.BLOCK));
        this.addChild("door",(Object) this.findRelatedEntry("door", BuiltInRegistries.BLOCK));
        this.addChild("trapdoor",(Object) this.findRelatedEntry("trapdoor", BuiltInRegistries.BLOCK));
        this.addChild("button",(Object) this.findRelatedEntry("button", BuiltInRegistries.BLOCK));
        this.addChild("pressure_plate",(Object) this.findRelatedEntry("pressure_plate", BuiltInRegistries.BLOCK));
    }

    @Override
    public void initializeChildrenItems() {
        this.addChild("boat",(Object) this.findRelatedEntry("boat", BuiltInRegistries.ITEM));
        this.addChild("chest_boat",(Object) this.findRelatedEntry("chest_boat", BuiltInRegistries.ITEM));
        this.addChild("sign",(Object) this.findRelatedEntry("sign", BuiltInRegistries.ITEM));
    }

    public static class Finder implements SetFinder<WoodType> {

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
                    () -> BuiltInRegistries.BLOCK.get(planksName),
                    () -> BuiltInRegistries.BLOCK.get(logName));
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
            if (PlatformHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block plank = planksFinder.get();
                    Block log = logFinder.get();
                    var d = BuiltInRegistries.BLOCK.get(BuiltInRegistries.BLOCK.getDefaultKey());
                    if (plank != d && log != d && plank != null && log != null) {
                        var w = new WoodType(id, plank, log);
                        childNames.forEach((key, value) -> w.addChild(key,(Object) BuiltInRegistries.BLOCK.get(value)));
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
