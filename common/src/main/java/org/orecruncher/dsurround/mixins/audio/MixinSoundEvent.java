package org.orecruncher.dsurround.mixins.audio;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Can't believe there isn't a toString() override
 */
@Mixin(SoundEvent.class)
public class MixinSoundEvent {

    @Shadow
    @Final
    private Identifier location;
    @Shadow
    @Final
    private Optional<Float> fixedRange;

    @Inject(method = "toString()Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void dsurround_toString(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("%s{fixedRange %s}".formatted(this.location.toString(), this.fixedRange.toString()));
    }
}
