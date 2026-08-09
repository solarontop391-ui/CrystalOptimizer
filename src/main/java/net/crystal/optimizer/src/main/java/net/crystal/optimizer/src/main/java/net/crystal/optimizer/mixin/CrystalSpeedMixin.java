package net.crystal.optimizer.mixin;

import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AmethystClusterBlock.class)
public class CrystalSpeedMixin {
    @ModifyArg(
        method = "randomTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
        index = 2
    )
    private int speedUpGrowth(int flags) {
        return 3;
    }
}
