package com.bsotniczuk.safekiddoapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bsotniczuk.safekiddoapp.datamodel.MessageModel;
import com.bumptech.glide.Glide;

public class MessageActivity extends AppCompatActivity {

    EditText textView1; //title
    EditText textView2; //description
    TextView textView3; //number of characters
    TextView textViewTitle; //title help when adding a message
    TextView textViewDescription; //description help when adding a message
    MenuItem actionEdit;
    MenuItem actionDone;
    MenuItem actionClear;
    LinearLayout addPhotoLayout;
    ImageView imageView;

    MessageState messageState;
    MessageModel messageModel;

    String imageUri;

    DatabaseHelper database;

    int position;

    private static final int PICK_IMAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MainActivity.messageActivityOpened > 1)
            finish(); //ensure that only one MessageActivity is opened

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = new DatabaseHelper(this);

        //set application state
        //if position is higher than -1 that means that a MessageModel object has been sent from MainActivity, if position is -2, that means that user wants to add an object using MessageActivity class and its resources, I chose that approach not to add another PutExtra
        position = getIntent().getIntExtra("position", -1);
        if (position > -1) messageState = MessageState.STANDARD_STATE;
        else if (position == -2) messageState = MessageState.ADD_NEW_MESSAGE;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_activity, menu);
        actionEdit = menu.findItem(R.id.action_edit);
        actionDone = menu.findItem(R.id.action_done);
        actionClear = menu.findItem(R.id.action_clear);

        initViews();

        return true;
    }

    private void initViews() {
        textView1 = findViewById(R.id.textView1Message);
        textView2 = findViewById(R.id.textView2Message);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        textView3 = findViewById(R.id.textView3Message);
        imageView = findViewById(R.id.imageViewMessage);
        addPhotoLayout = findViewById(R.id.addPhotoLayout);

        if (messageState == MessageState.STANDARD_STATE) {

            messageModel = getIntent().getParcelableExtra("messageModel");

            Glide.with(this)
                    .load(messageModel.getIcon())
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
            imageUri = messageModel.getIcon();
            textView1.setText(messageModel.getTitle());
            String description = messageModel.getDescription();
            textView2.setText(description);
            textView3.setText(getCharCountString(description));
        } else if (messageState == MessageState.ADD_NEW_MESSAGE) { //ADD_NEW_MESSAGE mode using MessageActivity Resources
            imageUri = null;
            textViewTitle.setVisibility(View.VISIBLE);
            textViewDescription.setVisibility(View.VISIBLE);
            textView3.setText("");
            textView2.setBackground(getDrawable(R.drawable.rounded_style));
        }
        if (messageState == MessageState.STANDARD_STATE) disableEditMode();
        else if (messageState == MessageState.ADD_NEW_MESSAGE) enableEditMode(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            enableEditMode(true);
            return true;
        } else if (id == R.id.action_done) {
            if (messageState == MessageState.MESSAGE_EDIT_STATE) {
                if (imageUri != null) messageModel.setIcon(imageUri.toString());
                notifyChangesToMain();
                updateMessage(position);
                disableEditMode();
            } else if (messageState == MessageState.ADD_NEW_MESSAGE) {
                performAddState();
            }
            return true;
        } else if (id == R.id.action_clear) { //this action is visible only in add_new_message state
            setResult(RESULT_CANCELED, getIntent());
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMessage(int position) {
        if (database.checkIfExists(position)) {
            database.update(position + "", messageModel.getId() + "", textView1.getText().toString(), textView2.getText().toString(), messageModel.getIcon(), "");
        }
    }

    @Override
    public void onBackPressed() {
        if (messageState != MessageState.ADD_NEW_MESSAGE) {
            performEditOrStandardState();
        } else if (messageState == MessageState.ADD_NEW_MESSAGE) {
            performAddState();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData().toString();
            Glide.with(this)
                    .load(imageUri)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        }
    }

    public void buttonNewPictureClick(View view) {
        int currentPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (currentPermission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        else if (currentPermission == PackageManager.PERMISSION_GRANTED) {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(gallery, PICK_IMAGE);
        }
    }

    private void performEditOrStandardState() {
        boolean wasDataEdited = messageModel.getTitle().compareToIgnoreCase(textView1.getText().toString()) != 0 || messageModel.getDescription().compareToIgnoreCase(textView2.getText().toString()) != 0 || messageModel.getIcon().compareToIgnoreCase(imageUri) != 0;
        if (wasDataEdited) {
            notifyChangesToMain();
            messageModel.setIcon(imageUri);
            messageModel.setTitle(textView1.getText().toString()); //I am doing this because if user changed data once -> clicked ok ->
            messageModel.setDescription(textView2.getText().toString()); //-> didn't change data second time -> clicked back: the pop-up would still ask if he wants to save the changes even though it didn't change the second time
            //check if application is in edit state and if user committed any changes to original data
            if (messageState == MessageState.MESSAGE_EDIT_STATE && wasDataEdited) {
                popUp(getString(R.string.want_to_save), getString(R.string.yes), getString(R.string.no));
            } else finish();
        } else finish();
    }

    private void performAddState() {
        boolean wasEdited = textView1.getText().toString().compareToIgnoreCase("") != 0 || textView2.getText().toString().compareToIgnoreCase("") != 0 || imageUri != null;

        if (!wasEdited) { //if message wasn't added
            textViewTitle.setText(getResources().getString(R.string.title) + " (" + getString(R.string.message_cannot_be_empty) + ")");
            textViewTitle.setTextColor(Color.RED);
        } else addMessage();
    }

    private void notifyChangesToMain() {
        Intent intent = getIntent();
        intent.putExtra("position", position);
        setResult(RESULT_OK, intent);
    }

    //set on imageView changed
    private void setImageIfChanged() {
        if (imageUri != null) {
            messageModel.setIcon(imageUri.toString());
        }
    }

    private boolean addMessage() {
        boolean wasEdited = textView1.getText().toString().compareToIgnoreCase("") != 0 || textView2.getText().toString().compareToIgnoreCase("") != 0 || imageUri != null/*messageModel.getIcon().compareToIgnoreCase("") != 0*/;
        if (wasEdited) {
            position = database.getLastId() + 1;
            //set the message model that will be added to db and passed to mainactivity
            messageModel = new MessageModel(position + 1, textView1.getText().toString(), textView2.getText().toString(), "");
            if (imageUri != null) messageModel.setIcon(imageUri);
            else imageUri = "";

            database.addData(position, messageModel.getId() + "", messageModel.getTitle(), messageModel.getDescription(), messageModel.getIcon(), "");
            notifyChangesToMain();
            Intent intent = getIntent();
            intent.putExtra("messageModel", messageModel);

            disableEditMode();
            return true;
        } else return false;
    }

    private void enableEditMode(boolean isEditMode) {
        if (isEditMode) {
            messageState = MessageState.MESSAGE_EDIT_STATE;
            actionClear.setVisible(false);
        } else {
            messageState = MessageState.ADD_NEW_MESSAGE;
            actionClear.setVisible(true);
        }
        addPhotoLayout.setVisibility(View.VISIBLE);
        actionDone.setVisible(true);
        actionEdit.setVisible(false);
        enableEditText(textView1, true);
        enableEditText(textView2, false);

        if (textView1.getText().toString().length() < 15) textViewTitle.setVisibility(View.VISIBLE);
        if (textView2.getText().toString().length() < 15)
            textViewDescription.setVisibility(View.VISIBLE);

        showKeyboard(textView1);
    }

    private void disableEditMode() {
        textViewTitle.setText(getResources().getString(R.string.title));
        textViewTitle.setTextColor(Color.BLACK);
        addPhotoLayout.setVisibility(View.GONE);
        textView3.setVisibility(View.VISIBLE);
        textView3.setText(getCharCountString(textView2.getText().toString()));
        messageState = MessageState.STANDARD_STATE;
        textViewTitle.setVisibility(View.GONE);
        textViewDescription.setVisibility(View.GONE);
        if (!actionEdit.isVisible()) actionEdit.setVisible(true);
        if (actionDone.isVisible()) actionDone.setVisible(false);
        if (actionClear.isVisible()) actionClear.setVisible(false);
        disableEditText(textView1, true);
        disableEditText(textView2, false);
        textViewTitle.setVisibility(View.GONE);
        textViewDescription.setVisibility(View.GONE);
    }

    private void disableEditText(EditText editText, boolean setBackground) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
        if (setBackground)
            editText.setBackground(getResources().getDrawable(R.drawable.rounded_style));
    }

    private void enableEditText(EditText editText, boolean setBackground) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setEnabled(true);
        editText.setCursorVisible(true);
        editText.setKeyListener(new EditText(getApplicationContext()).getKeyListener());
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
        if (setBackground) editText.setBackground(getDrawable(R.drawable.rounded_style));
        editText.setBackground(getDrawable(R.drawable.edit_text_style));
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

    public void popUp(String title, String buttonPositiveText, String buttonNegativeText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        //disableEditMode();

        builder.setPositiveButton(buttonPositiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateMessage(position);
                disableEditMode();
            }
        });
        builder.setNegativeButton(buttonNegativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //reset data as they were originally, not if else because both could be changed, not working properly
                if (messageModel.getTitle().compareToIgnoreCase(textView1.getText().toString()) != 0)
                    textView1.setText(messageModel.getTitle());
                if (messageModel.getDescription().compareToIgnoreCase(textView2.getText().toString()) != 0)
                    textView2.setText(messageModel.getDescription());
                if (messageModel.getIcon().compareToIgnoreCase(imageUri) != 0)
                    imageUri = messageModel.getIcon();
                Log.i("SafeKiddo", "TextView text: " + textView2.getText().toString() + " | description before: " + messageModel.getDescription());
                disableEditMode();
            }
        });
        builder.show();
    }

    private String getCharCountString(String description) {
        int count = 0;
        for (int i = 0; i < description.length(); i++) {
            if (description.charAt(i) != ' ')
                count++;
        }
        String toDisplay = getString(R.string.number_of_chars_wo_space) + ": " + count + "\n" + getString(R.string.number_of_chars) + ": " + description.length();
        return toDisplay;
    }
}