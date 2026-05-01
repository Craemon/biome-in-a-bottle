package com.craemon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;

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
	private InteractionResult onUseItem(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand) {
		if (!(world instanceof ServerLevel serverWorld)) {
			return InteractionResult.PASS; // Ignore if the action is on the client side
		}

		// Ensure the player is holding the correct item with Stored Biome
		ItemStack itemStack = player.getItemInHand(hand);
		var customData = itemStack.get(CUSTOM_DATA);

        // Check 1: Is the component itself null?
		if (customData == null) {
			return InteractionResult.PASS;
		}

        // Check 2: Does the NbtCompound contain the specific key?
		CompoundTag customNbt = customData.copyTag();
		if(!itemStack.is(Items.PAPER) || !customNbt.contains("StoredBiome")) {
			return InteractionResult.PASS;
		}

		// Print Chat Message
		player.displayClientMessage(Component.literal("Changing the biome..."), false);

		//get the biomeID and biome size
		String biomeId = customNbt.getString("StoredBiome").orElse("unknown_biome");
		String biomeSize = customNbt.getString("BiomeSize").orElse("unknown_size");

//		String StoredBiome = String.valueOf(itemStack.get(CUSTOM_DATA));
//		Pattern pattern = Pattern.compile("StoredBiome:\"(.*?)\"");
//		Matcher matcher = pattern.matcher(StoredBiome);
//		String BiomeId = matcher.find() ? matcher.group(1) : "unknown_biome";
//		Pattern pattern1 = Pattern.compile("BiomeSize:\"(.*?)\"");
//		Matcher matcher1 = pattern1.matcher(StoredBiome);
//		String BiomeSize = matcher1.find() ? matcher1.group(1) : "unknown_size";

		BlockPos playerPosition = player.blockPosition();
		ChunkPos chunkPos = new ChunkPos(playerPosition);
		//get player dimension
		String dimensionId =  player.level().dimension().identifier().toString();

		//try to execute operation
		if (biomeSize.equals("Giant")) {
			ChangeBiomes.changeBiomeChunk(serverWorld, chunkPos, dimensionId, biomeId);
		} else if (biomeSize.equals("Huge")) {
            int subchunkYStart = (playerPosition.getY() / 16) * 16;
            ChangeBiomes.changeBiomeSubchunk(serverWorld, chunkPos, subchunkYStart, dimensionId, biomeId);
        } else if (biomeSize.equals("Small")) {
			ChangeBiomes.changeBiomePaint(serverWorld, player, biomeId, 0);
		} else if (biomeSize.equals("Medium")) {
			ChangeBiomes.changeBiomePaint(serverWorld, player, biomeId, 2);
		} else if (biomeSize.equals("Large")) {
			ChangeBiomes.changeBiomePaint(serverWorld, player, biomeId, 5);
		} else {
			player.displayClientMessage(Component.literal("Invalid Item"), false);
			return InteractionResult.FAIL;
		}

		if (!player.isCreative()) { // Only decrease if the player is NOT in Creative mode
			itemStack.shrink(1); // Reduce by 1
		}

		// Let the player know the biome has been changed successfully
		player.displayClientMessage(Component.literal("Biome changed to: " + biomeId), false);

		return InteractionResult.SUCCESS;
	}
}