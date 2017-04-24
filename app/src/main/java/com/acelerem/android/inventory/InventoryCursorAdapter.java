package com.acelerem.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.acelerem.android.inventory.data.InventoryContract.InventoryEntry;

import static com.acelerem.android.inventory.R.id.price;

/**
 * Created by Salvador on 24/04/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_items,parent,false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.product_name);
        TextView tvPrice = (TextView) view.findViewById(price);
        TextView tvQty = (TextView) view.findViewById(R.id.current_qty);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_NAME));
        long price = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_PRICE));
        int qty = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_QTY));

        Log.i("Database", "Values are " + name + ", " + String.valueOf(price) + ", " +  String.valueOf(qty));

        // Populate the fields with extracted properties
        tvName.setText(name);
        tvPrice.setText(String.valueOf(price));
        tvQty.setText(String.valueOf(qty));

    }
}
