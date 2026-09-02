package org.orecruncher.dsurround.mixins.audio;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.audio.Library;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.orecruncher.dsurround.Configuration;
import org.orecruncher.dsurround.lib.di.ContainerManager;
import org.orecruncher.dsurround.runtime.audio.AudioUtilities;
import org.orecruncher.dsurround.mixinutils.ISoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.nio.IntBuffer;

@Mixin(Library.class)
public class MixinSoundLibrary implements ISoundEngine {

    @Shadow
    private long currentDevice;

    public long dsurround_getDevicePointer() {
        return this.currentDevice;
    }

    /**
     * Rewrite the capability buffer.  We only do this if advanced processing is enabled.
     */
    @WrapOperation(method = "init(Ljava/lang/String;Z)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcCreateContext(JLjava/nio/IntBuffer;)J", remap = false))
    private long dsurround_buildCapabilities(long deviceHandle, IntBuffer attrList, Operation<Long> original) {
        if (AudioUtilities.doEnhancedSounds()) {
            // 1.21.11 already allocated a larger attribute buffer. Insert the EFX send count
            // before the terminator without dropping vanilla HRTF/limiter attributes.
            int terminator = attrList.limit() - 1;
            attrList.limit(attrList.capacity());
            attrList.position(terminator);
            attrList.put(EXTEfx.ALC_MAX_AUXILIARY_SENDS).put(4).put(0);
            attrList.flip();
            return ALC10.alcCreateContext(deviceHandle, attrList);
        } else {
            return original.call(deviceHandle, attrList);
        }
    }

    /**
     * Modify the number of streaming sounds that can be handled by the underlying sound engine.  The number of
     * channels to set is driven by config settings.
     *
     * @param v Existing value for the number of streaming sounds (should be 8)
     * @return The quantity of streaming sounds (should be at least 8)
     */
    @ModifyConstant(method = "init(Ljava/lang/String;Z)V", constant = @Constant(intValue = 8, ordinal = 0))
    public int dsurround_initialize(int v) {
        var config = ContainerManager.resolve(Configuration.SoundSystem.class);
        return config.streamingChannels;
    }
}
