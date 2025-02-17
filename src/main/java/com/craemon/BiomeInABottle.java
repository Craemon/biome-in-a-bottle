package com.craemon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiomeInABottle implements ModInitializer {
	public static final String MOD_ID = "biome-in-a-bottle";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Register an event triggered when a player uses an item
		UseItemCallback.EVENT.register(this::onUseItem);
		LOGGER.info("BiomeInABottle mod initialized!");
	}

	/**
	 * Triggered when a player uses an item in the world.
	 */
	private ActionResult onUseItem(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand) {
		// Ensure that the player is on the server side
		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.PASS; // Ignore if the action is on the client side
		}

		// Ensure the player is holding the correct item (paper in this case)
		ItemStack itemStack = player.getStackInHand(hand);
		if (!itemStack.getItem().toString().equals("minecraft:paper")) {
			return ActionResult.PASS; // Ignore if the item is not paper
		}

		// Notify the player
		player.sendMessage(Text.literal("You used Biome-in-a-Bottle! Changing the biome..."), false);

		// Get the player's chunk position
		BlockPos playerPosition = player.getBlockPos();
		ChunkPos chunkPos = new ChunkPos(playerPosition);

		// Set the biome (default: PLAINS, can be changed to anything else)
		RegistryKey<?> biomeKey = BiomeKeys.PLAINS;

		// Execute the biome change
		changeChunkBiome(serverWorld, chunkPos, biomeKey);

		// Let the player know the biome has been changed successfully
		player.sendMessage(Text.literal("Biome changed to: " + biomeKey.getValue()), false);

		return ActionResult.SUCCESS;
	}

	/**
	 * Changes the biome for a specific chunk using the /fillbiome command.
	 * The command is executed by the server directly, bypassing player, so permissions aren't an issue.
	 */
	private void changeChunkBiome(ServerWorld serverWorld, ChunkPos chunkPos, RegistryKey<?> biomeKey) {
		// Get the server object
		MinecraftServer server = serverWorld.getServer();
		if (server == null) {
			LOGGER.error("Server instance is null, cannot execute fillbiome command.");
			return;
		}

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
			String command = String.format("fillbiome %d %d %d %d %d %d %s",
					minX, yStart, minZ, // Start of the sub-chunk
					maxX, yEnd, maxZ,   // End of the sub-chunk
					biomeKey.getValue() // Selected biome
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
}