import net.mehvahdjukaar.moonlight.api.misc.HolderRef;
import net.mehvahdjukaar.moonlight.api.misc.OptHolderRef;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.Block;

public class LazyHolderExample {

    // reference to a DamageType object from a datapack registry
    public static final HolderRef<DamageType> IN_FIRE = HolderRef.of(
            Identifier.withDefaultNamespace("in_fire"), Registries.DAMAGE_TYPE);

    // here it can be used as a reference to a block from another mod that might not be present
    public static final OptHolderRef<Block> DEPLOYER = HolderRef.optional(
            Identifier.parse("create:deployer"), Registries.BLOCK);

}
