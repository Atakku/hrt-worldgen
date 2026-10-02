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

public record Erosion(DensityFunction temperature, DensityFunction noise, DensityFunction ridge)
    implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Erosion> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("temperature").forGetter(Erosion::temperature),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Erosion::noise),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("ridge").forGetter(Erosion::ridge))
          .apply(instance, Erosion::new)));

  private static final CubicSpline<Float, ToFloatFunction<Float>> MNT_SPLINE;
  static {
    float radius = 0.1f;

    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);
    for (float peak : new float[] { -0.45f, 0.55f }) {
      spline = spline.addPoint(peak - radius, 1.0f, 0f);
      spline = spline.addPoint(peak, 0f, 0f);
      spline = spline.addPoint(peak + radius, 1.0f, 0f);
    }
    MNT_SPLINE = spline.build();
  }

  private static final CubicSpline<Float, ToFloatFunction<Float>> RDG_SPLINE;
  static {
    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);
    spline = spline.addPoint(0.08f, 0.5f, 0f);
    spline = spline.addPoint(0.35f, 0.45f, 0f);
    spline = spline.addPoint(0.4f, 0.325f, 0f);
    RDG_SPLINE = spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    if (Worldgen.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    double mountain = MNT_SPLINE.apply((float) (temperature.compute(pos) + noise.compute(pos)));
    double ridge = RDG_SPLINE.apply((float) Math.abs(this.ridge.compute(pos)));

    return Mth.lerp(mountain, 0, ridge);
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Erosion(temperature.mapAll(visitor), noise.mapAll(visitor), ridge.mapAll(visitor)));
  }

  @Override
  public double minValue() {
    return MNT_SPLINE.minValue();
  }

  @Override
  public double maxValue() {
    return MNT_SPLINE.maxValue();
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC_HOLDER;
  }
}
