package net.pullolo.magicabilities.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GeneralMethods {

    public static List<Location> getCircle(final Location loc, final int radius, final int height, final boolean hollow, final boolean sphere, final int plusY) {
        final List<Location> circleblocks = new ArrayList<Location>();
        final int cx = loc.getBlockX();
        final int cy = loc.getBlockY();
        final int cz = loc.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = (sphere ? cy - radius : cy); y < (sphere ? cy + radius : cy + height); y++) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);

                    if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1))) {
                        final Location l = new Location(loc.getWorld(), x, y + plusY, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static Vector getDirection(final Location location, final Location destination) {
        double x1, y1, z1;
        double x0, y0, z0;

        x1 = destination.getX();
        y1 = destination.getY();
        z1 = destination.getZ();

        x0 = location.getX();
        y0 = location.getY();
        z0 = location.getZ();

        return new Vector(x1 - x0, y1 - y0, z1 - z0);
    }

    public static BlockFace getBlockFaceFromValue(final int xyz, final double value) {
        switch (xyz) {
            case 0:
                if (value > 0) {
                    return BlockFace.EAST;
                } else if (value < 0) {
                    return BlockFace.WEST;
                } else {
                    return BlockFace.SELF;
                }
            case 1:
                if (value > 0) {
                    return BlockFace.UP;
                } else if (value < 0) {
                    return BlockFace.DOWN;
                } else {
                    return BlockFace.SELF;
                }
            case 2:
                if (value > 0) {
                    return BlockFace.SOUTH;
                } else if (value < 0) {
                    return BlockFace.NORTH;
                } else {
                    return BlockFace.SELF;
                }
            default:
                return null;
        }
    }

    public static boolean isAir(final Material material) {
        return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR ||
                material == Material.LIGHT;
    }

    public static Vector rotateVector(Vector vector, double whatAngle) {
        double sin = Math.sin(Math.toRadians(whatAngle));
        double cos = Math.cos(Math.toRadians(whatAngle));
        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = vector.getX() * -sin + vector.getZ() * cos;

        return vector.clone().setX(x).setZ(z);
    }
}
