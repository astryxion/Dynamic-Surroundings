package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ParticleResources.class)
public interface MixinParticleResources {

    @Accessor("spriteSets")
    Map<Identifier, SpriteSet> dsurround_getSpriteAwareFactories();
}
