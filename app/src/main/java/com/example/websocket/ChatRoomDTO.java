package com.example.websocket;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class ChatRoomDTO {
    @SerializedName(value = "isSuccess") private boolean isSuccess;
    @SerializedName(value = "code") private int code;
    @SerializedName(value = "message") private String message;

    @Nullable
    @SerializedName(value = "result")
    private ChatRoomResult result;

    public ChatRoomDTO(boolean isSuccess, int code, String message, @Nullable ChatRoomResult result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Nullable
    public ChatRoomResult getResult() {
        return result;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResult(@Nullable ChatRoomResult result) {
        this.result = result;
    }
}
