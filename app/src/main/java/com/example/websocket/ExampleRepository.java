package com.example.websocket;

import io.reactivex.Completable;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExampleRepository {
    @POST("/app/game/open")
    Completable sendRestEcho(@Query("id") String id);
}
