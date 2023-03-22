package com.example.websocket;

import com.google.gson.annotations.SerializedName;

public class ChatRoomResult {

    @SerializedName(value = "roomId") private String roomId;
    @SerializedName(value = "name")private String name;

    public ChatRoomResult(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
