package zone.hrt.worldgen.mixin;

import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import zone.hrt.worldgen.Worldgen;

@Mixin(NoiseBasedChunkGenerator.class)
public class MixinNoiseBasedChunkGenerator {
  @Inject(at = @At("HEAD"), cancellable = true, method = "doCreateBiomes(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)V")
  private void doCreateBiomes(Blender b, RandomState nc, StructureManager sa, ChunkAccess c,
      CallbackInfo ci) {
    if (Worldgen.isOutside(c.getPos(), Worldgen.R_CHUNKS)) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), cancellable = true, method = "applyCarvers(Lnet/minecraft/server/level/WorldGenRegion;JLnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;)V")
  public void applyCarvers(WorldGenRegion cr, long s, RandomState nc, BiomeManager ba, StructureManager sa,
      ChunkAccess c, GenerationStep.Carving cs, CallbackInfo ci) {
    if (Worldgen.isOutside(c.getPos(), Worldgen.R_CHUNKS)) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), cancellable = true, method = "buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/blending/Blender;)V")
  public void buildSurface(ChunkAccess c, WorldGenerationContext hc, RandomState nc, StructureManager sa,
      BiomeManager ba, Registry<Biome> br, Blender b, CallbackInfo ci) {
    if (Worldgen.isOutside(c.getPos(), Worldgen.R_CHUNKS)) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), cancellable = true, method = "spawnOriginalMobs(Lnet/minecraft/server/level/WorldGenRegion;)V")
  private void spawnOriginalMobs(WorldGenRegion r, CallbackInfo ci) {
    if (Worldgen.isOutside(r.getCenter(), Worldgen.R_CHUNKS)) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), cancellable = true, method = "doFill(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;II)Lnet/minecraft/world/level/chunk/ChunkAccess;")
  private void doFill(Blender b, StructureManager sa, RandomState nc, ChunkAccess c, int mY, int cY,
      CallbackInfoReturnable<ChunkAccess> cir) {
    ChunkPos p = c.getPos();
    if (Worldgen.isOutside(p, Worldgen.R_CHUNKS)) {
      cir.setReturnValue(c);
    }
  }
}
