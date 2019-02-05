package com.jedk1.jedcore.collision;

import com.jedk1.jedcore.JedCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AABB implements Collider {
    public static AABB PlayerBounds = new AABB(new Vector(-0.3, 0.0, -0.3), new Vector(0.3, 1.8, 0.3));
    public static AABB BlockBounds = new AABB(new Vector(0.0, 0.0, 0.0), new Vector(1.0, 1.0, 1.0));

    private static Class<?> CraftWorld, CraftEntity, World, AxisAlignedBB, Block, BlockPosition, IBlockData, Entity, VoxelShape;
    private static Method getHandle, getEntityHandle, getType, getBlock, getBlockBoundingBox, getBoundingBox, getVoxelShape, isEmptyBounds;
    private static Field minXField, minYField, minZField, maxXField, maxYField, maxZField;
    private static Constructor<?> bpConstructor;
    private static int serverVersion;

    private Vector min = null;
    private Vector max = null;

    static {
        serverVersion = 9;

        try {
            serverVersion = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().substring(23).split("_")[1]);
        } catch (Exception e) {

        }

        if (!setupReflection()) {
            JedCore.log.severe("Failed to setup AABB reflection.");
        }
    }

    public AABB(Block block) {
        this.min = min(block);
        this.max = max(block);
    }

    public AABB(Entity entity) {
        this.min = min(entity);
        this.max = max(entity);

        if (this.min != null) {
            this.min = this.min.subtract(entity.getLocation().toVector());
        }

        if (this.max != null) {
            this.max = this.max.subtract(entity.getLocation().toVector());
        }
    }

    public AABB(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    public AABB at(Vector pos) {
        if (min == null || max == null) return new AABB(null, null);

        return new AABB(min.clone().add(pos), max.clone().add(pos));
    }

    public AABB at(Location location) {
        if (min == null || max == null) return new AABB(null, null);

        return at(location.toVector());
    }

    public AABB grow(double x, double y, double z) {
        Vector change = new Vector(x, y, z);

        return new AABB(min.clone().subtract(change), max.clone().add(change));
    }

    public AABB scale(double x, double y, double z) {
        Vector extents = getHalfExtents();
        Vector newExtents = extents.clone().multiply(new Vector(x, y, z));

        Vector diff = newExtents.clone().subtract(extents);
        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public AABB scale(double amount) {
        Vector extents = getHalfExtents();
        Vector newExtents = extents.clone().multiply(amount);

        Vector diff = newExtents.clone().subtract(extents);
        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public Vector min() {
        return this.min;
    }

    public Vector max() {
        return this.max;
    }

    public Vector mid() {
        return this.min.clone().add(this.max().clone().subtract(this.min()).multiply(0.5));
    }

    public boolean contains(Vector test) {
        if (min == null || max == null) return false;

        return (test.getX() >= min.getX() && test.getX() <= max.getX()) &&
                (test.getY() >= min.getY() && test.getY() <= max.getY()) &&
                (test.getZ() >= min.getZ() && test.getZ() <= max.getZ());
    }

    public Optional<Double> intersects(Ray ray) {
        if (min == null || max == null) return Optional.empty();

        double t1 = (min.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();
        double t2 = (max.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();

        double t3 = (min.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();
        double t4 = (max.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();

        double t5 = (min.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();
        double t6 = (max.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax) {
            return Optional.empty();
        }

        return Optional.of(tmin);
    }

    public boolean intersects(AABB other) {
        if (min == null || max == null || other.min == null || other.max == null) {
            return false;
        }

        return (max.getX() > other.min.getX() &&
                min.getX() < other.max.getX() &&
                max.getY() > other.min.getY() &&
                min.getY() < other.max.getY() &&
                max.getZ() > other.min.getZ() &&
                min.getZ() < other.max.getZ());
    }

    public boolean intersects(Sphere sphere) {
        return sphere.intersects(this);
    }

    @Override
    public Vector getPosition() {
        return mid();
    }

    @Override
    public Vector getHalfExtents() {
        Vector half = max.clone().subtract(min).multiply(0.5);
        // Return a vector of half extents that reach from mid to box sides.
        return new Vector(Math.abs(half.getX()), Math.abs(half.getY()), Math.abs(half.getZ()));
    }

    private Vector min(Entity entity) {
        try {
            Object handle = getEntityHandle.invoke(CraftEntity.cast(entity));
            Object aabb = getBoundingBox.invoke(handle);

            if (aabb == null) return null;

            double a = (double)minXField.get(aabb);
            double b = (double)minYField.get(aabb);
            double c = (double)minZField.get(aabb);

            return new Vector(a, b, c);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Vector max(Entity entity) {
        try {
            Object handle = getEntityHandle.invoke(CraftEntity.cast(entity));
            Object aabb = getBoundingBox.invoke(handle);

            if (aabb == null) return null;

            double d = (double)maxXField.get(aabb);
            double e = (double)maxYField.get(aabb);
            double f = (double)maxZField.get(aabb);

            return new Vector(d, e, f);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Vector min(Block block) {
        Object aabb = getAABB(block);
        if (aabb == null) return null;

        try {
            double a = (double)minXField.get(aabb);
            double b = (double)minYField.get(aabb);
            double c = (double)minZField.get(aabb);

            return new Vector(a, b, c);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Vector max(Block block) {
        Object aabb = getAABB(block);
        if (aabb == null) return null;

        try {
            double d = (double)maxXField.get(aabb);
            double e = (double)maxYField.get(aabb);
            double f = (double)maxZField.get(aabb);

            return new Vector(d, e, f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getAABB(Block block) {
        if (serverVersion < 13) {
            try {
                Object bp = bpConstructor.newInstance(block.getX(), block.getY(), block.getZ());
                Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
                Object blockData = getType.invoke(world, BlockPosition.cast(bp));
                Object blockNative = getBlock.invoke(blockData);

                return getBlockBoundingBox.invoke(blockNative, IBlockData.cast(blockData), World.cast(world), BlockPosition.cast(bp));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Object bp = bpConstructor.newInstance(block.getX(), block.getY(), block.getZ());
                Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
                Object blockData = getType.invoke(world, BlockPosition.cast(bp));
                Object blockNative = getBlock.invoke(blockData);
                Object voxelShape = getVoxelShape.invoke(blockNative, IBlockData.cast(blockData), World.cast(world), BlockPosition.cast(bp));
                Object emptyBounds = isEmptyBounds.invoke(voxelShape);

                if (emptyBounds != null && !(Boolean)emptyBounds) {
                    return getBlockBoundingBox.invoke(voxelShape);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static boolean setupReflection() {
        CraftWorld = getNMSClass("org.bukkit.craftbukkit.%s.CraftWorld");
        CraftEntity = getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
        World = getNMSClass("net.minecraft.server.%s.World");
        AxisAlignedBB = getNMSClass("net.minecraft.server.%s.AxisAlignedBB");
        Block = getNMSClass("net.minecraft.server.%s.Block");
        BlockPosition = getNMSClass("net.minecraft.server.%s.BlockPosition");
        IBlockData = getNMSClass("net.minecraft.server.%s.IBlockData");
        Entity = getNMSClass("net.minecraft.server.%s.Entity");
        VoxelShape = getNMSClass("net.minecraft.server.%s.VoxelShape");
        Class<?> IBlockAccess = getNMSClass("net.minecraft.server.%s.IBlockAccess");

        try {
            getHandle = CraftWorld.getDeclaredMethod("getHandle");
            getEntityHandle = CraftEntity.getDeclaredMethod("getHandle");
            getType = World.getDeclaredMethod("getType", BlockPosition);
            getBlock = IBlockData.getDeclaredMethod("getBlock");
            bpConstructor = BlockPosition.getConstructor(int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }

        try {
            if (serverVersion < 13) {
                getBlockBoundingBox = Block.getDeclaredMethod("a", IBlockData, serverVersion >= 11 ? IBlockAccess : World, BlockPosition);
                getBoundingBox = Entity.getDeclaredMethod("getBoundingBox");
            } else {
                getVoxelShape = Block.getDeclaredMethod("a", IBlockData, IBlockAccess, BlockPosition);

                try {
                    getBlockBoundingBox = VoxelShape.getDeclaredMethod("getBoundingBox");
                } catch (NoSuchMethodException e) {
                    // Try to get the old method from before the 1.13.2 Spigot patch.
                    getBlockBoundingBox = VoxelShape.getDeclaredMethod("a");
                }

                getBoundingBox = Entity.getDeclaredMethod("getBoundingBox");

                try {
                    isEmptyBounds = VoxelShape.getDeclaredMethod("b");
                } catch (NoSuchMethodException e) {
                    isEmptyBounds = VoxelShape.getDeclaredMethod("isEmpty");
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }

        try {
            minXField = AxisAlignedBB.getField("a");
            minYField = AxisAlignedBB.getField("b");
            minZField = AxisAlignedBB.getField("c");
            maxXField = AxisAlignedBB.getField("d");
            maxYField = AxisAlignedBB.getField("e");
            maxZField = AxisAlignedBB.getField("f");
        } catch (NoSuchFieldException e) {
            try {
                minXField = AxisAlignedBB.getField("minX");
                minYField = AxisAlignedBB.getField("minY");
                minZField = AxisAlignedBB.getField("minZ");
                maxXField = AxisAlignedBB.getField("maxX");
                maxYField = AxisAlignedBB.getField("maxY");
                maxZField = AxisAlignedBB.getField("maxZ");
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private static Class<?> getNMSClass(String nmsClass) {
        String version = null;

        Pattern pattern = Pattern.compile("net\\.minecraft\\.(?:server)?\\.(v(?:\\d+_)+R\\d)");
        for (Package p : Package.getPackages()) {
            String name = p.getName();
            Matcher m = pattern.matcher(name);
            if (m.matches()) {
                version = m.group(1);
            }
        }

        if (version == null) return null;

        try {
            return Class.forName(String.format(nmsClass, version));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
