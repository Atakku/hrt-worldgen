// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;

public record Erosion(DensityFunction temperature) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Erosion> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("temperature").forGetter(Erosion::temperature))
          .apply(instance, Erosion::new)));

  private static final CubicSpline<Float, ToFloatFunction<Float>> MNT_SPLINE;

  static {
    float radius = 0.125f;

    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);
    for (float peak : new float[] { -0.48f, 0.58f }) {
      spline = spline.addPoint(peak - radius, 0.4f, 0f);
      spline = spline.addPoint(peak, 0f, 0f);
      spline = spline.addPoint(peak + radius, 0.4f, 0f);
    }
    MNT_SPLINE = spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    int x = pos.blockX();
    int z = pos.blockZ();

    if (x >= Worldgen.R_BLOCKS || z >= Worldgen.R_BLOCKS || x < -Worldgen.R_BLOCKS || z < -Worldgen.R_BLOCKS)
      return 0;

    return MNT_SPLINE.apply((float) this.temperature.compute(pos));
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Erosion(temperature.mapAll(visitor)));
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
