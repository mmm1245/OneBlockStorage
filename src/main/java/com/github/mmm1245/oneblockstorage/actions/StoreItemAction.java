package com.github.mmm1245.oneblockstorage.actions;

import com.github.mmm1245.oneblockstorage.Oneblockstorage;
import com.github.mmm1245.oneblockstorage.Util;
import com.github.mmm1245.oneblockstorage.client.OneblockstorageClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.stream.StreamSupport;

public class StoreItemAction extends Action{
    public final int slot;
    private boolean chestInventory;
    private Entity target;
    private boolean waitingConfirmation;
    public StoreItemAction(int slot){
        this.slot = slot;
    }

    @Override
    public void onStart() {
        Util.clickBlock(OneblockstorageClient.getInstance().getInputChestPos());
        this.chestInventory = true;
        this.waitingConfirmation = false;
    }

    @Override
    public void onInventory(Inventory inventory) {
        if(chestInventory){
            Util.quickMoveItem(slot);
            for(var entry : OneblockstorageClient.getInstance().getInventories().entrySet()){
                for(ItemStack is : entry.getValue()){
                    if(is.isEmpty()){
                        target = Util.getEntityByUUID(entry.getKey());
                        chestInventory = false;
                        MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, target, Hand.MAIN_HAND);
                        return;
                    }
                }
            }
        } else {
            if(waitingConfirmation){
                OneblockstorageClient.getInstance().registerInventory(target.getUuid(), Util.readContent(inventory));
                OneblockstorageClient.getInstance().setAction(null);
            } else {
                Util.quickMoveItem(62);
                MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, target, Hand.MAIN_HAND);
                waitingConfirmation = true;
            }
        }
    }

    @Override
    public void onInventoryChange(Inventory inventory, ScreenHandlerSlotUpdateS2CPacket packet) {

    }
}
