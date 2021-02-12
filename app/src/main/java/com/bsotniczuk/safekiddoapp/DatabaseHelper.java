package com.bsotniczuk.safekiddoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bsotniczuk.safekiddoapp.datamodel.MessageModel;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "message_table";
    private static final String COL1 = "ID"; //id primary key
    private static final String COL2 = "id_of_message"; //idOfMessage
    private static final String COL3 = "title"; //Title
    private static final String COL4 = "description"; //Description
    private static final String COL5 = "icon"; //Icon Address
    private static final String COL6 = "image_file"; //Icon Image file or Image File in case file inside the URL gets deleted


    public DatabaseHelper(Context context)//, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY, " + COL2 + " TEXT, " + COL3 + " TEXT, " + COL4 + " TEXT, " + COL5 + " TEXT, " + COL6 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL4 + " TEXT");

        Log.d(TAG, "onUpgrade: data altered");
    }

    public boolean addData(int ID, String item1, String item2, String item3, String item4, String item5){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, ID);
        contentValues.put(COL2, item1);
        contentValues.put(COL3, item2);
        contentValues.put(COL4, item3);
        contentValues.put(COL5, item4);
        contentValues.put(COL6, item5);
        Log.d(TAG, "addData: Adding " + item1 + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) return false;
        else return true;
    }

    public boolean deleteItem(String item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item);

        Log.d(TAG, "addData: Deleting " + item + " from " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        String whereArgs="name LIKE '%"+item+"%";

        db.delete(TABLE_NAME, COL2, new String[] {item});

        if(result==-1)
            return false;
        else
            return true;
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public Cursor getIfExists(String item)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT " + COL1 + ", " + COL2 + ", " + COL3 + ", " + COL4 + ", " + COL5 + ", " + COL6 +
                " FROM " + TABLE_NAME +
                " WHERE " + COL2 + " = '" + item + "'", null);
        data.moveToFirst();

        return data;
    }

    public MessageModel/*Cursor*/ getIfExists(int ID) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT " + COL1 + ", " + COL2 + ", " + COL3 + ", " + COL4 + ", " + COL5 + ", " + COL6 +
                " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = " + ID + "", null);
        data.moveToFirst(); //necessary

        MessageModel messageModel = new MessageModel();
        messageModel.setId(parseInt(data.getString(1)));
        messageModel.setTitle(data.getString(2));
        messageModel.setDescription(data.getString(3));
        messageModel.setIcon(data.getString(4));

        //return data;
        return messageModel;
    }

    public Cursor getIfExistsExtended(int ID) { //get the whole record with database id
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT " + COL1 + ", " + COL2 + ", " + COL3 + ", " + COL4 + ", " + COL5 + ", " + COL6 +
                " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = " + ID + "", null);
        data.moveToFirst(); //necessary

        return data;
    }

    public boolean checkIfExists(String item) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT " + COL1 + ", " + COL2 + ", " + COL3 + ", " + COL4 + ", " + COL5 + ", " + COL6 +
                " FROM " + TABLE_NAME +
                " WHERE " + COL2 + " = '" + item + "'", null);

        if (data.moveToFirst()) return true;
        else return false;
    }

    public boolean checkIfExists(int ID) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT " + COL1 + ", " + COL2 + ", " + COL3 + ", " + COL4 + ", " + COL5 + ", " + COL6 +
                " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = " + ID + "", null);

        if (data.moveToFirst()) return true;
        else return false;
    }

    public int size() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT COUNT(" + COL1 + ") " +
                " FROM " + TABLE_NAME, null);

        if (data.moveToFirst()) {
            return parseInt(data.getString(0));
        }
        else return -1;
    }

    public int update(String id, String idOfMessage, String title, String description, String icon, String imageFile) //it is basically updateRecord instead of updateUserInfo
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL2, idOfMessage);
        contentValues.put(COL3, title);
        contentValues.put(COL4, description);
        contentValues.put(COL5, icon);
        contentValues.put(COL6, imageFile);

        String whereStatement = COL1 + "=" + id;

        int toReturn = db.update(TABLE_NAME, contentValues, whereStatement,null);

        return toReturn;
    }

    public Cursor getLastFromDB() {
        //SELECT * FROM Table ORDER BY ID DESC LIMIT 1
        SQLiteDatabase db = this.getWritableDatabase();

        String sqlStatement = "SELECT * FROM " + TABLE_NAME + " ORDER BY ID DESC LIMIT 1";

        Cursor data = db.rawQuery(sqlStatement, null);
        return data;
    }

    private int parseInt(String toParse) {
        int idInt;
        try { idInt = Integer.parseInt(toParse); }
        catch (NumberFormatException e) { idInt = -2; } //error in parsing
        return idInt;
    }
}