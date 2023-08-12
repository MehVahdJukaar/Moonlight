package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * This interface will be injected in the base item class. Its used to add extra behaviors to items
 */
@ApiStatus.Internal
public interface IExtendedItem {

    @Nullable
    AdditionalItemPlacement moonlight$getAdditionalBehavior();

    void moonlight$addAdditionalBehavior(AdditionalItemPlacement b);
}
