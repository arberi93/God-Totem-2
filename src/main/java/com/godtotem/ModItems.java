package com.godtotem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

public class ModItems {

	/** Number of times the totem can save you before it breaks. */
	public static final int GOD_TOTEM_USES = 2;

	public static final Item GOD_TOTEM = register(
			"god_totem",
			Item::new,
			new Item.Properties().durability(GOD_TOTEM_USES).rarity(Rarity.EPIC)
	);

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, GodTotemMod.id(name));
		Item item = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void initialize() {
		// Show the totem in the Combat tab of the creative inventory.
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
				.register(output -> output.accept(GOD_TOTEM));
	}
}
