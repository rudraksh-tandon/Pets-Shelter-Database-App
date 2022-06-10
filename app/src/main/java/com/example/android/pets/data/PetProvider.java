package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.net.ConnectException;
import java.util.IllformedLocaleException;

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDbHelper mDbHelper;
    public static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PETS = 1;
    private static final int PET_ID = 2;

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                cursor = db.query(PetContract.petEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                break;
            case PET_ID:
                selection = PetContract.petEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetContract.petEntry.TABLE_NAME, projection, selection, selectionArgs,null,null,null);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
//        set notification uri on the cursor so we know what content uri the cursor was created for
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return PetContract.petEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.petEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        if(match == PETS){
            return insertPet(uri, contentValues);
        }
        else{
            throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues contentValues){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long newRowId = db.insert(PetContract.petEntry.TABLE_NAME, null, contentValues);

//        sanity check of data before insertion
        String name = contentValues.getAsString(PetContract.petEntry.COLUMN_NAME);
        String breed = contentValues.getAsString(PetContract.petEntry.COLUMN_BREED);
        int gender = contentValues.getAsInteger(PetContract.petEntry.COLUMN_GENDER);
        int weight = contentValues.getAsInteger(PetContract.petEntry.COLUMN_WEIGHT);
        if(name == null){
            throw new IllegalArgumentException("Pet requires a name");
        }
        if(!PetContract.petEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        if(weight < 0){
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        if(newRowId == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
//        notify all listeners that the data has been updated
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match){
            case PETS:
                rowsDeleted = db.delete(PetContract.petEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                selection = PetContract.petEntry._ID + "=?";
                selectionArgs = new String[] {String .valueOf(ContentUris.parseId(uri))};
                rowsDeleted =  db.delete(PetContract.petEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("delete not supported for " + uri);
        }
        if(rowsDeleted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                selection = PetContract.petEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("update not supported for "+uri);
        }
    }
    private int updatePet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs){
        if(contentValues.containsKey(PetContract.petEntry.COLUMN_NAME)){
            String name = contentValues.getAsString(PetContract.petEntry.COLUMN_NAME);
            if(name == null){
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if(contentValues.containsKey(PetContract.petEntry.COLUMN_GENDER)){
            int gender = contentValues.getAsInteger(PetContract.petEntry.COLUMN_GENDER);
            if (!PetContract.petEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }
        if(contentValues.containsKey(PetContract.petEntry.COLUMN_WEIGHT)){
            int weight = contentValues.getAsInteger(PetContract.petEntry.COLUMN_WEIGHT);
            if(weight < 0){
                throw new IllegalArgumentException("Pet requires a weight");
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated =  db.update(PetContract.petEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if(rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}