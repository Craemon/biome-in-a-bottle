package com.craemon.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

//may be all redundant
public class RaycastHelper {
    public static BlockPos getBlockLooking(PlayerEntity player) {
        BlockHitResult hitResult = (BlockHitResult) player.raycast(20, 0.0F, false);
        return hitResult.getBlockPos();
    }
}
