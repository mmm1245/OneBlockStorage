package com.github.mmm1245.oneblockstorage.actions;

import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

public abstract class Action {
    public abstract void onStart();
    public abstract void onInventory(Inventory inventory);
    public abstract void onInventoryChange(Inventory inventory, ScreenHandlerSlotUpdateS2CPacket packet);
    public boolean canInterrupt(){
        return false;
    }
}
