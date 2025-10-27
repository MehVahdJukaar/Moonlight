package net.mehvahdjukaar.moonlight.core.misc;

import net.minecraft.core.HolderLookup;

public interface LoaderCondition {

    boolean test(HolderLookup.Provider ra);
}
