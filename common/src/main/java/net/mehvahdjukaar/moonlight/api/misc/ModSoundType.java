package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Lazy sound type like forge one
 */
public class ModSoundType extends SoundType {
    private final Supplier<SoundEvent> breakSound;
    private final Supplier<SoundEvent> stepSound;
    private final Supplier<SoundEvent> placeSound;
    private final Supplier<SoundEvent> hitSound;
    private final Supplier<SoundEvent> fallSound;

    public ModSoundType(float volumeIn, float pitchIn, Supplier<SoundEvent> breakSoundIn, Supplier<SoundEvent> stepSoundIn, Supplier<SoundEvent> placeSoundIn, Supplier<SoundEvent> hitSoundIn, Supplier<SoundEvent> fallSoundIn) {
        super(volumeIn, pitchIn, null, null, null, null, null);
        this.breakSound = breakSoundIn;
        this.stepSound = stepSoundIn;
        this.placeSound = placeSoundIn;
        this.hitSound = hitSoundIn;
        this.fallSound = fallSoundIn;
    }

    @Override
    public @NotNull SoundEvent getBreakSound() {
        return this.breakSound.get();
    }

    @Override
    public @NotNull SoundEvent getStepSound() {
        return this.stepSound.get();
    }

    @Override
    public @NotNull SoundEvent getPlaceSound() {
        return this.placeSound.get();
    }

    @Override
    public @NotNull SoundEvent getHitSound() {
        return this.hitSound.get();
    }

    @Override
    public @NotNull SoundEvent getFallSound() {
        return this.fallSound.get();
    }
}
