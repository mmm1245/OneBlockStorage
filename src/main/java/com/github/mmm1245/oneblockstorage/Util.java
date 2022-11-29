package com.github.mmm1245.oneblockstorage;

import com.github.mmm1245.oneblockstorage.client.OneblockstorageClient;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

public class Util {
    public static List<ItemStack> readContent(Inventory inventory){
        ArrayList<ItemStack> list = new ArrayList<>();
        for(int i = 0;i < inventory.size();i++)
            list.add(inventory.getStack(i));
        return list;
    }
    public static void quickMoveItem(int slot){
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new ClickSlotC2SPacket(MinecraftClient.getInstance().player.currentScreenHandler.syncId, MinecraftClient.getInstance().player.currentScreenHandler.getRevision(), slot, 0, SlotActionType.QUICK_MOVE, ItemStack.EMPTY, new Int2ObjectArrayMap<>()));
    }
    public static void clickBlock(BlockPos blockPos){
        Vec3d hit = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.8, 0.5);
        MinecraftClient.getInstance().interactionManager.interactBlock(MinecraftClient.getInstance().player, Hand.MAIN_HAND, new BlockHitResult(hit, Direction.UP, blockPos, true));
    }
    public static Entity getEntityByUUID(UUID uuid){
        return StreamSupport.stream(MinecraftClient.getInstance().world.getEntities().spliterator(), false).filter(entity -> entity.getUuid().equals(uuid)).findFirst().get();
    }
}
