package dev.sefiraat.cultivation.implementation.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Replacement for SefiLib's ParticleUtils, whose coloured particles use {@code Particle.REDSTONE}.
 * That particle was renamed to {@link Particle#DUST} in Minecraft 1.20.5 and no longer exists.
 */
public final class ParticleUtils {

    private ParticleUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void displayParticleRandomly(@Nonnull Location location,
                                               @Nonnull Particle particle,
                                               double rangeRadius,
                                               int numberOfParticles
    ) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        for (int i = 0; i < numberOfParticles; i++) {
            world.spawnParticle(particle, randomOffset(location, rangeRadius), 1);
        }
    }

    public static void displayParticleRandomly(@Nonnull Entity entity,
                                               @Nonnull Particle particle,
                                               double rangeRadius,
                                               int numberOfParticles
    ) {
        displayParticleRandomly(entity.getLocation().add(0, 1, 0), particle, rangeRadius, numberOfParticles);
    }

    public static void displayParticleRandomly(@Nonnull Location location,
                                               double rangeRadius,
                                               int numberOfParticles,
                                               @Nonnull Particle.DustOptions dustOptions
    ) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        for (int i = 0; i < numberOfParticles; i++) {
            world.spawnParticle(Particle.DUST, randomOffset(location, rangeRadius), 1, dustOptions);
        }
    }

    public static void displayParticleRandomly(@Nonnull Entity entity,
                                               double rangeRadius,
                                               int numberOfParticles,
                                               @Nonnull Particle.DustOptions dustOptions
    ) {
        displayParticleRandomly(entity.getLocation(), rangeRadius, numberOfParticles, dustOptions);
    }

    @Nonnull
    private static Location randomOffset(@Nonnull Location location, double rangeRadius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return location.clone().add(
            random.nextDouble(-rangeRadius, rangeRadius + 0.1),
            random.nextDouble(-rangeRadius, rangeRadius + 0.1),
            random.nextDouble(-rangeRadius, rangeRadius + 0.1)
        );
    }
}
