package zone.hrt.worldgen.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import com.mojang.datafixers.util.Pair;
import dev.worldgen.lithostitched.api.worldgen.biomeinjector.BiomeInjector;
import dev.worldgen.lithostitched.impl.worldgen.biomeinjector.internal.InjectorBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import zone.hrt.worldgen.ReplacePoints;

@Mixin(InjectorBiomeSource.class)
public class MixinInjectorBiomeSource {
  @Shadow private Map<?, List<BiomeInjector>> injectorsByType;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @ModifyArg(at = @At(value = "INVOKE", target = "Ldev/worldgen/lithostitched/impl/worldgen/biomeinjector/AddPoints;apply(Ljava/util/ArrayList;Ljava/util/List;)V"), method = "applyInjectors(Ljava/util/Map;Ljava/util/Optional;Ljava/util/Map;Ldev/worldgen/lithostitched/api/worldgen/util/DensityFunctionWrapper;)V", remap = false, index = 0)
  private ArrayList<Pair<Climate.ParameterPoint, Holder<Biome>>> applyInjectors(
      ArrayList<Pair<Climate.ParameterPoint, Holder<Biome>>> modifiedParameters) {
    ReplacePoints.apply(modifiedParameters,
        (List) this.injectorsByType.getOrDefault(ReplacePoints.CODEC, new ArrayList()));
    return modifiedParameters;
  }
}
