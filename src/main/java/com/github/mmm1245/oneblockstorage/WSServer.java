package com.github.mmm1245.oneblockstorage;

import com.github.mmm1245.oneblockstorage.actions.RetrieveItemAction;
import com.github.mmm1245.oneblockstorage.client.OneblockstorageClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

public class WSServer extends WebSocketServer {
    private final Gson GSON = new Gson();
    public final OneblockstorageClient clientMain;
    public WSServer(int port, OneblockstorageClient clientMain) {
        super(new InetSocketAddress(port));
        this.clientMain = clientMain;
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        for(var inv : clientMain.getInventories().entrySet()){
            syncInventory(conn, inv.getKey(), inv.getValue());
        }
    }
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }
    @Override
    public void onMessage(WebSocket conn, String message) {
        JsonObject json = (JsonObject) JsonParser.parseString(message);
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        int slot = json.get("slot").getAsInt();
        clientMain.setAction(new RetrieveItemAction(Util.getEntityByUUID(uuid), slot));
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {

    }
    public void syncInventory(WebSocket conn, UUID uuid, List<ItemStack> items){
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid.toString());
        JsonArray itemsJson = new JsonArray();
        for(int i = 0;i < items.size();i++){
            JsonObject itemJson = new JsonObject();
            ItemStack item = items.get(i);
            itemJson.addProperty("slot", i);
            //Registry.ITEM.getId(item.getItem()).getPath()
            itemJson.addProperty("name", item.getCount() + "x " + item.getItem().getName().getString());
            itemsJson.add(itemJson);
        }
        json.add("items", itemsJson);
        conn.send(GSON.toJson(json));
    }
    @Override
    public void onStart() {

    }
}
