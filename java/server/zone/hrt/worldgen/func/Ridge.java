// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;

public record Ridge(DensityFunction temperature, DensityFunction vegetation) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Ridge> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("temperature").forGetter(Ridge::temperature),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("vegetation").forGetter(Ridge::vegetation))
          .apply(instance, Ridge::new)));

  private static final CubicSpline<Float, ToFloatFunction<Float>> TEM_SPLINE = buildSpline(
      new float[] { -0.9f, -0.48f, -0.15f, 0.2f, 0.58f, 0.95f});
  private static final CubicSpline<Float, ToFloatFunction<Float>> VEG_SPLINE = buildSpline(
      new float[] { -0.5f, -0.35f, -0.1f, 0.1f, 0.3f, 0.5f });

  private static final float RADIUS = 0.0075f;

  private static final CubicSpline<Float, ToFloatFunction<Float>> buildSpline(float[] input) {
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

      float start = i > 0 ? Mth.lerp(0.66f, points[i - 1], point): point - (points[i + 1] - point);
      float end = i < points.length - 1 ? Mth.lerp(0.33f, point, points[i + 1]) : point + (point - points[i - 1]);

      spline = spline.addPoint(start, sign * -0.75f, 0f);
      spline = spline.addPoint(point - RADIUS * 1.25f, sign * -0.08f, 0f);
      spline = spline.addPoint(point - RADIUS, sign * -0.08f, 0f);
      //spline = spline.addPoint(point - RADIUS * 0.5f, sign * -0.035f, 0f);
      //spline = spline.addPoint(point, 0, 0f);
      //spline = spline.addPoint(point + RADIUS * 0.5f, sign * 0.035f, 0f);
      spline = spline.addPoint(point + RADIUS, sign * 0.08f, 0f);
      spline = spline.addPoint(point + RADIUS * 1.25f, sign * 0.08f, 0f);
      spline = spline.addPoint(end, sign * 0.75f, 0f);
    }
    return spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    int x = pos.blockX();
    int z = pos.blockZ();

    if (x >= Worldgen.R_BLOCKS || z >= Worldgen.R_BLOCKS || x < -Worldgen.R_BLOCKS || z < -Worldgen.R_BLOCKS)
      return 0;

    float tem = TEM_SPLINE.apply((float) this.temperature.compute(pos));
    float veg = VEG_SPLINE.apply((float) this.vegetation.compute(pos));
    return Math.copySign(Math.min(Math.abs(tem), Math.abs(veg)), tem * veg);
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Ridge(temperature.mapAll(visitor), vegetation.mapAll(visitor)));
  }

  @Override
  public double minValue() {
    return TEM_SPLINE.minValue() * VEG_SPLINE.minValue();
  }

  @Override
  public double maxValue() {
    return TEM_SPLINE.maxValue() * VEG_SPLINE.maxValue();
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC_HOLDER;
  }
}
