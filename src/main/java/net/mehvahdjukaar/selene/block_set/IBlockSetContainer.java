package net.mehvahdjukaar.selene.block_set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface IBlockSetContainer<T extends IBlockType> {

    T fromNBT(String string);

    T getDefaultType();

    Map<ResourceLocation, T> getTypes();

    void registerBlockType(T newType);

    Optional<T> scanAndGet(Block block);

    void addFinder(IBlockType.SetFinder<T> finder);

    Collection<IBlockType.SetFinder<T>> getFinders();

    void finalizeAndFreeze();

    default void buildAll(){
        //adds default
        this.registerBlockType(this.getDefaultType());
        var finders = this.getFinders();
        //adds finders
        finders.stream().map(IBlockType.SetFinder::get).forEach(f->f.ifPresent(this::registerBlockType));
        for(Block b : ForgeRegistries.BLOCKS){
            this.scanAndGet(b).ifPresent(this::registerBlockType);
        }
        finalizeAndFreeze();
    }
}
