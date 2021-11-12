package net.mehvahdjukaar.selene;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.selene.setup.ClientSetup;
import net.mehvahdjukaar.selene.setup.ModSetup;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
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

        bus.addListener(ModSetup::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientSetup::init));
    }

    public static float getMaxHeadXRot(ModelPart head) {
        return Mth.clamp(head.xRot, (-(float) Math.PI / 2.5F), ((float) Math.PI / 3F));
    }


    public static <T extends LivingEntity> void poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {

        animateHands(model, entity, true);

        twoHanded.setTwoHanded(true);

    }

    public static <T extends LivingEntity> void animateHands(HumanoidModel<T> model, T entity, boolean leftHand) {

        ModelPart mainHand = leftHand ? model.leftArm : model.rightArm;
        ModelPart offHand = leftHand ? model.rightArm : model.leftArm;

        Vec3 bx = new Vec3(1, 0, 0);
        Vec3 by = new Vec3(0, 1, 0);
        Vec3 bz = new Vec3(0, 0, 1);

        //head rot + hand offset from flute

        float downFacingRot = Mth.clamp(model.head.xRot, 0f, 0.8f);

        float xRot = getMaxHeadXRot(model.head) - (entity.isCrouching() ? 1F : 0.0F)
                - 0.3f + downFacingRot * 0.5f;

        bx = bx.xRot(xRot);
        by = by.xRot(xRot);
        bz = bz.xRot(xRot);

        Vec3 armVec = new Vec3(0, 0, 1);

        float mirror = leftHand ? -1 : 1;

        armVec = armVec.yRot(-0.99f * mirror);

        Vec3 newV = bx.scale(armVec.x).add(by.scale(armVec.y)).add(bz.scale(armVec.z));


        float yaw = (float) Math.atan2(-newV.x, newV.z);
        float len = (float) newV.length();

        float pitch = (float) Math.asin(newV.y / len);

        mainHand.yRot = (yaw + model.head.yRot * 1.4f - 0.1f * mirror) - 0.5f * downFacingRot * mirror;
        mainHand.xRot = (float) (pitch - Math.PI / 2f);


        offHand.yRot = (float) Mth.clamp((mainHand.yRot - 1 * mirror) * 0.2, -0.15, 0.15) + 1.1f * mirror;
        offHand.xRot = mainHand.xRot - 0.06f;


        //shoulder joint hackery
        float offset = leftHand ? -Mth.clamp(model.head.yRot, -1, 0) :
                Mth.clamp(model.head.yRot, 0, 1);

        // model.rightArm.x = -5.0F + offset * 2f;
        mainHand.z = -offset * 1f;

        // model.leftArm.x = -model.rightArm.x;
        // model.leftArm.z = -model.rightArm.z;

        //hax. unbobs left arm
        AnimationUtils.bobModelPart(model.leftArm, entity.tickCount, 1.0F);
        AnimationUtils.bobModelPart(model.rightArm, entity.tickCount, -1.0F);
    }


    public static <T extends LivingEntity> void poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {
        animateHands(model, entity, false);

        twoHanded.setTwoHanded(false);
    }


    public static <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> void renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack, HumanoidArm humanoidArm,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        ModelPart head = parentModel.getHead();

        //hax
        float oldRot = head.xRot;
        head.xRot = getMaxHeadXRot(head);
        head.translateAndRotate(poseStack);
        head.xRot = oldRot;

        CustomHeadLayer.translateToHead(poseStack, false);
        boolean leftHand = humanoidArm == HumanoidArm.LEFT;
        //let the model handle it
        poseStack.translate(0, -4.25 / 16f, -8.5 / 16f);
        if (leftHand) poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
        Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, stack, ItemTransforms.TransformType.HEAD,
                leftHand, poseStack, bufferSource, light);
        poseStack.popPose();
    }


    public static InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {


        //default base
        Vec3 bx = new Vec3(1, 0, 0);
        Vec3 by = new Vec3(0, 1, 0);
        Vec3 bz = new Vec3(0, 0, 1);

        float toRad = (float) (Math.PI / 180f);
        float xRot = -player.getXRot() * toRad;
        float yRot = -player.yHeadRot * toRad;
        //apply rotation matrix
        bx = bx.xRot(xRot).yRot(yRot);
        by = by.xRot(xRot).yRot(yRot);
        bz = bz.xRot(xRot).yRot(yRot);

        //rotate a vector on y axis
        Vec3 armVec = new Vec3(0, 0, 0.28 + world.random.nextFloat() * 0.5);

        int mirror = player.getMainArm() == HumanoidArm.RIGHT ^ player.getUsedItemHand() == InteractionHand.MAIN_HAND ? -1 : 1;


        armVec = armVec.yRot((float) (-Math.PI / 2f * mirror)).add(0, 0.15, 0.1);
        //z dist from face


        //new vector is rotated on y axis relative to the rotated base
        Vec3 newV = bx.scale(armVec.x)
                .add(by.scale(armVec.y))
                .add(bz.scale(armVec.z));


        double x = player.getX() + newV.x;
        double y = player.getEyeY() + newV.y;
        double z = player.getZ() + newV.z;


        world.addParticle(ParticleTypes.NOTE, x, y, z, (double) world.random.nextInt(24) / 24.0D, 0.0D, 0.0D);

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    public static void animateFirstPerson(LivingEntity entity, ItemStack stack, InteractionHand hand, PoseStack matrixStack,
                                          float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (true || entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim

            int mirror = entity.getMainArm() == HumanoidArm.RIGHT ^ hand == InteractionHand.MAIN_HAND ? -1 : 1;

            matrixStack.translate(-0.4*mirror,0.2,0);
            matrixStack.mulPose(Vector3f.ZN.rotationDegrees(90));

            float timeLeft = (float) stack.getUseDuration() - ((float)entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f12 = 1;

            float f15 = Mth.sin((timeLeft - 0.1F) * 1.3F);
            float f18 = f12 - 0.1F;
            float f20 = f15 * f18;



            matrixStack.scale(1.0F*mirror, -1.0F*mirror, -(1.0F + f12 * 0.2F));

            matrixStack.translate(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);

            matrixStack.translate(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
        }
    }

    //float offset = Mth.clamp(model.head.yRot, -1, 0);
    // model.rightArm.x = -5.0F + offset * 2f;
    // model.rightArm.z = 1.0F + offset * 1.2f;
    //model.head.visible = false;
//    Vec3 bx = new Vec3(1,0,0);
//    Vec3 by = new Vec3(0,1,0);
//    Vec3 bz = new Vec3(0,0,1);
//
//    float toRad = (float) (Math.PI/180f);
//
//    float t = (float) ((entity.tickCount/50f)%(Math.PI*2f));
//    float xRot =  -model.head.xRot+0.1f;
//
//
//    bx = bx.xRot(xRot);
//    by = by.xRot(xRot);
//    bz = bz.xRot(xRot);
//
//    Vec3 armVec = new Vec3(0,0,1);
//    armVec = armVec.yRot(-0.9f);
//
//    Vec3 newV = bx.multiply(armVec.x,armVec.x,armVec.x)
//            .add(by.multiply(armVec.y,armVec.y,armVec.y))
//            .add(bz.multiply(armVec.z,armVec.z,armVec.z));
//
//
//    //armVec = armVec.xRot((float) (model.head.xRot));
//
//
//    float roll = (float) Math.atan2(-armVec.x, -armVec.y);
//
//    //model.rightArm.translateAndRotate();
//
//    float yaw = (float) Math.atan2(newV.z, newV.x);
//    float len = (float) newV.length();
//
//    float pitch =  (float) Math.asin(newV.y/ len);
//    //(float) Math.atan2(newV.y, Math.sqrt(newV.z*newV.z + newV.x*newV.x)*yaw>0?1:-1);
//
//    //if(yaw>Math.PI) yaw = (float) (yaw-Math.PI);
//
//    //model.rightArm.zRot = Mth.cos(-model.head.xRot);
//
//
//    //model.rightArm.zRot = roll;
//    model.rightArm.yRot = (float) (yaw +Math.PI/2f);
//    model.rightArm.xRot = (float) (Math.PI/2f + pitch);


}
