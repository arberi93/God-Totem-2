package com.godtotem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GodTotemMod implements ModInitializer {

	public static final String MOD_ID = "godtotem";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ServerLivingEntityEvents.ALLOW_DEATH.register(GodTotemMod::onAllowDeath);
	}

	/**
	 * Runs whenever a living entity is about to die.
	 * Returning false cancels the death.
	 */
	private static boolean onAllowDeath(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayer player)) {
			return true;
		}

		// Like the vanilla totem, don't save from /kill or the void.
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return true;
		}

		InteractionHand hand = findGodTotemHand(player);
		if (hand == null) {
			return true;
		}

		useUpOneCharge(player.getItemInHand(hand));

		// Same start as the vanilla totem: clear bad effects, then heal.
		player.removeAllEffects();
		player.setHealth(player.getMaxHealth());
		applyGodEffects(player);

		// Plays the totem pop animation and sound.
		player.level().broadcastEntityEvent(player, (byte) 35);

		return false;
	}

	private static InteractionHand findGodTotemHand(ServerPlayer player) {
		if (player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.GOD_TOTEM)) {
			return InteractionHand.MAIN_HAND;
		}
		if (player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.GOD_TOTEM)) {
			return InteractionHand.OFF_HAND;
		}
		return null;
	}

	private static void useUpOneCharge(ItemStack stack) {
		int newDamage = stack.getDamageValue() + 1;
		if (newDamage >= stack.getMaxDamage()) {
			stack.shrink(1); // last use: the totem breaks
		} else {
			stack.setDamageValue(newDamage);
		}
	}

	// Durations are in ticks (20 ticks = 1 second). Amplifier 0 = level I, 1 = level II, etc.
	private static void applyGodEffects(ServerPlayer player) {
		// Vanilla totem gives Regeneration II (45 s), so one level up is III.
		// Applied first because it is the weaker, longer effect.
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45 * 20, 2));
		// Regeneration X for 5 seconds. Minecraft keeps the longer III running underneath
		// and switches back to it when X runs out.
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 5 * 20, 9));

		// +10 hearts of absorption (each level is 2 hearts) for 3 minutes.
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 3 * 60 * 20, 4));

		// Vanilla gives Fire Resistance I (40 s), so one level up is II.
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40 * 20, 1));

		// Strength II for 3 minutes.
		player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 3 * 60 * 20, 1));

		// Speed III for 1 minute.
		player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60 * 20, 2));
	}
}
