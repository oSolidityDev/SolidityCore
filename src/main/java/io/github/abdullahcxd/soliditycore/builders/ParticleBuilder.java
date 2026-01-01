package io.github.abdullahcxd.soliditycore.builders;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A builder for spawning particle effects with a fluent API
 */
public class ParticleBuilder {

    private Particle particle;
    private int count = 1;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double offsetZ = 0.0;
    private double speed = 0.0;
    private Object data = null;
    private boolean force = false;

    private ParticleBuilder(@NotNull Particle particle) {
        this.particle = particle;
    }

    /**
     * Creates a new ParticleBuilder
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ParticleBuilder create(@NotNull Particle particle) {
        return new ParticleBuilder(particle);
    }

    /**
     * Sets the particle type
     */
    public ParticleBuilder particle(@NotNull Particle particle) {
        this.particle = particle;
        return this;
    }

    /**
     * Sets the number of particles to spawn
     */
    public ParticleBuilder count(int count) {
        this.count = Math.max(0, count);
        return this;
    }

    /**
     * Sets the offset for particle randomization
     */
    public ParticleBuilder offset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    /**
     * Sets the offset using a Vector
     */
    public ParticleBuilder offset(@NotNull Vector vector) {
        return offset(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Sets the offset to a single value for all axes
     */
    public ParticleBuilder offset(double offset) {
        return offset(offset, offset, offset);
    }

    /**
     * Sets the particle speed/extra data
     */
    public ParticleBuilder speed(double speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Sets the particle data (used for certain particles)
     */
    public ParticleBuilder data(@Nullable Object data) {
        this.data = data;
        return this;
    }

    /**
     * Sets whether to force render (shows particles even far away)
     */
    public ParticleBuilder force(boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Sets force render to true
     */
    public ParticleBuilder force() {
        return force(true);
    }

    /**
     * Sets color for REDSTONE particles
     */
    public ParticleBuilder color(@NotNull Color color) {
        return color(color, 1.0f);
    }

    /**
     * Sets color and size for REDSTONE particles
     */
    public ParticleBuilder color(@NotNull Color color, float size) {
        this.particle = Particle.DUST;
        this.data = new Particle.DustOptions(color, size);
        return this;
    }

    /**
     * Sets RGB color for REDSTONE particles
     */
    public ParticleBuilder color(int r, int g, int b) {
        return color(Color.fromRGB(r, g, b));
    }

    /**
     * Sets RGB color and size for REDSTONE particles
     */
    public ParticleBuilder color(int r, int g, int b, float size) {
        return color(Color.fromRGB(r, g, b), size);
    }

    /**
     * Spawns the particle at a location
     */
    public void spawn(@NotNull Location location) {
        if (location.getWorld() == null) return;

        if (data != null) {
            location.getWorld().spawnParticle(particle, location, count,
                    offsetX, offsetY, offsetZ, speed, data, force);
        } else {
            location.getWorld().spawnParticle(particle, location, count,
                    offsetX, offsetY, offsetZ, speed, force);
        }
    }

    /**
     * Spawns the particle for specific players only
     */
    public void spawn(@NotNull Location location, @NotNull Collection<? extends Player> players) {
        if (location.getWorld() == null) return;

        for (Player player : players) {
            if (data != null) {
                player.spawnParticle(particle, location, count,
                        offsetX, offsetY, offsetZ, speed, data, force);
            } else {
                player.spawnParticle(particle, location, count,
                        offsetX, offsetY, offsetZ, speed, force);
            }
        }
    }

    /**
     * Spawns the particle for a single player
     */
    public void spawn(@NotNull Location location, @NotNull Player player) {
        if (data != null) {
            player.spawnParticle(particle, location, count,
                    offsetX, offsetY, offsetZ, speed, data, force);
        } else {
            player.spawnParticle(particle, location, count,
                    offsetX, offsetY, offsetZ, speed, force);
        }
    }

    // === Quick Static Methods ===

    /**
     * Quickly spawns particles at a location
     */
    public static void quick(@NotNull Location location, @NotNull Particle particle, int count) {
        create(particle).count(count).spawn(location);
    }

    // === Common Particle Effects ===

    /**
     * Spawns a heart effect
     */
    public static void heart(@NotNull Location location) {
        create(Particle.HEART)
                .count(3)
                .offset(0.5, 0.5, 0.5)
                .spawn(location);
    }

    /**
     * Spawns a flame effect
     */
    public static void flame(@NotNull Location location) {
        create(Particle.FLAME)
                .count(10)
                .offset(0.3, 0.3, 0.3)
                .speed(0.02)
                .spawn(location);
    }

    /**
     * Spawns a smoke effect
     */
    public static void smoke(@NotNull Location location) {
        create(Particle.SMOKE)
                .count(10)
                .offset(0.3, 0.3, 0.3)
                .speed(0.02)
                .spawn(location);
    }

    /**
     * Spawns a large smoke effect
     */
    public static void largeSmoke(@NotNull Location location) {
        create(Particle.LARGE_SMOKE)
                .count(5)
                .offset(0.5, 0.5, 0.5)
                .speed(0.05)
                .spawn(location);
    }

    /**
     * Spawns an explosion effect
     */
    public static void explosion(@NotNull Location location) {
        create(Particle.EXPLOSION)
                .count(1)
                .spawn(location);
    }

    /**
     * Spawns sparkles/enchantment effect
     */
    public static void sparkle(@NotNull Location location) {
        create(Particle.ENCHANT)
                .count(20)
                .offset(0.5, 0.5, 0.5)
                .speed(1.0)
                .spawn(location);
    }

    /**
     * Spawns critical hit effect
     */
    public static void critical(@NotNull Location location) {
        create(Particle.CRIT)
                .count(10)
                .offset(0.3, 0.3, 0.3)
                .speed(0.1)
                .spawn(location);
    }

    /**
     * Spawns magic critical effect
     */
    public static void magicCritical(@NotNull Location location) {
        create(Particle.CRIT)
                .count(10)
                .offset(0.3, 0.3, 0.3)
                .speed(0.1)
                .spawn(location);
    }

    /**
     * Spawns a portal effect
     */
    public static void portal(@NotNull Location location) {
        create(Particle.PORTAL)
                .count(30)
                .offset(0.5, 0.5, 0.5)
                .speed(1.0)
                .spawn(location);
    }

    /**
     * Spawns a spell effect
     */
    public static void spell(@NotNull Location location) {
        create(Particle.WITCH)
                .count(20)
                .offset(0.5, 0.5, 0.5)
                .spawn(location);
    }

    /**
     * Spawns a witch spell effect
     */
    public static void witchSpell(@NotNull Location location) {
        create(Particle.WITCH)
                .count(15)
                .offset(0.5, 0.5, 0.5)
                .spawn(location);
    }

    /**
     * Spawns note particles
     */
    public static void note(@NotNull Location location) {
        create(Particle.NOTE)
                .count(5)
                .offset(0.5, 0.5, 0.5)
                .spawn(location);
    }

    /**
     * Spawns villager happy particles
     */
    public static void happy(@NotNull Location location) {
        create(Particle.HAPPY_VILLAGER)
                .count(10)
                .offset(0.5, 0.5, 0.5)
                .spawn(location);
    }

    /**
     * Spawns villager angry particles
     */
    public static void angry(@NotNull Location location) {
        create(Particle.ANGRY_VILLAGER)
                .count(5)
                .offset(0.5, 0.5, 0.5)
                .spawn(location);
    }

    /**
     * Spawns redstone dust with custom color
     */
    public static void coloredDust(@NotNull Location location, @NotNull Color color, int count) {
        create(Particle.DUST)
                .color(color, 1.0f)
                .count(count)
                .offset(0.3, 0.3, 0.3)
                .spawn(location);
    }

    /**
     * Spawns a circle of particles
     */
    public static void circle(@NotNull Location center, @NotNull Particle particle,
                              double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location point = center.clone().add(x, 0, z);
            create(particle).count(1).spawn(point);
        }
    }

    /**
     * Spawns a sphere of particles
     */
    public static void sphere(@NotNull Location center, @NotNull Particle particle,
                              double radius, int density) {
        for (int i = 0; i < density; i++) {
            double phi = Math.random() * 2 * Math.PI;
            double theta = Math.random() * Math.PI;

            double x = radius * Math.sin(theta) * Math.cos(phi);
            double y = radius * Math.sin(theta) * Math.sin(phi);
            double z = radius * Math.cos(theta);

            Location point = center.clone().add(x, y, z);
            create(particle).count(1).spawn(point);
        }
    }

    /**
     * Spawns a line of particles between two locations
     */
    public static void line(@NotNull Location start, @NotNull Location end,
                            @NotNull Particle particle, double spacing) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        for (double i = 0; i < distance; i += spacing) {
            Location point = start.clone().add(direction.clone().multiply(i));
            create(particle).count(1).spawn(point);
        }
    }
}