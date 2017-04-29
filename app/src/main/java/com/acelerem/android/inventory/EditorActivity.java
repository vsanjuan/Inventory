package com.acelerem.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.acelerem.android.inventory.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity {

    /**
     * EditText field to enter the item's name
     */

    private EditText mNameEditText;

    /**
     * EditText field to enter the item's description
     */

    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the item's quantity
     */

    private EditText mQtyEditText;

    /**
     * EditText field to enter the item's price
     */

    private EditText mPriceEditText;

    /**
     * EditText field to enter the item's supplier email
     */

    private EditText mEmailEditText;

    /**
     * Uri field for the current record
     **/
    private Uri mCurrentUri;

    /**
     * Follow if the app has changed
     **/
    private boolean mItemHasChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that will be needed to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_item_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQtyEditText = (EditText) findViewById(R.id.edit_item_amount);
        mEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);

    }

    /**
     * Get user input from editor and save new pet into database.
     */
    private void insertItem() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String qtyString = mQtyEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();

        long price = Long.parseLong(priceString);
        int amount = Integer.parseInt(qtyString);

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_DESCRIPTION, descriptionString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, price);
        values.put(InventoryEntry.COLUMN_ITEM_QTY, amount);
        values.put(InventoryEntry.COLUMN_ITEM_EMAIL, emailString);

        // Insert a new item into the provider, returning the content URI for the new item
        Uri newURI = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newURI == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                    Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                insertItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                //showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }


                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };


                // Show a dialog that notifies the user they have unsaved changes
                //showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
