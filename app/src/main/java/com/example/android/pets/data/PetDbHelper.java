package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PetDbHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + PetContract.petEntry.
            TABLE_NAME + " (" + PetContract.petEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PetContract.petEntry.COLUMN_NAME + " TEXT NOT NULL," + PetContract.petEntry.COLUMN_BREED
            + " TEXT," + PetContract.petEntry.COLUMN_GENDER + " INTEGER NOT NULL," +
            PetContract.petEntry.COLUMN_WEIGHT + " INTEGER NOT NULL DEFAULT 0" + " )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + PetContract.petEntry.TABLE_NAME;

    public PetDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        onUpgrade(sqLiteDatabase, oldVer, newVer);
    }
}
