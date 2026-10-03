// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext;
import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext;
import net.minecraft.world.phys.Vec3;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public final class Util {
  private static final HashFunction MUR = Hashing.murmur3_32_fixed(0);

  public static final Vec3 getSamplePos(DensityFunction noise, FunctionContext pos) {
    double nx = noise.compute(new SinglePointContext(pos.blockX(), 0, pos.blockZ()));
    double nz = noise.compute(new SinglePointContext(pos.blockX(), 10000, pos.blockZ()));
    return new Vec3(pos.blockX() + nx * Worldgen.CELL_SIZE, 0, pos.blockZ() + nz * Worldgen.CELL_SIZE);
  }

  public static double smoothstep(double start, double end, double x) {
    return Math.clamp((x - start) / (end - start), 0.0, 1.0);
  }

  public static final double minEdge(Vec3 samplePos, List<Vec3> positions) {
    return positions.stream().skip(1).map(i -> Util.edgeDist(samplePos, positions.get(0), i)).min(Double::compare)
        .get();
  }

  public static final double edgeDist(Vec3 samplePos, Vec3 a, Vec3 b) {
    Vec3 c = a.add(b).scale(0.5);

    double da = a.z - b.z;
    double db = b.x - a.x;
    double dc = da * c.z - db * c.x;

    return Math.abs(db * samplePos.x - da * samplePos.z + dc) / Math.sqrt(da * da + db * db);
  }

  public static final Stream<Vec3> getNearestPoints(FunctionContext pos, Vec3 samplePos) {
    return getAreaPoints(pos.blockX(), pos.blockZ()).stream()
        .sorted(Comparator.comparing(samplePos::distanceToSqr));
  }

  public static final List<Vec3> getAreaPoints(int x, int z) {
    int xd = (int) Math.round(x / Worldgen.CELL_SIZE);
    int zd = (int) Math.round(z / Worldgen.CELL_SIZE);

    return List.of(
        getPoint(xd - 1, zd - 1),
        getPoint(xd - 1, zd),
        getPoint(xd - 1, zd + 1),
        getPoint(xd, zd - 1),
        getPoint(xd, zd),
        getPoint(xd, zd + 1),
        getPoint(xd + 1, zd - 1),
        getPoint(xd + 1, zd),
        getPoint(xd + 1, zd + 1));
  }

  public static final Vec3 getPoint(int cx, int cz) {
    double x = ((sample(cx, cz) - 0.5f) * 0.9f + cx) * Worldgen.CELL_SIZE;
    double z = ((sample(cz, cx) - 0.5f) * 0.9f + cz) * Worldgen.CELL_SIZE;
    return new Vec3(x, Math.abs(cx + cz) % 2 - 0.5, z);
  }

  public static final float sample(double a, double b) {
    return sample((int) a, (int) b);
  }

  public static final float sample(int a, int b) {
    long random = Integer.toUnsignedLong(MUR.hashInt(a + b * 12345).asInt());
    return ((float) random) / (1L << 32);
  }

  public static final boolean isOutside(ChunkPos p, int b) {
    return isOutside(p.x, p.z, b);
  }

  public static final boolean isOutside(DensityFunction.FunctionContext p, int b) {
    return isOutside(p.blockX(), p.blockZ(), b);
  }

  public static final boolean isOutside(int x, int z, int b) {
    return x >= b || z >= b || x < -b || z < -b;
  }
}
