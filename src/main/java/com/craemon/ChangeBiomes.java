package com.craemon;

import com.craemon.utils.RaycastHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeBiomes {
    public static final String MOD_ID = "biome-in-a-bottle";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //Changes the biome for a specific chunk using the /fillbiome command.
    public static void changeBiomeChunk(ServerLevel serverWorld, ChunkPos chunkPos, String dimensionId, String biomeKey) {
        MinecraftServer server = serverWorld.getServer();
        // Get the server command source (execute the command as the server)
        CommandSourceStack commandSource = server.createCommandSourceStack();


        // Get chunk boundaries
        BlockPos chunkStart = chunkPos.getWorldPosition();
        int minX = chunkStart.getX();
        int minZ = chunkStart.getZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        // Get Y boundaries for this dimension
        int minY = serverWorld.getMinY(); // Lowest Y coordinate (e.g., -64 for Overworld)
        int maxY = minY + serverWorld.dimensionType().logicalHeight() - 1; // Maximum Y coordinate

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
                CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
                ParseResults<CommandSourceStack> results = dispatcher.parse(command, commandSource);
                dispatcher.execute(results);
                LOGGER.info("Executed command: " + command);
            } catch (Exception e) {
                LOGGER.error("Failed to execute fillbiome command: " + command, e);
            }
        }
    }
    //Changes the biome for a specific subchunk using the /fillbiome command.
    public static void changeBiomeSubchunk(ServerLevel serverWorld, ChunkPos chunkPos, int yStart, String dimensionId, String biomeKey) {
        MinecraftServer server = serverWorld.getServer();
        // Get the server command source (execute the command as the server)
        CommandSourceStack commandSource = server.createCommandSourceStack();

        // Get chunk boundaries
        BlockPos chunkStart = chunkPos.getWorldPosition();
        int minX = chunkStart.getX();
        int minZ = chunkStart.getZ();
        int minY = yStart;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        int maxY = minY + 15;

        // Formulate the /fillbiome command
        String command = String.format("execute in %s run fillbiome %d %d %d %d %d %d %s",
                dimensionId,
                minX, minY, minZ, // Start of the sub-chunk
                maxX, maxY, maxZ,// End of the sub-chunk
                biomeKey // Selected biome
        );

        // Execute the command
        try {
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
            ParseResults<CommandSourceStack> results = dispatcher.parse(command, commandSource);
            dispatcher.execute(results);
            LOGGER.info("Executed command: " + command);
        } catch (Exception e) {
            LOGGER.error("Failed to execute fillbiome command: " + command, e);
        }
    }
    public static void changeBiomePaint(ServerLevel serverWorld, Player player, String biomeKey, int size) {
        MinecraftServer server = serverWorld.getServer();
        // Get the server command source (execute the command as the server)
        CommandSourceStack commandSource = server.createCommandSourceStack();

        BlockPos centerBlock = RaycastHelper.getBlockLooking(player);
        //ensure valid block was found:
        if (centerBlock == null) {
            return;
        }
        String command = getCommand(biomeKey, size, centerBlock);
        // Execute the command
        try {
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
            ParseResults<CommandSourceStack> results = dispatcher.parse(command, commandSource);
            dispatcher.execute(results);
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
