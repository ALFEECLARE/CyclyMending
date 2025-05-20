package jp.ne.clane.cyclyMending.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import jp.ne.clane.cyclyMending.CyclyMending;
import jp.ne.clane.cyclyMending.commons.ClientUtils;
import jp.ne.clane.cyclyMending.commons.ClientUtils.InventoryType;
import jp.ne.clane.cyclyMending.commons.EnchantmentUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

@Mixin(value = LocalPlayer.class)
public class ClientPlayerMixin extends AbstractClientPlayer {
	private int swappedInventoryIndex = -1;
	private int awaitTick = 0;

    public ClientPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void tryOffhandDamagedItem(CallbackInfo callbackinfo) {
    	if (awaitTick > 0) {
    		awaitTick--;
    		return;
    	}
    	if (!CyclyMending.isMendingMode || swappedInventoryIndex != -1) { return; }
        if (this.onGround() || this.isInWater()) {
            Inventory inventory = this.getInventory();
            if (isDamagedAndMendableItem(inventory.offhand.getFirst())) {
            	return;
            }
            int targetIndex = getMendableItemIndex(inventory); 
            if (targetIndex != -1) {
        		swappedInventoryIndex = targetIndex;
        		ClientUtils.swapPlayerInventorySlot(this, ClientUtils.convertSlotIdFromInventoryPair(InventoryType.OFFHAND, 0), ClientUtils.convertSlotIdFromInventoryPair(InventoryType.INVENTORY, targetIndex));
        		awaitTick = 10;
            } else {
            	CyclyMending.isMendingMode = false;
            	this.sendSystemMessage(Component.translatable("jp.ne.clane.cyclyMending.message.autoDeactivaed"));
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tryReleaseRecoveredItem(CallbackInfo callbackinfo) {
    	if (awaitTick > 0) {
    		awaitTick--;
    		return;
    	}
    	if (swappedInventoryIndex == -1) { return; }
    	if (!CyclyMending.isMendingMode) {
    		//mendingModeがfalseの場合、完了前に無効化された=強制的に元に戻す
    		ClientUtils.swapPlayerInventorySlot(this, ClientUtils.convertSlotIdFromInventoryPair(InventoryType.OFFHAND, 0), ClientUtils.convertSlotIdFromInventoryPair(InventoryType.INVENTORY, swappedInventoryIndex));
    		swappedInventoryIndex = -1;
    		awaitTick = 10;
    	} else if (this.onGround() || this.isInWater()) {
            Inventory inventory = this.getInventory();
            if (!isDamagedAndMendableItem(inventory.offhand.getFirst())) {
	    		ClientUtils.swapPlayerInventorySlot(this, ClientUtils.convertSlotIdFromInventoryPair(InventoryType.OFFHAND, 0), ClientUtils.convertSlotIdFromInventoryPair(InventoryType.INVENTORY, swappedInventoryIndex));
	    		swappedInventoryIndex = -1;
        		awaitTick = 10;
            }
        }
    }

    private boolean isDamagedAndMendableItem(ItemStack itemStack) {
    	return itemStack.isDamaged() && itemStack.isEnchanted() && (EnchantmentUtils.getEnchantmentLevel(this.clientLevel, itemStack, Enchantments.MENDING) > 0);
    }
    
    private int getMendableItemIndex(Inventory inventory) {
	    NonNullList<ItemStack> mainInventory = inventory.items;
	    for (int i=0;i < mainInventory.size() ;i++) {
	    	if (i == inventory.selected) { continue; }
	    	if (isDamagedAndMendableItem(mainInventory.get(i))) {
	    		return i;
	    	}
	    }
    	return -1;
    }
    
}
