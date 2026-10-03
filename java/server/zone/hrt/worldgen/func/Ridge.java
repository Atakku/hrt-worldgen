// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import java.util.List;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;
import zone.hrt.worldgen.Util;

public record Ridge(DensityFunction edgeNoise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Ridge> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("edge_noise").forGetter(Ridge::edgeNoise))
          .apply(instance, Ridge::new)));


  private static final CubicSpline<Float, ToFloatFunction<Float>> SPLINE;

  static {
    CubicSpline.Builder<Float, ToFloatFunction<Float>> spline = CubicSpline.builder(ToFloatFunction.IDENTITY);

    spline = spline.addPoint(-Worldgen.RIVER, -0.08f, 0f);
    spline = spline.addPoint(Worldgen.RIVER, 0.08f, 0f);
    spline = spline.addPoint(Worldgen.RIVER + Worldgen.BANK, 0.08f, 0f);
    spline = spline.addPoint((float) (Worldgen.CELL_SIZE / 3), 0.11f, 0f);

    SPLINE = spline.build();
  }

  public double compute(FunctionContext pos) {
    if (Util.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    Vec3 samplePos = Util.getSamplePos(edgeNoise, pos);
    List<Vec3> positions = Util.getNearestPoints(pos, samplePos).toList();

    Vec3 a = positions.get(0);
    double edgeDist = Util.minEdge(samplePos, positions);
    return Math.copySign(SPLINE.apply((float) edgeDist), a.y);
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Ridge(edgeNoise.mapAll(visitor)));
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
