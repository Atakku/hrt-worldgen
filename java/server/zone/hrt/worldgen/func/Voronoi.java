// Copyright 2026 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package zone.hrt.worldgen.func;

import java.util.Comparator;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import zone.hrt.worldgen.Worldgen;

public record Voronoi(DensityFunction argument, DensityFunction noise) implements DensityFunction.SimpleFunction {
  public static final KeyDispatchDataCodec<Voronoi> CODEC_HOLDER = KeyDispatchDataCodec
      .of(RecordCodecBuilder.mapCodec(instance -> instance.group(
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(Voronoi::argument),
          DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Voronoi::noise))
          .apply(instance, Voronoi::new)));

  public double compute(DensityFunction.FunctionContext pos) {
    int x = pos.blockX();
    int z = pos.blockZ();

    if (x >= Worldgen.R_BLOCKS || z >= Worldgen.R_BLOCKS || x < -Worldgen.R_BLOCKS || z < -Worldgen.R_BLOCKS)
      return 0;

    double nx = this.noise.compute(pos);
    double nz = this.noise.compute(new SinglePointContext(pos.blockX(), pos.blockY() + 10000, pos.blockZ()));
    Vec3 samplePos = new Vec3(x + nx, 0, z + nz);

    Vec3 rp = Worldgen.getClosestPoints(x, z).stream().min(Comparator.comparing(samplePos::distanceToSqr)).get();
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
