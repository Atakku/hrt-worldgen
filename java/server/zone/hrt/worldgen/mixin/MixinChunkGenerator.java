package zone.hrt.worldgen.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import zone.hrt.worldgen.Worldgen;

@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {
  @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/level/chunk/ChunkGenerator;createStructures(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;)V")
  private void createStructures(RegistryAccess drm, ChunkGeneratorStructureState spc, StructureManager sa,
      ChunkAccess c, StructureTemplateManager stm, CallbackInfo ci) {
    if (Worldgen.isOutside(c.getPos(), Worldgen.R_CHUNKS_SOFT)) {
      ci.cancel();
    }
  }
}
