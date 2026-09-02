package org.orecruncher.dsurround.effects.particles;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.orecruncher.dsurround.lib.GameUtils;

public class DsurroundParticleRenderType {

    private final Identifier texture;
    private final ParticleRenderType group;
    private final SingleQuadParticle.Layer layer;

    public DsurroundParticleRenderType(final Identifier texture) {
        this.texture = texture;
        this.group = new ParticleRenderType(texture.toString(), "DS");
        this.layer = new SingleQuadParticle.Layer(true, texture, RenderPipelines.TRANSLUCENT_PARTICLE);
        GameUtils.getTextureManager().getTexture(texture);
    }

    public ParticleRenderType getGroup() {
        return this.group;
    }

    public SingleQuadParticle.Layer getLayer() {
        return this.layer;
    }

    protected VertexFormat getVertexFormat() {
        return DefaultVertexFormat.PARTICLE; //.POSITION_TEXTURE_COLOR_LIGHT;
    }

    protected Identifier getTexture() {
        return this.texture;
    }

    @Override
    public String toString() {
        return this.texture.toString();
    }
}
