package com.craemon.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;

//may be all redundant
public class RaycastHelper {
    public static BlockPos getBlockLooking(Player player) {
        BlockHitResult hitResult = (BlockHitResult) player.pick(20, 0.0F, false);
        return hitResult.getBlockPos();
    }
}
