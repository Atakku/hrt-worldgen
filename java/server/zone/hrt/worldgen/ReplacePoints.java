package zone.hrt.worldgen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.worldgen.lithostitched.api.predicate.LoadPredicate;
import dev.worldgen.lithostitched.api.worldgen.biomeinjector.BiomeInjector;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ReplacePoints(Optional<LoadPredicate> predicate, ResourceKey<LevelStem> dimension, int priority, Holder<Biome> biome, List<Climate.ParameterPoint> points) implements BiomeInjector {
	public static final MapCodec<ReplacePoints> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		LoadPredicate.FIELD_CODEC.forGetter(ReplacePoints::predicate),
		BiomeInjector.DIMENSION_CODEC.forGetter(ReplacePoints::dimension),
		BiomeInjector.PRIORITY_CODEC.forGetter(ReplacePoints::priority),
		Biome.CODEC.fieldOf("biome").forGetter(ReplacePoints::biome),
		Climate.ParameterPoint.CODEC.listOf().fieldOf("points").forGetter(ReplacePoints::points)
	).apply(i, ReplacePoints::new));

	public static void apply(ArrayList<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters, List<BiomeInjector> injectors) {
		for (var injector : injectors) {
			ReplacePoints inj = (ReplacePoints)injector;
			parameters.removeIf(a -> a.getSecond() == inj.biome());
			parameters.addAll(inj.points().stream().map(i -> Pair.of(i, inj.biome())).toList());
		}
	}

	@Override
	public List<Holder<Biome>> possibleBiomes() {
		return List.of(biome);
	}

	@Override
	public MapCodec<? extends BiomeInjector> codec() {
		return CODEC;
	}
}