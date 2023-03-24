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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class MainActivity extends AppCompatActivity {

    private EditText roomIDEdit, sendText;
    private TextView getRoomID;
    private Button socketBtn, makeRoomBtn, sendMsg;
    private StompClient sockClient;
    private List<StompHeader> headerList;
    private OkHttpClient okHttpClient;
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

        sendMsg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String msg = String.valueOf(sendText.getText());
                sendStomp(msg);
            }
        });
    }

    private void initView(){
        socketBtn = findViewById(R.id.button);
        makeRoomBtn = findViewById(R.id.button2);
        roomIDEdit = findViewById(R.id.text);
        getRoomID = findViewById(R.id.textView);
        sendText = findViewById(R.id.text2);
        sendMsg = findViewById(R.id.sendmsg);
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
    public void sendStomp(String msg){
        JsonObject data = new JsonObject();
        data.addProperty("roomId", "0");
        data.addProperty("writer", "client");
        data.addProperty("message", msg);
        Log.d("Send Msg: ", data.toString());
        sockClient.send("/pub/game/message", data.toString()).subscribe();
    }

    public void initStomp(String roomId) {
        //Log.d("getSocket Start: ", "getSocket Start");

        sockClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "wss://www.seop.site" + "/stomp/game/websocket");

        AtomicBoolean isUnexpectedClosed = new AtomicBoolean(false);

        //Log.d("lifecycle Start: ", "lifecycle Start");
        sockClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    //Log.d("Connected: ", "Stomp connection opened");
                    break;
                case ERROR:
                    //Log.d("Errored: ", "Error", lifecycleEvent.getException());
                    if (lifecycleEvent.getException().getMessage().contains("EOF")) {
                        isUnexpectedClosed.set(true);
                    }
                    break;
                case CLOSED:
                    //Log.d("Closed: ", "Stomp connection closed");
                    if (isUnexpectedClosed.get()) {
                        /**
                         * EOF Error
                         */
                        initStomp(roomId);
                        isUnexpectedClosed.set(false);
                    }
                    break;
            }
        });

        //Log.d("connect Start: ", "connect Start");
        sockClient.connect();

        //Log.d("topic Start: ", "topic Start with " + roomId);
        sockClient.topic("/sub/game/room/" + roomId).subscribe(topicMessage -> {
            JsonParser parser = new JsonParser();
            Object obj = parser.parse(topicMessage.getPayload());
            //Log.d("Recv Msg: ", obj.toString());
        }, System.out::println);

        JsonObject data = new JsonObject();
        data.addProperty("roomId", "0");
        data.addProperty("writer", "client");
        Log.d("Send Msg: ", data.toString());
        sockClient.send("/pub/game/message", data.toString()).subscribe();
    }


}

class RestClient {

    public static final String ANDROID_EMULATOR_LOCALHOST = "https://www.seop.site";
    public static final String SERVER_PORT = "9000";

    private static RestClient instance;
    private static final Object lock = new Object();

    public static RestClient getInstance() {
        RestClient instance = RestClient.instance;
        if (instance == null) {
            synchronized (lock) {
                instance = RestClient.instance;
                if (instance == null) {
                    RestClient.instance = instance = new RestClient();
                }
            }
        }
        return instance;
    }

    private final ExampleRepository mExampleRepository;

    private RestClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.seop.site" + ":" + SERVER_PORT + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mExampleRepository = retrofit.create(ExampleRepository.class);
    }

    public ExampleRepository getExampleRepository() {
        return mExampleRepository;
    }
}