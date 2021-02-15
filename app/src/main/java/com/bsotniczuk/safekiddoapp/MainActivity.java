package com.bsotniczuk.safekiddoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    //less important
    //TODO: Delete redundant code from DatabaseHelper
    //Retrofit requires at minimum Java 8+ or Android API 21+, this App requires API 23+ in spite of checkSelfPermission()

    RecyclerView recyclerView;
    AdapterRecyclerView adapter;
    List<MessageModel> messageListResponse;
    DatabaseHelper database;

    private static final int MESSAGE_ACTIVITY_REQUEST_CODE = 0;
    private static final int MESSAGE_ACTIVITY_ADD_REQUEST_CODE = 1;
    public static int messageActivityOpened = 0; //not elegant but works perfectly, singleTop can be cheated without that, now user cannot open three MessageActivities at once

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbarMainActivity);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView1);
        messageListResponse = new ArrayList<>();

        database = new DatabaseHelper(this);

        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (database.size() < 1 && isConnected) {
            fetchApiData();
        } else if (database.size() > 0) {
            for (int i = 0; i < database.size(); i++) {
                if (database.checkIfExists(i)) {
                    messageListResponse.add(database.getIfExists(i));
                }
            }
            populateRecyclerView(messageListResponse);
        } else if (!isConnected) {
            Toast.makeText(this, "Proszę włączyć sieć i przeładować aplikację", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchApiData() {
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
                    JsonMessage jsonMessage = response.body();
                    messageListResponse = new ArrayList<>(Arrays.asList(jsonMessage.getPosts())); //get posts array from Api

                    for (int i = 0; i < messageListResponse.size(); i++) {
                        database.addData(i, messageListResponse.get(i).getId() + "", messageListResponse.get(i).getTitle(), messageListResponse.get(i).getDescription(), messageListResponse.get(i).getIcon(), "");
                    }
                    populateRecyclerView(messageListResponse);
                }
            }

            @Override
            public void onFailure(Call<JsonMessage> call, Throwable t) {
                Log.i("SafeKiddo", "Data call to API failed: " + t);
            }
        });
    }

    private void populateRecyclerView(List<MessageModel> messageList) {
        adapter = new AdapterRecyclerView(this, messageList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onMessageClick(int position) {
        //create message activity
        Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
        intent.putExtra("messageModel", messageListResponse.get(position));
        intent.putExtra("position", position);

        messageActivityOpened++;
        startActivityForResult(intent, MESSAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            if (database.size() > 0) {
                Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                intent.putExtra("position", -2);
                messageActivityOpened++;
                startActivityForResult(intent, MESSAGE_ACTIVITY_ADD_REQUEST_CODE);
            } else
                Toast.makeText(this, "Proszę włączyć sieć i załadować dane z API", Toast.LENGTH_LONG).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        messageActivityOpened = 0;
        if (requestCode == MESSAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra("position", -1);
                messageListResponse.remove(position);
                messageListResponse.add(position, database.getIfExists(position));
                adapter.notifyItemChanged(position);
                Log.i("SafeKiddo", "MainNotified: " + position);
            }
        } else if (requestCode == MESSAGE_ACTIVITY_ADD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra("position", -1); //when adding, getting a position is redundant because program always adds to the end
                MessageModel messageModel = data.getParcelableExtra("messageModel");

                messageListResponse.add(messageModel);
                adapter.notifyItemInserted(messageListResponse.size());
            } else if (resultCode == RESULT_CANCELED) Log.i("SafeKiddo", "Result Canceled");
        }
    }
}