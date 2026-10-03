// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Util;
import zone.hrt.worldgen.Worldgen;

public record Vegetation(DensityFunction temperature, DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Vegetation> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("temperature").forGetter(Vegetation::temperature),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Vegetation::noise))
          .apply(instance, Vegetation::new)));

  public double compute(DensityFunction.FunctionContext pos) {
    if (Util.isOutside(pos, Worldgen.R_BLOCKS))
      return 0;

    double temp = this.temperature.compute(pos);
    double noise = this.noise.compute(pos);
    double raw = (pos.blockX() * 1.5 - pos.blockZ() * 0.5) / (2d * Worldgen.CR_BLOCKS);

    return Mth.clamp(Mth.lerp(temp * temp, raw / 1.75, raw) + noise, -1, 1);
  }

  @Override
  public void fillArray(double[] doubles, ContextProvider ctx) {
    ctx.fillAllDirectly(doubles, this);
  }

  @Override
  public DensityFunction mapAll(Visitor visitor) {
    return visitor.apply(new Vegetation(temperature.mapAll(visitor), noise.mapAll(visitor)));
  }

  @Override
  public double minValue() {
    return -1;
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
