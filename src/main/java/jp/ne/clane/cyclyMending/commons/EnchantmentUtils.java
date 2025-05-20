package jp.ne.clane.cyclyMending.commons;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentUtils {
	public static int getEnchantmentLevel(ClientLevel clientLevel, ItemStack itemStack, ResourceKey<Enchantment> enchant) {
		return itemStack.getEnchantmentLevel(getEnchantmentHolder(clientLevel, enchant));
	}
	
	private static Holder<Enchantment> getEnchantmentHolder(ClientLevel clientLevel, ResourceKey<Enchantment> enchant) {
		return clientLevel.registryAccess().holderOrThrow(enchant);
	}
	

}
