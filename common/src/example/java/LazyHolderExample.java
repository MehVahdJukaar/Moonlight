import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.Block;

public class LazyHolderExample {

    // reference to a DamageType object from a datapack registry
    public static final DynamicHolder<DamageType> IN_FIRE = DynamicHolder.of(
            new ResourceLocation( "in_fire"), Registries.DAMAGE_TYPE);

    // here it can be used as a reference to a block from another mod that might not be present
    public static final DynamicHolder<Block> DEPLOYER = DynamicHolder.of(
            new ResourceLocation( "create:deployer"), Registries.BLOCK);

}
