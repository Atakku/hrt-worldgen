// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import java.util.List;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;
import zone.hrt.worldgen.Util;

public record Erosion(DensityFunction temperature, DensityFunction edgeNoise, DensityFunction mountainNoise, DensityFunction plateauNoise)
    implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Erosion> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("temperature").forGetter(Erosion::temperature),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("edge_noise").forGetter(Erosion::edgeNoise),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("mountain_noise").forGetter(Erosion::mountainNoise),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("plateau_noise").forGetter(Erosion::plateauNoise))
          .apply(instance, Erosion::new)));

  private static final CubicSpline<Float, ToFloatFunction<Float>> MNT_SPLINE;
  static {
    float radius = 0.1f;

    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);
    for (float peak : new float[] { -0.45f, 0.55f }) {
      spline = spline.addPoint(peak - radius, 0.5f, 0f);
      spline = spline.addPoint(peak, 0f, 0f);
      spline = spline.addPoint(peak + radius, 0.5f, 0f);
    }
    MNT_SPLINE = spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    if (Util.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    Vec3 samplePos = Util.getSamplePos(edgeNoise, pos);
    List<Vec3> positions = Util.getNearestPoints(pos, samplePos).toList();
    Vec3 a = positions.get(0);

    double mountain = MNT_SPLINE.apply((float) (temperature.compute(pos) + mountainNoise.compute(pos)));
    double plateau = Math.abs(plateauNoise.compute(new SinglePointContext((int)a.x, 0, (int)a.z)));

    double edgeDist = Util.minEdge(samplePos, positions);
    double riverDist = Math.max(0, edgeDist - Worldgen.RIVER);
    double landDist = samplePos.distanceTo(a);

    double delta = Util.smoothstep(0.6f - plateau * 2, 0.7f, (riverDist / (riverDist + landDist)));
    return Math.min(mountain, Mth.lerp(delta, 0.5, 0.325 - plateau));
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Erosion(temperature.mapAll(visitor), edgeNoise.mapAll(visitor), mountainNoise.mapAll(visitor), plateauNoise.mapAll(visitor)));
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
