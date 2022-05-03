package net.mehvahdjukaar.selene.block_set;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public abstract class BlockTypeRegistry<T extends IBlockType> {

    protected boolean frozen = false;
    private final List<IBlockType.SetFinder<T>> finders = new ArrayList<>();
    private final List<ResourceLocation> notInclude = new ArrayList<>();
    private final List<T> builder = new ArrayList<>();

    /**
     * Gets corresponding block type or oak if the provided one is not installed or missing
     *
     * @param name string resource location name of the type
     * @return wood type
     */
    public T getFromNBT(String name) {
        return this.getTypes().getOrDefault(new ResourceLocation(name), this.getDefaultType());
    }

    public abstract T getDefaultType();

    public abstract Map<ResourceLocation, T> getTypes();

    /**
     * Returns an optional block Type based on the given block. Pretty much defines the logic of how a block set is constructed
     */
    public abstract Optional<T> detectTypeFromBlock(Block block);

    protected abstract void saveTypes(ImmutableMap<ResourceLocation, T> types);

    public void registerBlockType(T newType) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register wood types after registry events");
        }
        builder.add(newType);
    }

    public Collection<IBlockType.SetFinder<T>> getFinders() {
        return finders;
    }

    public void addFinder(IBlockType.SetFinder<T> finder) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register block type finder after registry events");
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
        saveTypes(ImmutableMap.copyOf(linkedHashMap));
        builder.clear();
        this.frozen = true;
    }


    public void buildAll() {
        if (!frozen) {
            //adds default
            this.registerBlockType(this.getDefaultType());
            var finders = this.getFinders();
            //adds finders
            finders.stream().map(IBlockType.SetFinder::get).forEach(f -> f.ifPresent(this::registerBlockType));
            for (Block b : ForgeRegistries.BLOCKS) {
                this.detectTypeFromBlock(b).ifPresent(t -> {
                    if (!notInclude.contains(t.getId())) this.registerBlockType(t);
                });
            }
            this.finalizeAndFreeze();
        }
    }


}
