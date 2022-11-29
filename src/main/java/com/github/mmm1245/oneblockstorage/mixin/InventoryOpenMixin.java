package com.github.mmm1245.oneblockstorage.mixin;

import com.github.mmm1245.oneblockstorage.client.OneblockstorageClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class InventoryOpenMixin {
    @Inject(method = "onInventory",at = @At("TAIL"))
    public void onInventory(InventoryS2CPacket packet, CallbackInfo ci){
        OneblockstorageClient.getInstance().onInventoryOpen(packet);
    }
    @Inject(method = "onScreenHandlerSlotUpdate",at = @At("TAIL"))
    public void onInventory(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci){
        OneblockstorageClient.getInstance().onInventoryChange(packet);
    }
}
