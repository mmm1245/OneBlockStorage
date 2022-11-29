package com.github.mmm1245.oneblockstorage.actions;

import com.github.mmm1245.oneblockstorage.Oneblockstorage;
import com.github.mmm1245.oneblockstorage.Util;
import com.github.mmm1245.oneblockstorage.client.OneblockstorageClient;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class RetrieveItemAction extends Action{
    public final Entity minecart;
    public final int slot;
    private boolean expectingStorage;
    private boolean alreadyOpen;
    public RetrieveItemAction(Entity minecart, int slot) {
        this.minecart = minecart;
        this.slot = slot;

    }
    @Override
    public void onStart() {
        MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, minecart, Hand.MAIN_HAND);
        this.expectingStorage = true;
        alreadyOpen = false;
    }
    @Override
    public void onInventory(Inventory inventory) {
        if(expectingStorage){
            if(alreadyOpen) {
                OneblockstorageClient.getInstance().registerInventory(minecart.getUuid(), Util.readContent(inventory));
                Util.clickBlock(OneblockstorageClient.getInstance().getOutputChestPos());
                expectingStorage = false;
            } else {
                Util.quickMoveItem(slot);
                //MinecraftClient.getInstance().player.currentScreenHandler.onSlotClick(slot, 0, SlotActionType.QUICK_MOVE, MinecraftClient.getInstance().player);
                MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, minecart, Hand.MAIN_HAND);
                alreadyOpen = true;
            }
        } else {
            Util.quickMoveItem(62);
            //MinecraftClient.getInstance().player.currentScreenHandler.onSlotClick(slot, 62, SlotActionType.QUICK_MOVE, MinecraftClient.getInstance().player);
        }
    }

    @Override
    public void onInventoryChange(Inventory inventory, ScreenHandlerSlotUpdateS2CPacket packet) {
        if(!expectingStorage){
            OneblockstorageClient.getInstance().setAction(null);
        }
    }
}
