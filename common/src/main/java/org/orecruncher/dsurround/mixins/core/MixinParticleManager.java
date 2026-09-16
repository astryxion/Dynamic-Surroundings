package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParticleEngine.class)
public interface MixinParticleManager {

    @Accessor("resourceManager")
    ParticleResources dsurround_getParticleResources();

    @Invoker("createParticle")
    <T extends ParticleOptions> Particle dsurround_createParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ);
}
