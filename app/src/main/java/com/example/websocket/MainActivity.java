package com.example.websocket;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class MainActivity extends AppCompatActivity {

    private EditText roomIDEdit;
    private TextView getRoomID;
    private Button socketBtn, makeRoomBtn;
    private StompClient sockClient;
    private List<StompHeader> headerList;
    private final SocketConnect socketConnectInterface = NetworkModule.getRetrofit().create(SocketConnect.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        makeRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomId = String.valueOf(roomIDEdit.getText());
                initMatchRoom(roomId);
            }
        });

        socketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initStomp(String.valueOf(getRoomID.getText()));
            }

        });
    }

    private void initView(){
        socketBtn = findViewById(R.id.button);
        makeRoomBtn = findViewById(R.id.button2);
        roomIDEdit = findViewById(R.id.text);
        getRoomID = findViewById(R.id.textView);
    }

    public void initMatchRoom(String roomId){
        socketConnectInterface.getMatchRoomID(roomId).enqueue(new Callback<ChatRoomDTO>() {
            @Override
            public void onResponse(Call<ChatRoomDTO> call, Response<ChatRoomDTO> response) {
                ChatRoomDTO resp = response.body();
                assert resp!= null;
                if(resp.getCode() == 1000){
                    getRoomID.setText(resp.getResult().getRoomId());
                }
            }

            @Override
            public void onFailure(Call<ChatRoomDTO> call, Throwable t) {

            }
        });
    }

    public void initStomp(String roomId){
        Log.d("getSocket Start: ", "getSocket Start");
        sockClient = NetworkModule.getSocket();
        AtomicBoolean isUnexpectedClosed = new AtomicBoolean(false);

        Log.d("lifecycle Start: ", "lifecycle Start");
        sockClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("Connected: ", "Stomp connection opened");
                    break;
                case ERROR:
                    Log.d("Errored: ", "Error", lifecycleEvent.getException());
                    if(lifecycleEvent.getException().getMessage().contains("EOF")){
                        isUnexpectedClosed.set(true);
                    }
                    break;
                case CLOSED:
                    Log.d("Closed: ", "Stomp connection closed");
                    if(isUnexpectedClosed.get()){
                        /**
                         * EOF Error
                         */
                        initStomp(roomId);
                        isUnexpectedClosed.set(false);
                    }
                    break;
            }
        });

        Log.d("connect Start: ", "connect Start");
        sockClient.connect();

        Log.d("topic Start: ", "topic Start with "+ roomId);
        sockClient.topic("/sub/game/room/"+ roomId).subscribe(topicMessage ->{
            JsonParser parser = new JsonParser();
            Object obj = parser.parse(topicMessage.getPayload());
            Log.d("Recv Msg: ", obj.toString());
        }, System.out::println);

        /*JsonObject data = new JsonObject();
        data.addProperty("roomId", "0");
        data.addProperty("writer", "client");
        Log.d("Send Msg: ", data.toString());
        sockClient.send("/pub/game/message", data.toString()).subscribe();*/
    }
}