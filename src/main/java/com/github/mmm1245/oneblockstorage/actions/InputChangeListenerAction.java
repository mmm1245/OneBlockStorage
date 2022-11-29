package com.github.mmm1245.oneblockstorage.actions;

import com.github.mmm1245.oneblockstorage.Util;
import com.github.mmm1245.oneblockstorage.client.OneblockstorageClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

public class InputChangeListenerAction extends Action{
    public InputChangeListenerAction(){

    }
    @Override
    public void onStart() {
        Util.clickBlock(OneblockstorageClient.getInstance().getInputChestPos());
    }
    @Override
    public void onInventory(Inventory inventory) {

    }
    @Override
    public void onInventoryChange(Inventory inventory, ScreenHandlerSlotUpdateS2CPacket packet) {
        var content = Util.readContent(inventory);
        for(int i = 0;i < content.size();i++){
            if(!content.get(i).isEmpty()){
                OneblockstorageClient.getInstance().setAction(new StoreItemAction(i));
                return;
            }
        }
    }
    @Override
    public boolean canInterrupt() {
        return true;
    }
}
