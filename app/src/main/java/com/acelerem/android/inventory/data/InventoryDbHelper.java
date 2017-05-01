package com.acelerem.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.acelerem.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by Salvador on 14/02/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "items.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of (@Link PetDbHelper).
     *
     * @param context of the app
     *
     */
    public InventoryDbHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    /**
     * This is called when the database is create for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_ITEM_IMAGE + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_DESCRIPTION + " TEXT, "
                + InventoryEntry.COLUMN_ITEM_PICTURE + " TEXT, "
                + InventoryEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_QTY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_EMAIL + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ITEMS_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

}
