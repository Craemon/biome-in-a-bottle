package com.craemon;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BiomeInABottle implements ModInitializer {
	public static final String MOD_ID = "biome-in-a-bottle";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		UseItemCallback.EVENT.register(this::onUseItem);

		LOGGER.info("Hello Fabric world!");
	}

	private ActionResult onUseItem(PlayerEntity player, World world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		// Check if it's a piece of paper with the 'custom_item' tag
		if (stack.getItem().toString().equals("minecraft:paper")) {
			if (!world.isClient) {
				player.sendMessage(Text.literal("You used the Magic Paper!"), false);
				// Add more server-side logic here, like triggering effects or events
			}
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

}