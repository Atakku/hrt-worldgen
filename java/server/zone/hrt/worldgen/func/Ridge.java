// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import java.util.Comparator;
import java.util.List;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec2;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;

public record Ridge(DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Ridge> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Ridge::noise))
          .apply(instance, Ridge::new)));

  private static final CubicSpline<Float, ToFloatFunction<Float>> TEM_SPLINE = buildSpline(
      new float[] { -0.9f, -0.48f, -0.15f, 0.2f, 0.58f, 0.95f }, 0.0075f);
  private static final CubicSpline<Float, ToFloatFunction<Float>> VEG_SPLINE = buildSpline(
      new float[] { -0.5f, -0.35f, -0.1f, 0.1f, 0.3f, 0.5f }, 0.0075f);

  private static final float THRESHOLD = 0.08f;

  private static final CubicSpline<Float, ToFloatFunction<Float>> buildSpline(float[] input, float radius) {
    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);
    float[] points = new float[input.length * 2 - 1];
    int resIndex = 0;
    for (int i = 0; i < input.length - 1; i++) {
      points[resIndex++] = input[i];
      points[resIndex++] = (input[i] + input[i + 1]) / 2;
    }
    points[resIndex] = input[input.length - 1];

    for (int i = 0; i < points.length; i++) {
      float point = points[i];
      int sign = (i % 2 == 0) ? 1 : -1;

      float start = i > 0 ? Mth.lerp(0.55f, points[i - 1], point)
          : point -
              (points[i + 1] - point);
      float end = i < points.length - 1 ? Mth.lerp(0.45f, point, points[i + 1]) : point + (point - points[i - 1]);

      spline = spline.addPoint(start, sign * -1.5f, 0f);
      spline = spline.addPoint(point - radius * 1.25f, sign * -THRESHOLD, 0f);
      spline = spline.addPoint(point - radius, sign * -THRESHOLD, 0f);
      spline = spline.addPoint(point + radius, sign * THRESHOLD, 0f);
      spline = spline.addPoint(point + radius * 1.25f, sign * THRESHOLD, 0f);
      spline = spline.addPoint(end, sign * 1.5f, 0f);
    }
    return spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    int x = pos.blockX();
    int z = pos.blockZ();

    if (x >= Worldgen.R_BLOCKS || z >= Worldgen.R_BLOCKS || x < -Worldgen.R_BLOCKS || z < -Worldgen.R_BLOCKS)
      return 0;

    Vec2 samplePos = new Vec2(pos.blockX(), pos.blockZ());
    List<Vec2> positions = Worldgen.getClosestPoints(x, z).stream()
        .sorted(Comparator.comparing(p -> Worldgen.distManhattan(p, samplePos))).limit(2)
        .toList();

    Vec2 a = positions.getFirst();
    Vec2 b = positions.getLast();
    // return (Worldgen.distManhattan(b, samplePos) - Worldgen.distManhattan(a,
    // samplePos)) / 256.;
    return (Math.sqrt(Worldgen.distManhattan(b, samplePos)) - Math.sqrt(Worldgen.distManhattan(a, samplePos))) / 20.;

    // Vec3i realPos =
    // Worldgen.POINTS.stream().min(Comparator.comparing(samplePos::distSqr)).get();

    // float tem = TEM_SPLINE.apply((float) temperature.compute(pos));
    // float veg = VEG_SPLINE.apply((float) vegetation.compute(pos));

    // float river = Math.min(Math.abs(tem), Math.abs(veg));
    // double land = Math.min(Math.abs(noise.compute(pos)), THRESHOLD);
    // return Math.copySign(Math.min(river, land), tem * veg);
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
    return Math.min(TEM_SPLINE.minValue(), VEG_SPLINE.minValue());
  }

  @Override
  public double maxValue() {
    return Math.max(TEM_SPLINE.maxValue(), VEG_SPLINE.maxValue());
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC_HOLDER;
  }
}
