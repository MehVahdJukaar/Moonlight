package net.mehvahdjukaar.moonlight.block_set;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.moonlight.client.language.AfterLanguageLoadEvent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BlockTypeRegistry<T extends BlockType> {

    protected boolean frozen = false;
    private final String name;
    private final List<BlockType.SetFinder<T>> finders = new ArrayList<>();
    private final List<ResourceLocation> notInclude = new ArrayList<>();
    private final List<T> builder = new ArrayList<>();
    private final Class<T> typeClass;
    private Map<ResourceLocation, T> types = new LinkedHashMap<>();

    public BlockTypeRegistry(Class<T> typeClass, String name) {
        this.typeClass = typeClass;
        this.name = name;
    }

    public Class<T> getType() {
        return typeClass;
    }

    /**
     * Gets corresponding block type or oak if the provided one is not installed or missing
     *
     * @param name string resource location name of the type
     * @return wood type
     */
    public T getFromNBT(String name) {
        return this.types.getOrDefault(new ResourceLocation(name), this.getDefaultType());
    }

    @Nullable
    public T get(ResourceLocation res) {
        return this.types.get(res);
    }

    public abstract T getDefaultType();

    public Collection<T> getValues(){
        return Collections.unmodifiableCollection(types.values());
    };

    public String typeName(){
        return name;
    };

    /**
     * Returns an optional block Type based on the given block. Pretty much defines the logic of how a block set is constructed
     */
    public abstract Optional<T> detectTypeFromBlock(Block block);

    public void registerBlockType(T newType) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to addListener wood types after registry events");
        }
        builder.add(newType);
    }

    public Collection<BlockType.SetFinder<T>> getFinders() {
        return finders;
    }

    public void addFinder(BlockType.SetFinder<T> finder) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to addListener block type finder after registry events");
        }
        finders.add(finder);
    }

    public void addRemover(ResourceLocation id) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried remove a block type after registry events");
        }
        notInclude.add(id);
    }

    private void finalizeAndFreeze() {
        if (frozen) {
            throw new UnsupportedOperationException("Block types are already finalized");
        }
        LinkedHashMap<ResourceLocation, T> linkedHashMap = new LinkedHashMap<>();
        builder.forEach(e -> {
            if (!linkedHashMap.containsKey(e.getId())) {
                linkedHashMap.put(e.getId(), e);
                //Selene.LOGGER.warn("Found wood type with duplicate id ({}), skipping",e.id);
            }
        });
        this.types = ImmutableMap.copyOf(linkedHashMap);
        builder.clear();
        this.frozen = true;
    }


    public void buildAll() {
        if (!frozen) {
            //adds default
            this.registerBlockType(this.getDefaultType());
            var finders = this.getFinders();
            //adds finders
            finders.stream().map(BlockType.SetFinder::get).forEach(f -> f.ifPresent(this::registerBlockType));
            for (Block b : Registry.BLOCK) {
                this.detectTypeFromBlock(b).ifPresent(t -> {
                    if (!notInclude.contains(t.getId())) this.registerBlockType(t);
                });
            }
            this.finalizeAndFreeze();
        }
    }

    /**
     * Called at the right time on language reload. Use to add translations of your block type names.
     * Useful to merge more complex translation strings using RPAwareDynamicTextureProvider::addDynamicLanguage
     */
    public void addTypeTranslations(AfterLanguageLoadEvent language) {

    }

}
