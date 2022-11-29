package com.github.mmm1245.oneblockstorage.actions;

import com.github.mmm1245.oneblockstorage.Util;
import com.github.mmm1245.oneblockstorage.client.OneblockstorageClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LoadMinecartsAction extends Action{
    private List<Entity> minecarts;
    public LoadMinecartsAction(){

    }

    @Override
    public void onStart() {
        minecarts = StreamSupport.stream(MinecraftClient.getInstance().world.getEntities().spliterator(), false).filter(entity -> entity.getType() == EntityType.CHEST_MINECART).filter(entity -> entity.getPos().distanceTo(MinecraftClient.getInstance().player.getPos()) < 4).collect(Collectors.toList());
        if(minecarts.size() > 0){
            MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, minecarts.get(0), Hand.MAIN_HAND);
        }
    }

    @Override
    public void onInventory(Inventory inventory) {
        Entity minecart = minecarts.get(0);
        OneblockstorageClient.getInstance().registerInventory(minecart.getUuid(), Util.readContent(inventory));
        minecarts.remove(0);
        if(minecarts.size() > 0){
            MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, minecarts.get(0), Hand.MAIN_HAND);
        } else {
            OneblockstorageClient.getInstance().setAction(null);
        }
    }
    public void onInventoryChange(Inventory inventory, ScreenHandlerSlotUpdateS2CPacket packet){}
}
