package com.acelerem.android.inventory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.acelerem.android.inventory.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the Inventory data loader */
    private static final int EXISTING_INVENTORY_LOADER = 0;


    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     *
     */

    private ImageView mImageView;

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

    // Variable to manage IntentforAction

    /**
     * Variables to handle image loading
     */

    /**
     * Request code for onActivityResult intent
     */
    private static final int PICK_IMAGE_REQUEST = 0;

    /**
     * Selected Image Uri
     */
    private Uri mImageUri;
    private static final String STATE_URI = "STATE_URI";

    /**
     * Intent for email to supplier
     */
    private static final int SEND_MAIL_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        final Intent intent = getIntent();
        mCurrentUri = intent.getData();

        // If the intent DOES NOT contain an item content URI, then we know that we are
        // creating a new pet.
        if (mCurrentUri == null) {
            // This is a new item, so change the ba to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change the app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        // Load item image
        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        // Find all relevant views that will be needed to read user input from
        mImageView = (ImageView) findViewById(R.id.product_image);
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_item_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQtyEditText = (EditText) findViewById(R.id.edit_item_amount);
        mEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);

    }

    // Make sure the Image is not lost before saving if the  user moves the phone or tablet

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null)
            outState.putString(STATE_URI, mImageUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_URI));

            ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
                }
            });
        }
    }


    private boolean checkItem() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String qtyString = mQtyEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();

        if (nameString == "" || TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.error_empty_name), Toast.LENGTH_SHORT ).show();
            return false;
        }
        // If the price or amount are not provided, don't try to parse the string. Use 0

        double price = 0.0;

        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        int amount = 0;

        if (!TextUtils.isEmpty(qtyString)) {
            amount = Integer.parseInt(qtyString);

        }

        // Check the price or amount are not negative
        if ( price < 0 || amount < 0) {
            Toast.makeText(this, getString(R.string.invalid_price_or_amount), Toast.LENGTH_SHORT ).show();
            return false;
        }

        // Check the email is valid

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if ( !emailString.matches(emailPattern)) {

            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT ).show();
            return false;

        }

        return true;

    }

    /**
     * Get user input from editor and save new pet into database.
     */
    private boolean saveItem() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String imageUriString = "";

        if (mImageUri != null) {
            imageUriString = mImageUri.toString();
        }

        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String qtyString = mQtyEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();


        // If the price or amount are not provided, don't try to parse teh string. Use 0

        double price = 0.0;

        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        int amount = 0;

        if (!TextUtils.isEmpty(qtyString)) {
            amount = Integer.parseInt(qtyString);

        }

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(qtyString) &&
                TextUtils.isEmpty(emailString)){
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return true;
        }

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, imageUriString);
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_DESCRIPTION, descriptionString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, price);
        values.put(InventoryEntry.COLUMN_ITEM_QTY, amount);
        values.put(InventoryEntry.COLUMN_ITEM_EMAIL, emailString);

        // Determine if this is a new or existing pet by checking if mCurrentUri is null or not

        if (mCurrentUri == null ) {

            // Insert a new item into the provider, returning the content URI for the new item
            Uri newURI = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newURI == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {

            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
                return false;

            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
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
                // Check if the values are valid
                if (checkItem()){
                    // Save pet to database
                    if (saveItem()) {
                        finish();
                        return true;
                    }
                }
                break;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
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
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_DESCRIPTION,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QTY,
                InventoryEntry.COLUMN_ITEM_EMAIL,
        };

        // This loader will execute the ContentProvider's query method on a background thread

        return new CursorLoader(this,   // Parent activity context
                mCurrentUri,             // Query the content URI for the current item
                projection ,            // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null                    // Default sort order
                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount()<1 ) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of Item attributes that we're interested in
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_DESCRIPTION);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int amountColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QTY);
            int emailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_EMAIL);

            // Extract out the value from the Cursor for the given column index
            final Uri image = Uri.parse(cursor.getString(imageColumnIndex));
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String amount = cursor.getString(amountColumnIndex);
            String email = cursor.getString(emailColumnIndex);

            Log.i("ImageUri", "Image Uri is " + image.toString());

            // Get the image from the Uri
            //Bitmap bitmap = getBitmapFromUri(image);

            ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageView.setImageBitmap(getBitmapFromUri(image));
                    mImageUri = image;
                }
            });

            // Update the view on the screen wit the values from the database
            //mImageView.setImageBitmap(bitmap);
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(price);
            mQtyEditText.setText(amount);
            mEmailEditText.setText(email);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mImageView.setImageResource(android.R.color.transparent);
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText("");
        mQtyEditText.setText("");
        mEmailEditText.setText("");

    }

    private void deleteItem() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK ) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            if (resultData != null) {
                mImageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
            }

        } else if ( requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT).show();
        }
    }

    public void openImageSelector() {

        // Check permissions needed

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            verifyStoragePermissions(this);
        } else {


            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            intent.setType("image/*");
            //intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);

            //intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            //intent.addCategory(Intent.CATEGORY_OPENABLE);
            //intent.setType("image/*");
            //startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


    // This part handles the dynamic permission needd for the las

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    // Methods to add and subtract from the quantity at hand

    public void AddAmount(View view) {

        int amount = Integer.parseInt(mQtyEditText.getText().toString().trim());
        amount++;

        mQtyEditText.setText(String.valueOf(amount));

    }

    public void SubtractAmount(View view) {

        int amount = Integer.parseInt(mQtyEditText.getText().toString().trim());

        if (amount <1 ){
            Toast.makeText(this, getString(R.string.amount_error), Toast.LENGTH_SHORT ).show();
        } else {
            amount--;
        }

        mQtyEditText.setText(String.valueOf(amount));
    }

    // Method to send order to supplier

    public void sendEmail(View view) {
        if (mCurrentUri != null) {

            String email = mEmailEditText.getText().toString().trim();
            String name =mNameEditText.getText().toString().trim();


            String subject = "Order for " + name;
            String stream = "Hello! \n"
                    + "Please send to our address the following amount XXX " + ".\n"
                    + "of " + name + "\n" + "\n"
                    + "Best Regards," + "\n" + "\n"
                    + "Your customer"
                    ;

            /* Create intent */
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);

            /* Fill with data */
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, stream);

            /* Send it off to the Activity-chooser */
            startActivityForResult(Intent.createChooser(emailIntent, "Send email"), SEND_MAIL_REQUEST);

        } else {
            return;
        }
    }

    public void deleteButton(View view) {

        showDeleteConfirmationDialog();
        deleteItem();

    }


}
