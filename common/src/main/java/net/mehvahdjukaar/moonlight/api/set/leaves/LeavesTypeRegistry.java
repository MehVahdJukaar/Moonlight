package net.mehvahdjukaar.moonlight.api.set.leaves;

import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LeavesTypeRegistry extends BlockTypeRegistry<LeavesType> {

    public static final LeavesTypeRegistry INSTANCE = new LeavesTypeRegistry();

    private final Map<Identifier, Identifier> specialLeavesToWood = new HashMap<>();
    private final Map<LeavesType, WoodType> leavesToWood = new IdentityHashMap<>();

    public LeavesTypeRegistry() {
        super(LeavesType.class, "leaves_type");

        LeavesType.CODEC = this.getCodec();
        LeavesType.STREAM_CODEC = this.getStreamCodecExplicit();
    }

    static void touch() {
    }

    @Override
    /// manual registry method. Only for Vanilla LeavesType
    protected LeavesType register(LeavesType vanillaType) {
        return super.register(vanillaType);
    }

    @Override
    public LeavesType getDefaultType() {
        return VanillaLeavesTypes.OAK;
    }

    @Nullable
    public WoodType getEquivalentWoodType(LeavesType leavesType) {
        return leavesToWood.get(leavesType);
    }

    //returns if this block is the base plank block
    @Override
    public Optional<LeavesType> detectTypeFromBlock(Block baseBlock, Identifier baseId) {
        String name = null;
        String path = baseId.getPath();
        //needs to contain planks in its name
        if (path.endsWith("_leaves")) {
            name = path.substring(0, path.length() - "_leaves" .length());
        } else if (path.startsWith("leaves_")) {
            name = path.substring("leaves_" .length());
        }
        String namespace = baseId.getNamespace();
        if (name != null && !namespace.equals("securitycraft") && !path.contains("hanging")) {
            if (baseBlock instanceof LeavesBlock) {
                Identifier id = baseId.withPath(name);
                if (!valuesReg.containsKey(id)) return Optional.of(new LeavesType(id, baseBlock));
            }
        }
        return Optional.empty();
    }

    //- BLACKLISTED_MODS
    private static boolean isBlacklisted(String namespace, String path) {
        return namespace.equals("securitycraft") || namespace.equals("dynamic_trees") ||
                namespace.matches("dynamictrees|dt\\w+") || path.contains("hanging");
    }


    @Override
    public void finalizeAndFreeze() {
        super.finalizeAndFreeze();

        // add wood to leaves mapping. we know this runs after wood types are registered
        for (var l : this.getValues()) {
            Identifier leavesId = l.id;
            Identifier id = specialLeavesToWood.getOrDefault(leavesId, leavesId);
            WoodType o = WoodTypeRegistry.INSTANCE.get(id);
            String path = id.getPath();
            String namespace = id.getNamespace();
            if (o == null) {
                for (WoodType w : WoodTypeRegistry.INSTANCE.getValues()) {
                    if (w.id.getPath().equals(path)) {
                        o = w;
                        break;
                    }
                }
            }
            if (o == null) {
                //this assigns "variant leaves types" to their expected vanilla woods
                //i.e. "blossoming_oak" -> "oak"
                for (WoodType w : WoodTypeRegistry.INSTANCE.getValues()) {
                    if (w.isVanilla() || w.id.getNamespace().equals(namespace)) { //true vanilla
                        if (path.endsWith(w.id.getPath())) {
                            o = w;
                            //don't break to avoid associating "oak" instead of "dark_oak"
                        }
                    }
                }
            }
            if (o != null) {
                leavesToWood.put(l, o);
            }
        }
    }


    // Adds a mapping from leaves type to wood type.
    // Used for non-conventional wood types or leaves types that don't have a log
    public void addLeavesToWoodMapping(Identifier leavesTypeId, Identifier woodTypeId) {
        specialLeavesToWood.put(leavesTypeId, woodTypeId);
    }

    public void addLeavesToWoodMapping(String leavedId, String woodId) {
        addLeavesToWoodMapping(Identifier.parse(leavedId), Identifier.parse(woodId));
    }

    public void addLeavesToWoodMapping(String modId, String leavesTypeName, String woodTypeName) {
        addLeavesToWoodMapping(Identifier.fromNamespaceAndPath(modId, leavesTypeName), Identifier.fromNamespaceAndPath(modId, woodTypeName));
    }

    @Override
    public int priority() {
        return 99;
    }

    //shorthand for add finder. Gives a builder-like object that's meant to be configured inline
    public LeavesType.Finder addSimpleFinder(Identifier typeId) {
        LeavesType.Finder finder = new LeavesType.Finder(typeId);
        this.addFinder(finder);
        return finder;
    }


    public LeavesType.Finder addSimpleFinder(String typeId) {
        return addSimpleFinder(Identifier.parse(typeId));
    }

    public LeavesType.Finder addSimpleFinder(String namespace, String name) {
        return addSimpleFinder(Identifier.fromNamespaceAndPath(namespace, name));
    }

}
