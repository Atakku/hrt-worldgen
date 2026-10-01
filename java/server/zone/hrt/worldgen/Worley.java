// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext;
import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext;
import net.minecraft.world.phys.Vec3;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public final class Worley {
  public static final float D = 1024f;
  public static final HashFunction MUR = Hashing.murmur3_32_fixed(0);

  public static final Vec3 getSamplePos(DensityFunction noise, FunctionContext pos) {
    double nx = noise.compute(pos);
    double nz = noise.compute(new SinglePointContext(pos.blockX(), pos.blockY() + 10000, pos.blockZ()));
    return new Vec3(pos.blockX() + nx * D, 0, pos.blockZ() + nz * D);
  }

  public static final Stream<Vec3> getNearestPoints(FunctionContext pos, Vec3 samplePos) {
    return getAreaPoints(pos.blockX(), pos.blockZ()).stream()
        .sorted(Comparator.comparing(samplePos::distanceToSqr));
  }

  public static final List<Vec3> getAreaPoints(int x, int z) {
    int xd = (int) Math.round(x / D);
    int zd = (int) Math.round(z / D);

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
    return new Vec3(sample(cx, cz) * D, Math.abs(cx + cz) % 2 - 0.5, sample(cz, cx) * D);
  }

  public static final float sample(int input, int seed) {
    return ((((float) Integer.toUnsignedLong(MUR.hashInt(input + seed * 12345).asInt())) / (1L << 32) - 0.5f) * 0.9f
        + input);
  }
}
