// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EdgeRatio(DensityFunction size, DensityFunction end) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<EdgeRatio> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("size").forGetter(EdgeRatio::size),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("end").forGetter(EdgeRatio::end))
          .apply(instance, EdgeRatio::new)));

  public double compute(DensityFunction.FunctionContext pos) {
    double size = this.size.compute(pos);
    double end = this.end.compute(pos);

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
    return visitor.apply(new EdgeRatio(size.mapAll(visitor), end.mapAll(visitor)));
  }

  @Override
  public double minValue() {
    return 0;
  }

  @Override
  public double maxValue() {
    return 1;
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC_HOLDER;
  }
}