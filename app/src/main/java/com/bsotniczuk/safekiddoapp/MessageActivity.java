package com.bsotniczuk.safekiddoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsotniczuk.safekiddoapp.datamodel.MessageModel;
import com.bumptech.glide.Glide;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent = getIntent();
        MessageModel messageModel = intent.getParcelableExtra("messageModel");
        Log.i("SafeKiddo", "MessageModel acquired from the MainActivity - ID: " + messageModel.getId() + " | title: " + messageModel.getTitle());

        TextView textView1 = findViewById(R.id.textView1Message);
        TextView textView2 = findViewById(R.id.textView2Message);
        TextView textView3 = findViewById(R.id.textView3Message);
        ImageView imageView = findViewById(R.id.imageViewMessage);

        Glide.with(this)
                .load(messageModel.getIcon())
                //.placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView);
        textView1.setText(messageModel.getTitle());
        String description = messageModel.getDescription();
        textView2.setText(description);

        //Counts each character except space
        int count = 0;
        for(int i = 0; i < description.length(); i++) {
            if(description.charAt(i) != ' ')
                count++;
        }
        String toDisplay = "Ilość znaków w opisie: " + count;
        textView3.setText(toDisplay);
    }
}