package io.github.abdullahcxd.soliditycore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with Locations
 */
public final class LocationUtils {

    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    private LocationUtils() {}

    /**
     * Serializes a location to a string (world,x,y,z,yaw,pitch)
     */
    public static @NotNull String serialize(@NotNull Location location) {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
            location.getWorld() != null ? location.getWorld().getName() : "world",
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }

    /**
     * Serializes a location to a simple string (world,x,y,z)
     */
    public static @NotNull String serializeSimple(@NotNull Location location) {
        return String.format("%s,%.2f,%.2f,%.2f",
            location.getWorld() != null ? location.getWorld().getName() : "world",
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }

    /**
     * Deserializes a location from a string
     */
    public static @Nullable Location deserialize(@NotNull String string) {
        try {
            String[] parts = string.split(",");
            if (parts.length < 4) return null;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            if (parts.length >= 6) {
                float yaw = Float.parseFloat(parts[4]);
                float pitch = Float.parseFloat(parts[5]);
                return new Location(world, x, y, z, yaw, pitch);
            }

            return new Location(world, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formats a location to a readable string
     */
    public static @NotNull String format(@NotNull Location location) {
        return String.format("X: %s, Y: %s, Z: %s in %s",
            FORMAT.format(location.getX()),
            FORMAT.format(location.getY()),
            FORMAT.format(location.getZ()),
            location.getWorld() != null ? location.getWorld().getName() : "unknown"
        );
    }

    /**
     * Formats a location with yaw and pitch
     */
    public static @NotNull String formatFull(@NotNull Location location) {
        return String.format("X: %s, Y: %s, Z: %s, Yaw: %s, Pitch: %s in %s",
            FORMAT.format(location.getX()),
            FORMAT.format(location.getY()),
            FORMAT.format(location.getZ()),
            FORMAT.format(location.getYaw()),
            FORMAT.format(location.getPitch()),
            location.getWorld() != null ? location.getWorld().getName() : "unknown"
        );
    }

    /**
     * Centers a location to the middle of a block
     */
    @Contract("_ -> new")
    public static @NotNull Location center(@NotNull Location location) {
        return new Location(
            location.getWorld(),
            location.getBlockX() + 0.5,
            location.getBlockY(),
            location.getBlockZ() + 0.5,
            location.getYaw(),
            location.getPitch()
        );
    }

    /**
     * Centers a location on all axes
     */
    @Contract("_ -> new")
    public static @NotNull Location centerAll(@NotNull Location location) {
        return new Location(
            location.getWorld(),
            location.getBlockX() + 0.5,
            location.getBlockY() + 0.5,
            location.getBlockZ() + 0.5,
            location.getYaw(),
            location.getPitch()
        );
    }

    /**
     * Gets the distance between two locations (2D, ignores Y)
     */
    public static double distance2D(@NotNull Location loc1, @NotNull Location loc2) {
        if (!isSameWorld(loc1, loc2)) return Double.MAX_VALUE;
        
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Checks if two locations are in the same world
     */
    public static boolean isSameWorld(@NotNull Location loc1, @NotNull Location loc2) {
        if (loc1.getWorld() == null || loc2.getWorld() == null) return false;
        return loc1.getWorld().equals(loc2.getWorld());
    }

    /**
     * Checks if a location is within a radius of another
     */
    public static boolean isWithinRadius(@NotNull Location center, @NotNull Location location, double radius) {
        if (!isSameWorld(center, location)) return false;
        return center.distanceSquared(location) <= radius * radius;
    }

    /**
     * Checks if a location is within a 2D radius (ignores Y)
     */
    public static boolean isWithinRadius2D(@NotNull Location center, @NotNull Location location, double radius) {
        if (!isSameWorld(center, location)) return false;
        return distance2D(center, location) <= radius;
    }

    /**
     * Checks if a location is within a cuboid region
     */
    public static boolean isInRegion(@NotNull Location location, @NotNull Location corner1, @NotNull Location corner2) {
        if (!isSameWorld(location, corner1) || !isSameWorld(location, corner2)) return false;

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return location.getX() >= minX && location.getX() <= maxX
            && location.getY() >= minY && location.getY() <= maxY
            && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    /**
     * Gets the highest block at a location
     */
    public static @NotNull Location getHighestBlock(@NotNull Location location) {
        if (location.getWorld() == null) return location;
        
        Block highest = location.getWorld().getHighestBlockAt(location);
        return highest.getLocation().add(0, 1, 0);
    }

    /**
     * Gets a safe spawn location (on solid ground)
     */
    public static @NotNull Location getSafeLocation(@NotNull Location location) {
        Location safe = getHighestBlock(location);
        safe.setYaw(location.getYaw());
        safe.setPitch(location.getPitch());
        return safe;
    }

    /**
     * Gets all blocks in a sphere around a location
     */
    public static @NotNull List<Block> getBlocksInRadius(@NotNull Location center, double radius) {
        List<Block> blocks = new ArrayList<>();
        if (center.getWorld() == null) return blocks;

        int radiusInt = (int) Math.ceil(radius);
        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (center.distance(loc) <= radius) {
                        blocks.add(loc.getBlock());
                    }
                }
            }
        }
        return blocks;
    }

    /**
     * Gets all entities within a radius of a location
     */
    public static @NotNull List<Entity> getEntitiesInRadius(@NotNull Location center, double radius) {
        if (center.getWorld() == null) return new ArrayList<>();
        return new ArrayList<>(center.getWorld().getNearbyEntities(center, radius, radius, radius));
    }

    /**
     * Gets the direction vector from one location to another
     */
    public static @NotNull Vector getDirection(@NotNull Location from, @NotNull Location to) {
        return to.toVector().subtract(from.toVector()).normalize();
    }

    /**
     * Rotates a location around a point
     */
    @Contract("_, _, _ -> new")
    public static @NotNull Location rotateAround(@NotNull Location location, @NotNull Location center, double angle) {
        double cos = Math.cos(Math.toRadians(angle));
        double sin = Math.sin(Math.toRadians(angle));

        double x = location.getX() - center.getX();
        double z = location.getZ() - center.getZ();

        double newX = x * cos - z * sin;
        double newZ = x * sin + z * cos;

        return new Location(
            location.getWorld(),
            center.getX() + newX,
            location.getY(),
            center.getZ() + newZ,
            location.getYaw(),
            location.getPitch()
        );
    }

    /**
     * Gets a random location within a radius
     */
    @Contract("_, _ -> new")
    public static @NotNull Location getRandomLocation(@NotNull Location center, double radius) {
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * radius;
        
        double x = center.getX() + distance * Math.cos(angle);
        double z = center.getZ() + distance * Math.sin(angle);
        
        return new Location(center.getWorld(), x, center.getY(), z);
    }

    /**
     * Gets locations in a circle around a center point
     */
    public static @NotNull List<Location> getCircle(@NotNull Location center, double radius, int points) {
        List<Location> locations = new ArrayList<>();
        
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            locations.add(new Location(center.getWorld(), x, center.getY(), z));
        }
        
        return locations;
    }

    /**
     * Gets locations in a sphere around a center point
     */
    public static @NotNull List<Location> getSphere(@NotNull Location center, double radius, int density) {
        List<Location> locations = new ArrayList<>();
        
        for (int i = 0; i < density; i++) {
            double phi = Math.random() * 2 * Math.PI;
            double theta = Math.random() * Math.PI;
            
            double x = center.getX() + radius * Math.sin(theta) * Math.cos(phi);
            double y = center.getY() + radius * Math.sin(theta) * Math.sin(phi);
            double z = center.getZ() + radius * Math.cos(theta);
            
            locations.add(new Location(center.getWorld(), x, y, z));
        }
        
        return locations;
    }

    /**
     * Checks if a location is safe to teleport to (not in lava, void, etc.)
     */
    public static boolean isSafe(@NotNull Location location) {
        if (location.getWorld() == null) return false;
        if (location.getY() < 0) return false;
        
        Block block = location.getBlock();
        Block below = block.getRelative(0, -1, 0);
        Block above = block.getRelative(0, 1, 0);
        
        return !block.isLiquid() 
            && !above.isLiquid()
            && below.getType().isSolid()
            && !block.getType().isSolid();
    }

    /**
     * Clones a location without world reference
     */
    @Contract("_ -> new")
    public static @NotNull Location cloneWithoutWorld(@NotNull Location location) {
        return new Location(
            null,
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }

    /**
     * Compares two locations (ignoring yaw/pitch)
     */
    public static boolean equals(@NotNull Location loc1, @NotNull Location loc2) {
        return isSameWorld(loc1, loc2)
            && loc1.getBlockX() == loc2.getBlockX()
            && loc1.getBlockY() == loc2.getBlockY()
            && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Compares two locations exactly (including yaw/pitch)
     */
    public static boolean equalsExact(@NotNull Location loc1, @NotNull Location loc2) {
        return equals(loc1, loc2)
            && Math.abs(loc1.getYaw() - loc2.getYaw()) < 0.01
            && Math.abs(loc1.getPitch() - loc2.getPitch()) < 0.01;
    }
}