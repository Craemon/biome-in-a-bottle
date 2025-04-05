package com.craemon;

import com.craemon.utils.RaycastHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeBiomes {
    public static final String MOD_ID = "biome-in-a-bottle";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //Changes the biome for a specific chunk using the /fillbiome command.
    public static void changeBiomeChunk(ServerWorld serverWorld, ChunkPos chunkPos, String dimensionId, String biomeKey) {
        MinecraftServer server = serverWorld.getServer();
        // Get the server command source (execute the command as the server)
        ServerCommandSource commandSource = server.getCommandSource();


        // Get chunk boundaries
        BlockPos chunkStart = chunkPos.getStartPos();
        int minX = chunkStart.getX();
        int minZ = chunkStart.getZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        // Get Y boundaries for this dimension
        int minY = serverWorld.getBottomY(); // Lowest Y coordinate (e.g., -64 for Overworld)
        int maxY = minY + serverWorld.getDimension().logicalHeight() - 1; // Maximum Y coordinate

        int subChunkHeight = 16; // Process biome changes in 16-block-high slices (e.g., sub-chunks)

        // Iterate through the chunk's vertical space in 16-block slices
        for (int yStart = minY; yStart <= maxY; yStart += subChunkHeight) {
            int yEnd = Math.min(yStart + subChunkHeight - 1, maxY); // Ensure we don't go above the maximum Y level

            // Formulate the /fillbiome command
            String command = String.format("execute in %s run fillbiome %d %d %d %d %d %d %s",
                    dimensionId,
                    minX, yStart, minZ, // Start of the sub-chunk
                    maxX, yEnd, maxZ,// End of the sub-chunk
                    biomeKey // Selected biome
            );

            // Execute the command
            try {
                server.getCommandManager().executeWithPrefix(commandSource, command);
                LOGGER.info("Executed command: " + command);
            } catch (Exception e) {
                LOGGER.error("Failed to execute fillbiome command: " + command, e);
            }
        }
    }
    public static void changeBiomePaint(ServerWorld serverWorld, PlayerEntity player, String biomeKey, int size) {
        MinecraftServer server = serverWorld.getServer();
        // Get the server command source (execute the command as the server)
        ServerCommandSource commandSource = server.getCommandSource();

        BlockPos centerBlock = RaycastHelper.getBlockLooking(player);
        //ensure valid block was found:
        if (centerBlock == null) {
            return;
        }
        String command = getCommand(biomeKey, size, centerBlock);
        // Execute the command
        try {
            server.getCommandManager().executeWithPrefix(commandSource, command);
            LOGGER.info("Executed command: " + command);
        } catch (Exception e) {
            LOGGER.error("Failed to execute fillbiome command: " + command, e);
        }
    }

    private static @NotNull String getCommand(String biomeKey, int size, BlockPos centerBlock) {
        int maxX = centerBlock.getX() + size;
        int maxY = centerBlock.getY() + size;
        int maxZ = centerBlock.getZ() + size;
        int minX = centerBlock.getX() - size;
        int minY = centerBlock.getY() - size;
        int minZ = centerBlock.getZ() - size;

        //formulate command
        String command = String.format("fillbiome %d %d %d %d %d %d %s",
                minX, minY, minZ, // Start position
                maxX, maxY, maxZ, // End position
                biomeKey // Target biome
        );
        return command;
    }

}
