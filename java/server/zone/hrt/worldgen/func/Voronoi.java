// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;
import zone.hrt.worldgen.Worley;

public record Voronoi(DensityFunction argument, DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Voronoi> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(Voronoi::argument),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Voronoi::noise))
          .apply(instance, Voronoi::new)));

  public double compute(DensityFunction.FunctionContext pos) {
    if(Worldgen.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    Vec3 samplePos = Worley.getSamplePos(noise, pos);
    Vec3 rp = Worley.getNearestPoints(pos, samplePos).findFirst().get();
    return argument.compute(new SinglePointContext((int)rp.x, 0, (int)rp.z));
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Voronoi(argument.mapAll(visitor), noise.mapAll(visitor)));
  }

  @Override
  public double minValue() {
    return argument.minValue();
  }

  @Override
  public double maxValue() {
    return argument.maxValue();
  }

  @Override
  public KeyDispatchDataCodec<? extends DensityFunction> codec() {
    return CODEC_HOLDER;
  }
}
