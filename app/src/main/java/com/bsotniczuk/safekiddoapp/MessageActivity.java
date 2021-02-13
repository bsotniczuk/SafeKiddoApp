package com.bsotniczuk.safekiddoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsotniczuk.safekiddoapp.datamodel.MessageModel;
import com.bumptech.glide.Glide;

public class MessageActivity extends AppCompatActivity {

    EditText textView1;
    EditText textView2;
    MenuItem actionEdit;
    MenuItem actionDone;
    MessageState messageState;
    MessageModel messageModel;

    int idInDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        messageState = MessageState.STANDARD_STATE;

        Intent intent = getIntent();
        messageModel = intent.getParcelableExtra("messageModel");
        idInDatabase = intent.getIntExtra("idInDatabase", -1);
        Log.i("SafeKiddo", "id of msg in DB: " + idInDatabase + " | MessageModel acquired from the MainActivity - ID: " + messageModel.getId() + " | title: " + messageModel.getTitle());

        textView1 = findViewById(R.id.textView1Message);
        textView2 = findViewById(R.id.textView2Message);
        TextView textView3 = findViewById(R.id.textView3Message);
        ImageView imageView = findViewById(R.id.imageViewMessage);

        Glide.with(this)
                .load(messageModel.getIcon())
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView);
        textView1.setText(messageModel.getTitle());
        String description = messageModel.getDescription();
        textView2.setText(description);

        int count = 0;
        for (int i = 0; i < description.length(); i++) {
            if (description.charAt(i) != ' ')
                count++;
        }
        String toDisplay = "Ilość znaków w opisie (bez spacji): " + count + "\nIlość wszystkich znaków: " + description.length();
        textView3.setText(toDisplay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_activity, menu);
        actionEdit = menu.findItem(R.id.action_edit);
        actionDone = menu.findItem(R.id.action_done);
        disableEditMode();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Log.i("SafeKiddo", "Action Edit pressed");
            enableEditMode();
            return true;
        }
        else if (id == R.id.action_done) {
            Log.i("SafeKiddo", "Action Done pressed");
            disableEditMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void enableEditMode() {
        messageState = MessageState.MESSAGE_EDIT_STATE;
        actionDone.setVisible(true);
        actionEdit.setVisible(false);
        enableEditText(textView1, true);
        enableEditText(textView2, false);
        showKeyboard(textView1);
        Log.i("SafeKiddo", "MessageState: " + messageState);
    }

    private void disableEditMode() {
        messageState = MessageState.STANDARD_STATE;
        if (!actionEdit.isVisible()) actionEdit.setVisible(true);
        if (actionDone.isVisible()) actionDone.setVisible(false);
        disableEditText(textView1, true);
        disableEditText(textView2, false);
        Log.i("SafeKiddo", "MessageState: " + messageState);
    }

    private void disableEditText(EditText editText, boolean bool) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
        if (bool == true) editText.setBackground(getResources().getDrawable(R.drawable.rounded_style));
    }

    private void enableEditText(EditText editText, boolean bool) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setEnabled(true);
        editText.setCursorVisible(true);
        editText.setKeyListener(new EditText(getApplicationContext()).getKeyListener());
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
        if (bool == true) editText.setBackground(getResources().getDrawable(R.drawable.rounded_style));
    }

    public void showKeyboard(final EditText editText) {
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.setSelection(editText.getText().length());
                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(editText, 0);
            }
        }, 150);
    }
}