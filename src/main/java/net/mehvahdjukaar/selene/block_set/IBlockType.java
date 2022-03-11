package net.mehvahdjukaar.selene.block_set;

import java.util.Optional;

public interface IBlockType {

    String toString();

    String getNamespace();

    abstract class SetFinder<T extends IBlockType>{

        public abstract Optional<T> get();
    }
}
