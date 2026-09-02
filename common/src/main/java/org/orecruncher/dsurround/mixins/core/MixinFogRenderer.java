package org.orecruncher.dsurround.mixins.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.orecruncher.dsurround.eventing.ClientEventHooks;
import org.orecruncher.dsurround.mixinutils.IBiomeExtended;
import org.orecruncher.dsurround.mixinutils.MixinHelpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    /**
     * On 1.21.11 setupFog returns Vector4f (fog color). FogData remains a local that is
     * written into the GPU fog UBO via updateBuffer. Inject immediately before that write
     * so FOG_RENDER_EVENT can still mutate the same FogData vanilla filled.
     */
    @Inject(
            method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
            )
    )
    private void dsurround_renderFog(Camera camera, int i, DeltaTracker deltaTracker, float renderDistance, ClientLevel level, CallbackInfoReturnable<Vector4f> cir, @Local FogData fogData) {

        if (camera.getFluidInCamera() != FogType.NONE)
            return;

        // At this point, Minecraft has already configured fog. It's possible that another
        // mixin fired and configured as well. We use the FogData vanilla (and other mixins)
        // already filled, which is the 26.1 equivalent of interrogating the shader uniforms.
        ClientEventHooks.FOG_RENDER_EVENT.raise().onRenderFog(fogData, renderDistance, deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    /**
     * Vanilla 26.1 moved biome fog color off Biome.getFogColor() onto AtmosphericFogEnvironment
     * reading EnvironmentAttributes.FOG_COLOR. Wrap that color source so biome config fog still applies.
     * On 1.21.11 computeFogColor returns Vector4f (no out-param).
     */
    @WrapOperation(method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)Lorg/joml/Vector4f;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;getBaseColor(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/Camera;IF)I"))
    private int dsurround_getFogColor(FogEnvironment environment, ClientLevel level, Camera camera, int renderDistance, float partialTick, Operation<Integer> original) {
        int vanilla = original.call(environment, level, camera, renderDistance, partialTick);
        if (camera.getFluidInCamera() != FogType.NONE)
            return vanilla;
        if (MixinHelpers.fogOptions.enableFogEffects && MixinHelpers.fogOptions.enableBiomeFog) {
            var info = ((IBiomeExtended) (Object) level.getBiome(camera.blockPosition()).value()).dsurround_getInfo();
            if (info != null) {
                var color = info.getFogColor();
                if (color != null)
                    return color.getValue();
            }
        }
        return vanilla;
    }
}
