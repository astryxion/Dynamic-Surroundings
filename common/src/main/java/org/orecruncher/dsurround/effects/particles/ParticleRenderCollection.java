package org.orecruncher.dsurround.effects.particles;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.orecruncher.dsurround.eventing.ClientEventHooks;
import org.orecruncher.dsurround.eventing.ClientState;
import org.orecruncher.dsurround.eventing.CollectDiagnosticsEvent;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.collections.ObjectArray;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Special particle that proxies a collection in the particle engine. The commonality
 * of the collection is rendering setup. This collection is centered on the player
 * to prevent it from going out of scope. It is modeled on the NoRenderParticle in
 * Minecraft.
 */
public final class ParticleRenderCollection<TParticle extends SingleQuadParticle> extends SingleQuadParticle {

    private final Consumer<Camera> setup;
    private final Supplier<Identifier> textureSupplier;
    private final ObjectArray<TParticle> particles;

    private ParticleRenderCollection(@NotNull ClientLevel clientLevel, @NotNull Supplier<Identifier> textureSupplier, @Nullable Consumer<Camera> setup) {
        super(clientLevel, 0, 0, 0, ParticleUtils.getSpriteProvider(ParticleTypes.CLOUD).first());
        this.setup = Objects.requireNonNullElseGet(setup, () -> this::standardSetup);
        this.textureSupplier = textureSupplier;
        GameUtils.getTextureManager().getTexture(textureSupplier.get());
        this.particles = new ObjectArray<>(128);
        this.tick();
    }

    @NotNull
    @Override
    public ParticleRenderType getGroup() {
        // Can't use NO_RENDER as the ParticleEngine will not attempt to render
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return new SingleQuadParticle.Layer(true, this.textureSupplier.get(), RenderPipelines.TRANSLUCENT_PARTICLE);
    }

    @Override
    public void tick() {
        // Keep the particle colocated with the player
        var playerPos = GameUtils.getPlayer().orElseThrow().getEyePosition();
        this.setPos(playerPos.x(), playerPos.y(), playerPos.z());

        if (!this.particles.isEmpty()) {
            this.particles.forEach(Particle::tick);
            this.particles.removeIf(p -> !p.isAlive());
        }
    }

    @Override
    public void extract(@NotNull QuadParticleRenderState state, @NotNull Camera camera, float tickDelta) {
        if (this.particles.isEmpty())
            return;

        this.setup.accept(camera);
        this.particles.forEach(p -> p.extract(state, camera, tickDelta));
    }

    private void standardSetup(@NotNull Camera camera) {
        // Depth mask, blend, and default blend func are expressed by TRANSLUCENT_PARTICLE on each child's Layer.
    }

    public void add(@NotNull TParticle particle) {
        // Can only accept custom style particles
        if (particle.getGroup() != this.getGroup())
            throw new RuntimeException("Can only add render type %s particles to collection".formatted(this.getGroup()));
        this.particles.add(particle);
    }

    /**
     * Helper that manages related particles in Minecraft's ParticleEngine. The helper will register with events, so
     * instances of this class need to be maintained as singletons throughout the lifetime of the client.
     */
    public static final class Helper<TParticle extends SingleQuadParticle> {

        private final String name;
        private final Consumer<Camera> setup;
        private final Supplier<Identifier> textureSupplier;

        private WeakReference<ParticleRenderCollection<TParticle>> particle;
        private String diagnostics;

        /**
         * Initializes a helper instance used to manage the state of the main particle within the ParticleEngine.
         * Particle rendering will use the default setup.
         *
         * @param name The name of the helper; used in diagnostics
         * @param textureSupplier Provides the texture to bind when rendering
         */
        public Helper(@NotNull String name, @NotNull Supplier<Identifier> textureSupplier) {
            this(name, textureSupplier, null);
        }

        /**
         * Initializes a helper instance used to manage the state of the main particle within the ParticleEngine.
         *
         * @param name The name of the helper; used in diagnostics
         * @param textureSupplier Provides the texture to bind when rendering
         * @param setup Provides for the configuration of the rendering system if the default is not enough
         */
        public Helper(@NotNull String name, @NotNull Supplier<Identifier> textureSupplier, @Nullable Consumer<Camera> setup) {
            this.name = name;
            this.setup = setup;
            this.textureSupplier = textureSupplier;
            this.diagnostics = this.name;

            ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(state -> this.clear());
            ClientEventHooks.COLLECT_DIAGNOSTICS.register(this::collectDiagnostics);
            ClientState.TICK_END.register(this::tick);
        }

        /**
         * Adds a particle to the helper.
         *
         * @param particle The particle to add
         */
        public void add(TParticle particle) {
            this.get().add(particle);
        }

        @NotNull
        private ParticleRenderCollection<TParticle> get() {
            var pc = this.particle != null ? this.particle.get() : null;
            if (pc == null || !pc.isAlive()) {
                pc = new ParticleRenderCollection<>(GameUtils.getWorld().orElseThrow(), this.textureSupplier, this.setup);
                this.particle = new WeakReference<>(pc);
                GameUtils.getParticleManager().add(pc);
            }
            return pc;
        }

        private void clear() {
            var pc = this.particle != null ? this.particle.get() : null;
            if (pc != null) {
                pc.remove();
                this.particle = null;
            }
        }

        private void tick(@NotNull Minecraft client) {
            var pc = this.particle != null ? this.particle.get() : null;
            this.diagnostics = this.name + ": ";
            if (pc == null)
                this.diagnostics += "Not Set";
            else if (!pc.isAlive())
                this.diagnostics += "DEAD";
            else
                this.diagnostics += pc.particles.size();
        }

        private void collectDiagnostics(@NotNull CollectDiagnosticsEvent event) {
            event.add(CollectDiagnosticsEvent.Section.Particles, this.diagnostics);
        }
    }
}
