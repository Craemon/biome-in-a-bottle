package com.craemon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.component.DataComponentTypes.CUSTOM_DATA;

public class BiomeInABottle implements ModInitializer {
	public static final String MOD_ID = "biome-in-a-bottle";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Register an event triggered when a player uses an item
		UseItemCallback.EVENT.register(this::onUseItem);
		LOGGER.info("BiomeInABottle mod initialized!");
	}

	//triggered when player uses item
	private ActionResult onUseItem(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.PASS; // Ignore if the action is on the client side
		}

		// Ensure the player is holding the correct item with Stored Biome
		ItemStack itemStack = player.getStackInHand(hand);
		if (!itemStack.getItem().toString().equals("minecraft:paper") || itemStack.get(CUSTOM_DATA) == null || !Objects.requireNonNull(itemStack.get(CUSTOM_DATA)).contains("StoredBiome")) {
			return ActionResult.PASS;
		}
		// Print Chat Message
		player.sendMessage(Text.literal("Changing the biome..."), false);

		//get the biomeID
		String StoredBiome = String.valueOf(itemStack.get(CUSTOM_DATA));
		Pattern pattern = Pattern.compile("StoredBiome:\"(.*?)\"");
		Matcher matcher = pattern.matcher(StoredBiome);
		String BiomeId = matcher.find() ? matcher.group(1) : "unknown_biome";
		//get biome size
		Pattern pattern1 = Pattern.compile("BiomeSize:\"(.*?)\"");
		Matcher matcher1 = pattern1.matcher(StoredBiome);
		String BiomeSize = matcher1.find() ? matcher1.group(1) : "unknown_size";
		//try to execute operation
		if (BiomeSize.equals("Giant")) {
			// Get the player's chunk position
			BlockPos playerPosition = player.getBlockPos();
			ChunkPos chunkPos = new ChunkPos(playerPosition);
			//get player dimension
			String dimensionId =  player.getWorld().getRegistryKey().getValue().toString();

			// Execute the biome change
			ChangeBiomes.changeBiomeChunk(serverWorld, chunkPos, dimensionId, BiomeId);
		} else if (BiomeSize.equals("Small")) {
			ChangeBiomes.changeBiomePaint(serverWorld, player, BiomeId, 0);
		} else if (BiomeSize.equals("Medium")) {
			ChangeBiomes.changeBiomePaint(serverWorld, player, BiomeId, 2);
		} else if (BiomeSize.equals("Large")) {
			ChangeBiomes.changeBiomePaint(serverWorld, player, BiomeId, 5);
		} else {
			player.sendMessage(Text.literal("Invalid Item"), false);
			return ActionResult.FAIL;
		}

		if (!player.isCreative()) { // Only decrease if the player is NOT in Creative mode
			itemStack.decrement(1); // Reduce by 1
		}

		// Let the player know the biome has been changed successfully
		player.sendMessage(Text.literal("Biome changed to: " + BiomeId), false);

		return ActionResult.SUCCESS;
	}
}