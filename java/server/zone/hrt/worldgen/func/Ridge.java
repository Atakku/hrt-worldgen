// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import java.util.Comparator;
import java.util.List;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;

public record Ridge(DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Ridge> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Ridge::noise))
          .apply(instance, Ridge::new)));

  private static final int SIZE = 40;
  private static final CubicSpline<Float, ToFloatFunction<Float>> SPLINE;

  static {
    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);

    spline = spline.addPoint(-SIZE, -0.08f, 0f);
    spline = spline.addPoint(SIZE, 0.08f, 0f);
    spline = spline.addPoint(SIZE * 1.25f, 0.08f, 0f);

    SPLINE = spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    int x = pos.blockX();
    int z = pos.blockZ();

    if (x >= Worldgen.R_BLOCKS || z >= Worldgen.R_BLOCKS || x < -Worldgen.R_BLOCKS || z < -Worldgen.R_BLOCKS)
      return 0;

    double nx = this.noise.compute(pos);
    double nz = this.noise.compute(new SinglePointContext(pos.blockX(), pos.blockY() + 10000, pos.blockZ()));
    Vec3 samplePos = new Vec3(x + nx, 0, z + nz);

    List<Vec3> positions = Worldgen.getClosestPoints(x, z).stream()
        .sorted(Comparator.comparing(samplePos::distanceToSqr)).limit(2)
        .toList();

    Vec3 a = positions.getFirst();
    Vec3 b = positions.getLast();
    Vec3 c = a.add(b).scale(0.5);

    double da = a.z - b.z;
    double db = b.x - a.x;
    double dc = da * c.z - db * c.x;

    double dist = Math.abs(db * samplePos.x - da * samplePos.z + dc) / Math.sqrt(da * da + db * db);
    return Math.copySign(SPLINE.apply((float) dist), a.y);

    //Math.copySign(Mth.clamp((samplePos.distanceTo(a) - samplePos.distanceTo(b)) / 512., -1, 1), a.y);
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Ridge(noise.mapAll(visitor)));
  }

  @Override
  public double minValue() {
    return -SPLINE.maxValue();
  }

  @Override
  public double maxValue() {
    return SPLINE.maxValue();
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC_HOLDER;
  }
}
