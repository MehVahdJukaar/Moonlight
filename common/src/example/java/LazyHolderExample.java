import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.misc.OptionalHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class LazyHolderExample {

    // reference to a DamageType object from a datapack registry
    public static final DynamicHolder<DamageType> IN_FIRE = DynamicHolder.of(
            ResourceLocation.withDefaultNamespace( "in_fire"), Registries.DAMAGE_TYPE);

    // here it can be used as a reference to a block from another mod that might not be present
    public static final OptionalHolder<Block> DEPLOYER = OptionalHolder.of(
            ResourceLocation.parse( "create:deployer"), Registries.BLOCK);

    //both these implement the Holder and Supplier interfaces

}
