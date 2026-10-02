// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;

public record Continents(DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Continents> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Continents::noise))
          .apply(instance, Continents::new)));

  public double compute(DensityFunction.FunctionContext pos) {
    if (Worldgen.isOutside(pos, Worldgen.R_BLOCKS))
      return 0.25;

    double noise = this.noise.compute(pos);
    double world = Mth.lerp(Math.clamp(edgeRatio(pos, 4096, Worldgen.R_BLOCKS - 2048) + noise, 0, 1), 1.7, 0);
    return Mth.lerp(edgeRatio(pos, 1536, Worldgen.R_BLOCKS - 256), world, 0.25);
  }

  private static final double edgeRatio(DensityFunction.FunctionContext pos, double size, double end) {
    double start = end - size;
    double edge = start - size;

    double distX = Math.min(Math.abs(pos.blockX()), end);
    double distZ = Math.min(Math.abs(pos.blockZ()), end);

    double edgeX = distX - edge;
    double edgeZ = distZ - edge;
    if (edgeX > 0 && edgeZ > 0) {
      double dist = Math.sqrt(edgeX * edgeX + edgeZ * edgeZ) - size;
      if (dist < 0)
        return 0;
      if (dist > size)
        return 1;
      return dist / size;
    }

    double point = Math.max(distX, distZ) - start;
    return Math.max(0, point) / size;
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Continents(noise.mapAll(visitor)));
  }

  @Override
  public double minValue() {
    return -1.2;
  }

  @Override
  public double maxValue() {
    return 1.2;
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC_HOLDER;
  }
}
