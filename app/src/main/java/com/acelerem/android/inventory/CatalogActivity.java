package com.acelerem.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.acelerem.android.inventory.data.InventoryContract.InventoryEntry;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    /* This is the adapter being used to display the list data */
    private InventoryCursorAdapter mInventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab= (FloatingActionButton) findViewById(R.id.add_product);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView inventoryListView = (ListView) findViewById(R.id.list_items);

        // Setup and adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mInventoryCursorAdapter);

        // Setup the item click listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the uri would "content://com.example.android.items/items/2"
                // if the item with ID 2 was clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field on the intent
                intent.setData(currentItemUri);

                Log.i("OnClickListener", "I listen");

                // Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
            }


        });


        // Kick off the loader
        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu/menu_catalog.xml file
        // This add menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;

    }

    private void insertItem() {

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, "Table");
        values.put(InventoryEntry.COLUMN_ITEM_DESCRIPTION, "Very nice one");
        values.put(InventoryEntry.COLUMN_ITEM_QTY, 50);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE,20);
        values.put(InventoryEntry.COLUMN_ITEM_EMAIL,"prueba@gmail.com");

        // Insert operation using PetProvider method
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertItem();
                // Do nothing for now
                return true;
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int Loaderid, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QTY,
        };

        // This loader will execute the
        return new CursorLoader(
                this,                       // Activity context
                InventoryEntry.CONTENT_URI,       // Table to query
                projection,                 // Projection to return
                null,                       // No selection clause
                null,                       // No selection arguments
                null                        // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mInventoryCursorAdapter.swapCursor( data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mInventoryCursorAdapter.changeCursor(null);

    }
}
