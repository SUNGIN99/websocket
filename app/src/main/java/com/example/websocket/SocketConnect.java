package com.example.websocket;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SocketConnect {

    @POST("/app/game/open")
    Call<ChatRoomDTO> getMatchRoomID(@Query("id") String id);

}
