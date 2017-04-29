package com.acelerem.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.acelerem.android.inventory.R;
import com.acelerem.android.inventory.data.InventoryContract.InventoryEntry;

import static java.lang.Boolean.FALSE;

/**
 * Created by Salvador on 02/04/2017.
 */

public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * Initialize pets database
     */
    private SQLiteOpenHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int ITEMS = 100;

    /**
     * URI matcher code for the content URI for a single item in the inventory table
     */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // Add 2 content URIs to URI matcher
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);

    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        // Create and initialize a InventoryDbHelper object to gain access to the Inventory database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new InventoryDbHelper(getContext());

        return true;

    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                // Perform database query on pets table
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknow URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match" + match);

        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Notify all listeners that the data has changed for the pet content URI
                getContext().getContentResolver().notifyChange(uri,null);

                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.name_error));
        }

        // Check the description is not null
        String description = values.getAsString(InventoryEntry.COLUMN_ITEM_DESCRIPTION);
        if (name == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.description_error));
        }

        // Check the weight is positive
        int weight = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QTY);
        if (weight < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.amount_error));
        }

        // Check the price is positive
        int price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
        if (price < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.price_error));
        }

        // Check there is a valid email
        String email = values.getAsString(InventoryEntry.COLUMN_ITEM_EMAIL).trim();

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (email.matches(emailPattern) == FALSE) {
            throw new IllegalArgumentException(String.valueOf(R.string.email_error));
        }


        // Insert a new item into the inventory table with the given ContentValues
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        if (newRowId == -1) {
            Log.e(LOG_TAG, "No insertion ");
            return null;
        }

        // Notify all listeners that the data has changed for the inventory content URI
        getContext().getContentResolver().notifyChange(uri,null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, newRowId);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME,selection,selectionArgs);
                // Notify all listeners that the data has changed for the pet content URI
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            case ITEM_ID:

                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME,selection,selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Update the selected pets in the InventoryItems database table with the given ContentValues

        // Check the values before inserting into the database
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null || name == "") {
                throw new IllegalArgumentException(String.valueOf(R.string.name_error));
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_ITEM_DESCRIPTION)) {
            String description = values.getAsString(InventoryEntry.COLUMN_ITEM_DESCRIPTION);

            if (description == null || description == "") {
                throw new IllegalArgumentException(String.valueOf(R.string.description_error));
            }

        }

        if (values.containsKey(InventoryEntry.COLUMN_ITEM_QTY)) {
            int amount = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QTY);
            if (amount < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.amount_error));
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_ITEM_PRICE)) {
            int price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.price_error));
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_ITEM_EMAIL)) {
            String email = values.getAsString(InventoryEntry.COLUMN_ITEM_EMAIL);
            if (email == null || email == "") {
                throw new IllegalArgumentException(String.valueOf(R.string.email_error_null));
            }

            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if (email.matches(emailPattern) == FALSE) {
                throw new IllegalArgumentException(String.valueOf(R.string.email_error));
            }

        }

        // Connect to the database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Update the values
        int numRows = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // Notify all listeners that the data has changed for the pet content URI
        if (numRows != 0 ) {
            getContext().getContentResolver().notifyChange(uri,null);
        }

        // Return the number of rows that were affected
        return numRows;
    }

}

