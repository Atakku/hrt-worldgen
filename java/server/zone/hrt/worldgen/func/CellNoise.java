// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;
import zone.hrt.worldgen.Util;

public record CellNoise(DensityFunction argument, DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<CellNoise> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(CellNoise::argument),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("edge_noise").forGetter(CellNoise::noise))
          .apply(instance, CellNoise::new)));

  public double compute(DensityFunction.FunctionContext pos) {
    if(Util.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    Vec3 samplePos = Util.getSamplePos(noise, pos);
    Vec3 a = Util.getNearestPoints(pos, samplePos).findFirst().get();
    return argument.compute(new SinglePointContext((int)a.x, 0, (int)a.z));
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new CellNoise(argument.mapAll(visitor), noise.mapAll(visitor)));
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
