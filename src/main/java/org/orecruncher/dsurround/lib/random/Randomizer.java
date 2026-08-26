package org.orecruncher.dsurround.lib.random;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.lib.Library;

import java.util.function.Supplier;

/**
 * Pluggable randomizer instances to be used. At the time of this checking, the Java randomizer "Xoroshiro128PlusPlus"
 * provides the best performance. The Minecraft randomizer is next, followed by the legacy Random implementation. Given
 * the number of randoms generated per tick, I sweat these details.
 */
@SuppressWarnings("unused")
public final class Randomizer implements IRandomizer {
    private static final Supplier<IRandomizer> DEFAULT_RANDOMIZER = Randomizer::getRandomizer;
    private static final ThreadLocal<IRandomizer> THREAD_LOCAL = ThreadLocal.withInitial(DEFAULT_RANDOMIZER);
    /**
     * Reusable instance that wraps a ThreadLocal. Guards against multiple threads trying to utilize the same
     * concrete randomizer.
     */
    private static final IRandomizer SHARED = new Randomizer();

    /**
     * Returns a shared instance of the default randomizer.
     */
    public static IRandomizer current() {
        return SHARED;
    }

    private Randomizer() {
    }

    @Override
    public @NotNull RandomSource fork() {
        return THREAD_LOCAL.get().fork();
    }

    @Override
    public @NotNull PositionalRandomFactory forkPositional() {
        return THREAD_LOCAL.get().forkPositional();
    }

    @Override
    public void setSeed(long l) {
        THREAD_LOCAL.get().setSeed(l);
    }

    @Override
    public int nextInt() {
        return THREAD_LOCAL.get().nextInt();
    }

    @Override
    public int nextInt(int i) {
        return THREAD_LOCAL.get().nextInt(i);
    }

    @Override
    public long nextLong() {
        return THREAD_LOCAL.get().nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return THREAD_LOCAL.get().nextBoolean();
    }

    @Override
    public float nextFloat() {
        return THREAD_LOCAL.get().nextFloat();
    }

    @Override
    public double nextDouble() {
        return THREAD_LOCAL.get().nextDouble();
    }

    @Override
    public double nextGaussian() {
        return THREAD_LOCAL.get().nextGaussian();
    }

    private static IRandomizer getRandomizer() {
        IRandomizer randomizer = tryCreateJavaRandomizer(JavaRandomizer.XOROSHIRO_128_PLUS_PLUS);
        if (randomizer != null)
            return randomizer;

        randomizer = tryCreateJavaRandomizer(JavaRandomizer.SPLITTABLE_RANDOM);
        if (randomizer != null)
            return randomizer;

        Library.LOGGER.info("Falling back to Minecraft randomizer");
        return new MinecraftRandomizer();
    }

    private static IRandomizer tryCreateJavaRandomizer(String algorithm) {
        try {
            Library.LOGGER.info("Creating RandomGenerator '%s'", algorithm);
            return new JavaRandomizer(algorithm);
        } catch (Exception ex) {
            Library.LOGGER.info("RandomGenerator '%s' is unavailable (%s)", algorithm, ex.getMessage());
            return null;
        }
    }
}
