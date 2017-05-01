package com.acelerem.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Salvador on 05/01/2017.
 *
 * API Contract for the Inventory App
 */

public final class InventoryContract {

    /** Content provider URI String*/
    public static final String CONTENT_AUTHORITY = "com.acelerem.android.inventory";

    /** Content provider URI object */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Items table path */
    public  static final String PATH_ITEMS = "items";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract() {}

    /**
     * Inner class that defines constant values for the items database table.
     * Each entry in the table represents a single item.
     */
    public static final class InventoryEntry implements BaseColumns {

        /** Name of database table for items */
        public final static String TABLE_NAME = "items";

        /** URI constant for the class */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * Unique ID number for the item (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         *  Uri of the image
         *  Type: TEXT
         */

        public final static String COLUMN_ITEM_IMAGE = "image";

        /**
         * Name of the item.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_NAME ="name";

        /**
         * Description of the item.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_DESCRIPTION ="description";

        /**
         * Amount in inventory.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_QTY = "quantity";

        /**
         * Item sale price.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_PRICE = "price";

        /**
         * Item picture. Path to where the picture is stored
         *
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_PICTURE = "picture";

        /**
         * Supplier's email. Where to order the item.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_EMAIL = "email";


        /**
         *  MIME Type of the {@link #CONTENT_URI} for a list of items.
         *  */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_ITEMS;

    }


}
