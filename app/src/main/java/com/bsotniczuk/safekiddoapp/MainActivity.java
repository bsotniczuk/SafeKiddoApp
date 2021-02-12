package com.bsotniczuk.safekiddoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bsotniczuk.safekiddoapp.adapter.AdapterRecyclerView;
import com.bsotniczuk.safekiddoapp.api.MessageApi;
import com.bsotniczuk.safekiddoapp.datamodel.JsonMessage;
import com.bsotniczuk.safekiddoapp.datamodel.MessageModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterRecyclerView.OnMessageClickListener {

    //important
    //TODO: test path
    //TODO: Please enable internet connection prompt when internet is not enabled to download from API
    //TODO: Repair Glide error related to loading last image with glide
    //TODO: Add ability to edit message
    //TODO: Add ability to add message
    //TODO: Restyle message_item
    //TODO: Count all characters in description

    //less important
    //TODO: Restyle MessageActivity
    //TODO: Restyle application colors
    //TODO: Add application icon
    //TODO: Delete redundant code from DatabaseHelper
    //TODO: Disable a possibility to open two messages at once
    //TODO: Add toolbar back
    //TODO: Add toolbar edit
    //Retrofit requires at minimum Java 8+ or Android API 21+.

    RecyclerView recyclerView;
    List<MessageModel> messageListResponse;
    DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView1);
        messageListResponse = new ArrayList<>();

        initDB();

        if (database.size() < 1) {
            //SafeKiddo Api
            //https://run.mocky.io/v3/6125f2d0-0688-4547-aae8-0295d984f196
            Log.i("SafeKiddo", "Hello");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://run.mocky.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            MessageApi messageApi = retrofit.create(MessageApi.class);
            Call<JsonMessage> call = messageApi.getMessages();

            call.enqueue(new Callback<JsonMessage>() {
                @Override
                public void onResponse(Call<JsonMessage> call, Response<JsonMessage> response) {

                    if (response.code() != 200) { //HTTP Code 200 equals to OK
                        Log.i("SafeKiddo", "Code is NOT HTTP Get OK, Code: " + response.code());
                    } else if (response.code() == 200) {
                        Log.i("SafeKiddo", "Got an API response");
                        JsonMessage jsonMessage = response.body();
                        messageListResponse = new ArrayList<>(Arrays.asList(jsonMessage.getPosts())); //get posts array from Api

                        for (int i = 0; i < messageListResponse.size(); i++) {
                            Log.i("SafeKiddo", "ID: " + messageListResponse.get(i).getId() + " | title: " + messageListResponse.get(i).getTitle() + " | Icon: " + messageListResponse.get(i).getIcon());
                            database.addData(i, messageListResponse.get(i).getId() + "", messageListResponse.get(i).getTitle(), messageListResponse.get(i).getDescription(), messageListResponse.get(i).getIcon(), "");
                            Log.i("SafeKiddo", "Data stored to database successfully");
                        }
                        populateRecyclerView(messageListResponse);
                    }
                }

                @Override
                public void onFailure(Call<JsonMessage> call, Throwable t) {
                    Log.i("SafeKiddo", "Data call to API failed: " + t);
                }
            });
        } else {
            for (int i = 0; i < database.size(); i++) {
                if (database.checkIfExists(i)) {
                    messageListResponse.add(database.getIfExists(i));
                }
            }
            populateRecyclerView(messageListResponse);
            Log.i("SafeKiddo", "Data successfully loaded from database and populated into RecyclerView");
        }

    }

    private void populateRecyclerView(List<MessageModel> messageList) {
        AdapterRecyclerView adapter = new AdapterRecyclerView(this, messageList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onMessageClick(int position) {
        //create message activity
        Toast.makeText(this, position + " clicked | title: " + messageListResponse.get(position).getTitle(), Toast.LENGTH_SHORT);
        Log.i("SafeKiddo", "Position nr: " + position + " clicked | id: " + messageListResponse.get(position).getId() + " | title: " + messageListResponse.get(position).getTitle());

        Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
        intent.putExtra("messageModel", messageListResponse.get(position));
        startActivity(intent);
    }

    public void initDB() {
        try {
            database = new DatabaseHelper(this);
            Log.i("SafeKiddo", "Entered INIT of SQLite Database | Database size: " + database.size());
            Log.i("SafeKiddo", "___________Saved_IN_Local_Database___________");
            //log all data stored in database
            for (int i = 0; i < database.size(); i++) {
                if (database.checkIfExists(i)) {
                    Cursor abcd = database.getIfExistsExtended(i);
                    Log.i("SafeKiddo", "ID in DB: " + abcd.getString(0) + " | title: " + abcd.getString(1) + " | description: " + abcd.getString(2));
                }
            }
        } catch (Exception e) {
            Log.i("SafeKiddo", "Exception: " + e);
        }
    }
}