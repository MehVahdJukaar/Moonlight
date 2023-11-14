package net.mehvahdjukaar.moonlight.api.misc;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.spongepowered.asm.mixin.injection.IInjectionPointContext;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.points.BeforeInvoke;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;

import java.util.Collection;


// hack. dont use with shift
@InjectionPoint.AtCode(namespace = "moonlight", value = "INVOKE_UNRESTRICTED")
public class InvokeUnrestricted extends BeforeInvoke {

    public InvokeUnrestricted(InjectionPointData data) {
        super(data);
    }

    @Override
    protected boolean addInsn(InsnList insns, Collection<AbstractInsnNode> nodes, AbstractInsnNode insn) {
        insn = InjectionPoint.nextNode(insns, insn);
        nodes.add(insn);
        return true;
    }

    @Override
    public InjectionPoint.RestrictTargetLevel getTargetRestriction(IInjectionPointContext context) {
        return RestrictTargetLevel.ALLOW_ALL;
    }
}
