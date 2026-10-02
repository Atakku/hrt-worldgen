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
import net.minecraft.world.level.levelgen.DensityFunctions.Spline;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;
import zone.hrt.worldgen.Worley;

public record Ridge(DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Ridge> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Ridge::noise))
          .apply(instance, Ridge::new)));

  private static final int SIZE = 40;
  private static final int BANK = 10;
  private static final int THRES = 100;

  private static final CubicSpline<Float, ToFloatFunction<Float>> SPLINE;

  static {
    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);

    spline = spline.addPoint(-SIZE, -0.08f, 0f);
    spline = spline.addPoint(SIZE, 0.08f, 0f);
    spline = spline.addPoint(SIZE + BANK, 0.08f, 0f);
    spline = spline.addPoint(300, 0.45f, 0f);

    SPLINE = spline.build();
  }

  public double compute(DensityFunction.FunctionContext pos) {
    if(Worldgen.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    Vec3 samplePos = Worley.getSamplePos(noise, pos);
    List<Vec3> positions = Worley.getNearestPoints(pos, samplePos).toList();

    Vec3 a = positions.get(0);
    double riverDist = positions.stream().skip(1).map(i -> edgeDist(samplePos, a, i)).min(Double::compare).get();

    //double landDist = positions.stream().skip(1).map(i -> samplePos.distanceTo(i)).limit(3).mapToDouble(Double::doubleValue).average().orElse(0);
    double landDist = Math.abs(samplePos.distanceTo(a) - samplePos.distanceTo(a));

    double dist = Mth.lerp(Math.clamp((riverDist - (SIZE + BANK)) / THRES, 0, 1), Math.min(riverDist, SIZE + BANK), Math.max(SIZE + BANK, landDist));
    return Math.copySign(SPLINE.apply((float) dist), a.y);
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
