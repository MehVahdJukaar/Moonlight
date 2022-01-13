package net.mehvahdjukaar.selene;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.selene.setup.ClientSetup;
import net.mehvahdjukaar.selene.setup.ModSetup;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.mehvahdjukaar.selene.villager_ai.VillagerAIManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Selene.MOD_ID)
public class Selene {


    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    public Selene() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(bus);
        bus.addListener(ModSetup::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientSetup::init));
    }



}
