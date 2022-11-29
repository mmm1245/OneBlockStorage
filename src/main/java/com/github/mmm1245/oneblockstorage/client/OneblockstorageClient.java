package com.github.mmm1245.oneblockstorage.client;

import com.github.mmm1245.oneblockstorage.WSServer;
import com.github.mmm1245.oneblockstorage.actions.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.StreamSupport;

@Environment(EnvType.CLIENT)
public class OneblockstorageClient implements ClientModInitializer {
    private static OneblockstorageClient CLIENT_MOD;
    private BlockPos inputChestPos;
    private BlockPos outputChestPos;
    private HashMap<UUID, List<ItemStack>> inventories;
    private boolean started;
    private Action action;
    private WSServer server;
    @Override
    public void onInitializeClient() {
        CLIENT_MOD = this;
        this.inputChestPos = null;
        this.outputChestPos = null;
        this.inventories = new HashMap<>();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("openMinecart").then(RequiredArgumentBuilder.<FabricClientCommandSource,Integer>argument("index", IntegerArgumentType.integer()).executes(context -> {
                int index = IntegerArgumentType.getInteger(context, "index");
                List<Entity> minecarts = StreamSupport.stream(MinecraftClient.getInstance().world.getEntities().spliterator(), false).filter(entity -> entity.getType() == EntityType.CHEST_MINECART).filter(entity -> entity.getPos().distanceTo(MinecraftClient.getInstance().player.getPos()) < 4).toList();
                MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, minecarts.get(index), Hand.MAIN_HAND);
                return 1;
            })));
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("setInputChest").executes(context -> {
                if(started){
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Storage already started"));
                    return 1;
                }
                BlockPos blockPos = MinecraftClient.getInstance().player.getBlockPos();
                if(MinecraftClient.getInstance().world.getBlockState(blockPos).getBlock() != Blocks.CHEST){
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("You must be standing on chest"));
                    return 1;
                }
                this.inputChestPos = blockPos;
                return 1;
            }));
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("setOutputChest").executes(context -> {
                if(started){
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Storage already started"));
                    return 1;
                }
                BlockPos blockPos = MinecraftClient.getInstance().player.getBlockPos();
                if(MinecraftClient.getInstance().world.getBlockState(blockPos).getBlock() != Blocks.CHEST){
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("You must be standing on chest"));
                    return 1;
                }
                this.outputChestPos = blockPos;
                return 1;
            }));
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("startStorage").executes(context -> {
                if(started){
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Storage already started"));
                    return 1;
                }
                if(outputChestPos == null || inputChestPos == null){
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Input and output chests must be specified"));
                    return 1;
                }
                this.started = true;
                this.action = new LoadMinecartsAction();
                this.action.onStart();
                this.server = new WSServer(4321, this);
                this.server.start();
                return 1;
            }));
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("retrieveItem").then(RequiredArgumentBuilder.<FabricClientCommandSource,Integer>argument("index", IntegerArgumentType.integer()).then(RequiredArgumentBuilder.<FabricClientCommandSource,Integer>argument("slot", IntegerArgumentType.integer()).executes(context -> {
                int index = IntegerArgumentType.getInteger(context, "index");
                int slot = IntegerArgumentType.getInteger(context, "slot");
                List<Entity> minecarts = StreamSupport.stream(MinecraftClient.getInstance().world.getEntities().spliterator(), false).filter(entity -> entity.getType() == EntityType.CHEST_MINECART).filter(entity -> entity.getPos().distanceTo(MinecraftClient.getInstance().player.getPos()) < 4).toList();
                setAction(new RetrieveItemAction(minecarts.get(index), slot));
                return 1;
            }))));
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("storeItem").then(RequiredArgumentBuilder.<FabricClientCommandSource,Integer>argument("slot", IntegerArgumentType.integer()).executes(context -> {
                int slot = IntegerArgumentType.getInteger(context, "slot");
                setAction(new StoreItemAction(slot));
                return 1;
            })));
        });
    }
    public BlockPos getInputChestPos() {
        return inputChestPos;
    }
    public BlockPos getOutputChestPos() {
        return outputChestPos;
    }
    public void setAction(Action action){
        if(this.action == null || this.action.canInterrupt() || action == null) {
            this.action = action;
            if (this.action == null)
                this.action = new InputChangeListenerAction();
            this.action.onStart();
            System.out.println("new action: " + this.action);
        }
    }
    public Map<UUID, List<ItemStack>> getInventories() {
        return Collections.unmodifiableMap(inventories);
    }
    public void registerInventory(UUID uuid, List<ItemStack> inventory){
        inventories.put(uuid, inventory);
        for(var conn : server.getConnections()){
            server.syncInventory(conn, uuid, inventory);
        }
    }
    public void onInventoryOpen(InventoryS2CPacket packet){
        if(action != null)
            action.onInventory(MinecraftClient.getInstance().player.currentScreenHandler.getSlot(0).inventory);
    }
    public void onInventoryChange(ScreenHandlerSlotUpdateS2CPacket packet){
        if(action != null)
            action.onInventoryChange(MinecraftClient.getInstance().player.currentScreenHandler.getSlot(0).inventory, packet);
    }
    public static OneblockstorageClient getInstance(){
        return CLIENT_MOD;
    }
}
