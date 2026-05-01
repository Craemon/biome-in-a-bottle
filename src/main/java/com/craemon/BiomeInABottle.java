package com.craemon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		player.sendSystemMessage(Component.literal("Changing the biome..."));

		//get the biomeID and biome size
		String biomeId = customNbt.getString("StoredBiome").orElse("unknown_biome");
		String biomeSize = customNbt.getString("BiomeSize").orElse("unknown_size");

		BlockPos playerPosition = player.blockPosition();
		ChunkPos chunkPos = new ChunkPos(playerPosition.getX() >> 4, playerPosition.getZ() >> 4);
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
			player.sendSystemMessage(Component.literal("Invalid Item"));
			return InteractionResult.FAIL;
		}

		if (!player.isCreative()) { // Only decrease if the player is NOT in Creative mode
			itemStack.shrink(1); // Reduce by 1
		}

		// Let the player know the biome has been changed successfully
		player.sendSystemMessage(Component.literal("Biome changed to: " + biomeId));

		return InteractionResult.SUCCESS;
	}
}