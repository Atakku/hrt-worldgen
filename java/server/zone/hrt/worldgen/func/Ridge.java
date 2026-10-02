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
import zone.hrt.worldgen.Worley;

public record Ridge(DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Ridge> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Ridge::noise))
          .apply(instance, Ridge::new)));

  private static final int SIZE = 30;
  private static final int BANK = 5;

  private static final CubicSpline<Float, ToFloatFunction<Float>> SPLINE;

  static {
    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);

    spline = spline.addPoint(-SIZE, -0.08f, 0f);
    spline = spline.addPoint(SIZE, 0.08f, 0f);
    spline = spline.addPoint(SIZE + BANK, 0.08f, 0f);
    spline = spline.addPoint(SIZE + BANK * 3, 0.08f, 0f);

    SPLINE = spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    if (Worldgen.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    Vec3 samplePos = Worley.getSamplePos(noise, pos);
    List<Vec3> positions = Worley.getNearestPoints(pos, samplePos).toList();

    Vec3 a = positions.get(0);
    double edgeDist = positions.stream().skip(1).map(i -> edgeDist(samplePos, a, i)).min(Double::compare).get();
    double riverDist = Math.max(0, edgeDist - SIZE);
    double landDist = samplePos.distanceTo(a);
    // double landDist = samplePos.distanceTo(approximateCenter(positions));

    return Math.copySign(
        Mth.lerp(smoothstep(0.65f, 0.7f, (riverDist / (riverDist + landDist))), SPLINE.apply((float) edgeDist), 0.41f),
        a.y);
    // return Math.copySign(Mth.lerp(0, SPLINE.apply((float) edgeDist), 0.5f), a.y);
  }

  private static double smoothstep(double start, double end, double x) {
    return Math.clamp((x - start) / (end - start), 0.0, 1.0);
  }

  private static final double edgeDist(Vec3 pos, Vec3 a, Vec3 b) {
    Vec3 c = a.add(b).scale(0.5);

    double da = a.z - b.z;
    double db = b.x - a.x;
    double dc = da * c.z - db * c.x;

    return Math.abs(db * pos.x - da * pos.z + dc) / Math.sqrt(da * da + db * db);
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
