package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MacIndex Favourite Activity
 * Jan. 15, 2021
 */
public class FavouriteActivity extends AppCompatActivity {

    private int[][] loadPositions = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        this.setTitle(getResources().getString(R.string.menu_favourite));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MainActivity.validateOperation(this);

        isEmptyString(R.string.menu_favourite);
        initFavourites();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // If reload is needed..
        if (PrefsHelper.getBooleanPrefs("isFavouritesReloadNeeded", this)) {
            initFavourites();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_favourite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addFolderItem:
                createFolder();
                break;
            case R.id.deleteFolderItem:
                deleteFolder();
                break;
            case R.id.renameFolderItem:
                renameFolder();
                break;
            case R.id.clearFolderItem:
                final AlertDialog.Builder clearFoldersDialog = new AlertDialog.Builder(this);
                clearFoldersDialog.setTitle(R.string.submenu_favourite_clear);
                clearFoldersDialog.setMessage(R.string.favourites_clear_warning);
                clearFoldersDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.clearPrefs("userFavourites", this);
                    initFavourites();
                });
                clearFoldersDialog.setNegativeButton(R.string.link_cancel, ((dialogInterface, i) -> {
                    // Cancelled.
                }));
                clearFoldersDialog.show();
                break;
            case R.id.favouriteHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/favourites", this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initFavourites() {
        // Reset reload parameter
        PrefsHelper.editPrefs("isFavouritesReloadNeeded", false, this);
        Log.i("initFavourites", PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));

        // Adapt initInterface from MainActivity
        try {
            // Parent layout of all categories.
            final LinearLayout categoryContainer = findViewById(R.id.categoryContainer);
            // Fix an animation bug here
            LayoutTransition layoutTransition = categoryContainer.getLayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            categoryContainer.removeAllViews();
            // Get Folder Names
            final String[] allFolders = getFolders(this, false);
            final String[] splitedString = PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this).split("││");

            final LinearLayout emptyLayout = findViewById(R.id.emptyLayout);
            final TextView emptyText = findViewById(R.id.emptyText);

            if (allFolders.length == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    emptyText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                } else {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(emptyText, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                }
                emptyLayout.setVisibility(View.VISIBLE);
            } else {
                emptyLayout.setVisibility(View.GONE);
            }

            ProgressDialog waitDialog = new ProgressDialog(this);
            waitDialog.setMessage(getString(R.string.loading_favourites));
            waitDialog.setCancelable(false);
            waitDialog.show();
            new Thread() {
                @Override
                public void run() {
                    try {
                        // Get Load Positions
                        loadPositions = new int[allFolders.length][];
                        for (int i = 0; i < allFolders.length; i++) {
                            final String[] thisFolder = splitedString[i + 1].split("│");
                            loadPositions[i] = new int[thisFolder.length - 1];
                            for (int j = 0; j < thisFolder.length - 1; j++) {
                                 final int[] thisID = MainActivity.getMachineHelper().searchHelper("name", thisFolder[j + 1].substring(1, thisFolder[j + 1].length() - 1),
                                        "all", FavouriteActivity.this, true);
                                if (thisID.length != 1) {
                                    Log.e("FavouritesSearchThread", "Error occurred on search string " + thisFolder[j + 1]);
                                }
                                loadPositions[i][j] = thisID[0];
                            }
                            // Is sorting needed?
                            if (PrefsHelper.getBooleanPrefs("isSortComment", FavouriteActivity.this)) {
                                loadPositions[i] = MainActivity.getMachineHelper().directSortByYear(loadPositions[i]);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    waitDialog.dismiss();
                                    // Set up each category.
                                    TextView[][] allMachines = new TextView[loadPositions.length][];
                                    for (int i = 0; i < loadPositions.length; i++) {
                                        final View categoryChunk = getLayoutInflater().inflate(R.layout.chunk_category, null);
                                        final LinearLayout categoryChunkLayout = categoryChunk.findViewById(R.id.categoryInfoLayout);
                                        final TextView categoryName = categoryChunk.findViewById(R.id.category);

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            categoryName.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                                        } else {
                                            TextViewCompat.setAutoSizeTextTypeWithDefaults(categoryName, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                                        }

                                        if (loadPositions[i].length != 0) {
                                            categoryName.setText(allFolders[i]);

                                            /* Remake my teammate's code */
                                            categoryName.setOnClickListener(new View.OnClickListener() {
                                                private boolean thisVisibility = false;

                                                @Override
                                                public void onClick(final View view) {
                                                    try {
                                                        final View firstChild = categoryChunkLayout.getChildAt(1);
                                                        if (thisVisibility) {
                                                            // Make machines invisible.
                                                            if (!(firstChild instanceof LinearLayout)) {
                                                                // Have the divider
                                                                for (int j = 2; j < categoryChunkLayout.getChildCount(); j++) {
                                                                    categoryChunkLayout.getChildAt(j).setVisibility(View.GONE);
                                                                    thisVisibility = false;
                                                                }
                                                                firstChild.setVisibility(View.VISIBLE);
                                                            } else {
                                                                // Does not have the divider
                                                                for (int j = 1; j < categoryChunkLayout.getChildCount(); j++) {
                                                                    categoryChunkLayout.getChildAt(j).setVisibility(View.GONE);
                                                                    thisVisibility = false;
                                                                }
                                                            }
                                                        } else {
                                                            // Make machines visible.
                                                            if (!(firstChild instanceof LinearLayout)) {
                                                                // Have the divider
                                                                for (int j = 2; j < categoryChunkLayout.getChildCount(); j++) {
                                                                    categoryChunkLayout.getChildAt(j).setVisibility(View.VISIBLE);
                                                                    thisVisibility = true;
                                                                }
                                                                firstChild.setVisibility(View.GONE);
                                                            } else {
                                                                // Does not have the divider
                                                                for (int j = 1; j < categoryChunkLayout.getChildCount(); j++) {
                                                                    categoryChunkLayout.getChildAt(j).setVisibility(View.VISIBLE);
                                                                    thisVisibility = true;
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        ExceptionHelper.handleException(FavouriteActivity.this, e, "initFavourites", "Illegal Favourites String. Please reset the application. String is: "
                                                                + PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));
                                                    }
                                                }
                                            });
                                            Log.i("FavouriteActivity", "Loading folder " + allFolders[i]);
                                            allMachines[i] = SpecsIntentHelper
                                                    .initCategory(categoryChunkLayout, loadPositions[i], false, FavouriteActivity.this);
                                            categoryContainer.addView(categoryChunk);
                                        } else {
                                            // Empty folder
                                            categoryName.setText(allFolders[i] + " " + getString(R.string.favourites_new_folder_tips));
                                            categoryContainer.addView(categoryChunk);
                                        }
                                    }
                                    // Remove the last divider.
                                    if (categoryContainer.getChildCount() != 0) {
                                        ((LinearLayout) categoryContainer.getChildAt(categoryContainer.getChildCount() - 1)).removeViewAt(1);
                                    }

                                    // Load the favourites star.
                                    SpecsIntentHelper.refreshFavourites(allMachines, FavouriteActivity.this);
                                } catch (Exception e) {
                                    ExceptionHelper.handleException(FavouriteActivity.this, e, "initFavourites", "Illegal Favourites String. Please reset the application. String is: "
                                            + PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));
                                }
                            }
                        });
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (final Exception e) {
            ExceptionHelper.handleException(FavouriteActivity.this, e, "initFavourites", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));
        }
    }

    // Called when to check the favourites is empty.
    private boolean isEmptyString(final int titleRes) {
        if (PrefsHelper.getStringPrefs("userFavourites", this).isEmpty()) {
            final AlertDialog.Builder emptyStringDialog = new AlertDialog.Builder(this);
            emptyStringDialog.setTitle(titleRes);
            emptyStringDialog.setMessage(R.string.favourites_no_folder);
            emptyStringDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Create new folder
                createFolder();
            });
            emptyStringDialog.setNegativeButton(R.string.link_cancel, ((dialogInterface, i) -> {
                // Cancelled, do nothing
            }));
            emptyStringDialog.show();
            return true;
        } else {
            return false;
        }
    }

    public static String[] getFolders(final Context thisContext, final Boolean isTailing) {
        try {
            String[] splitedString = PrefsHelper.getStringPrefs("userFavourites", thisContext).split("││");
            String[] toReturn = new String[splitedString.length - 1];
            for (int i = 1; i < splitedString.length; i++) {
                if (splitedString[i].isEmpty()) {
                    Log.e("getFolders", "Invalid non-trailing empty string");
                    throw new IllegalStateException();
                }
                String[] tempSplit = splitedString[i].split("│");
                toReturn[i - 1] = tempSplit[0].substring(1, tempSplit[0].length() - 1) + (isTailing ? (" (" + (tempSplit.length - 1) + ")") : "");
            }
            return toReturn;
        } catch (Exception e) {
            ExceptionHelper.handleException(thisContext, e, "getFolders", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", thisContext));
            return new String[0];
        }
    }

    public static boolean validateFolderName(final String inputtedName, final String[] currentStrings, final Context thisContext) {
        if (inputtedName.isEmpty()) {
            Log.w("validateFolderName", "Empty input.");
            Toast.makeText(thisContext, R.string.favourites_error_empty, Toast.LENGTH_LONG).show();
            return false;
        } else if (inputtedName.contains("│")) {
            Log.w("validateFolderName", "Illegal Character Detected.");
            Toast.makeText(thisContext, R.string.favourites_error_illegal, Toast.LENGTH_LONG).show();
            return false;
        } else if (inputtedName.length() > 30 || inputtedName.contains("\n")) {
            Log.w("validateFolderName", "Input is too long.");
            Toast.makeText(thisContext, R.string.favourites_error_length, Toast.LENGTH_LONG).show();
            return false;
        } else {
            // Check if specified
            for (String toCheck : currentStrings) {
                if (toCheck.equals(inputtedName)) {
                    Log.w("validateFolderName", "Conflict - Specified.");
                    Toast.makeText(thisContext, R.string.favourites_error_conflict, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isFavourite(final String machineName, final Context thisContext) {
        try {
            if (machineName == null) {
                throw new IllegalArgumentException();
            }
            return PrefsHelper.getStringPrefs("userFavourites", thisContext).contains("[" + machineName + "]");
        } catch (Exception e) {
            ExceptionHelper.handleException(thisContext, e, "isFavourite", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", thisContext));
            return false;
        }
    }

    private void createFolder() {
        // Check for folder count
        final String[] currentStrings = getFolders(this, false);
        if (currentStrings.length >= 10) {
            final AlertDialog.Builder folderLimitDialog = new AlertDialog.Builder(this);
            folderLimitDialog.setTitle(R.string.submenu_favourite_add);
            folderLimitDialog.setMessage(R.string.favourites_error_limit);
            folderLimitDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed
            });
            folderLimitDialog.show();
        } else {
            final View newFolderChunk = getLayoutInflater().inflate(R.layout.chunk_favourites_new, null);
            final EditText folderName = newFolderChunk.findViewById(R.id.folderName);
            final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(this);
            newFolderDialog.setTitle(R.string.submenu_favourite_add);
            newFolderDialog.setMessage(R.string.favourites_new_folder);
            newFolderDialog.setView(newFolderChunk);
            newFolderDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // To be overwritten...
            });
            newFolderDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                // Do nothing
            });

            final AlertDialog newFolderDialogCreated = newFolderDialog.create();
            newFolderDialogCreated.show();
            // Overwrite the positive button
            newFolderDialogCreated.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                try {
                    final String inputtedName = folderName.getText().toString().trim();
                    // Check if the input is legal
                    if (validateFolderName(inputtedName, currentStrings, this)) {
                        // Finally create the new folder.
                        PrefsHelper.editPrefs("userFavourites", "││{"
                                + inputtedName + "}" + PrefsHelper.getStringPrefs("userFavourites", this), this);
                        newFolderDialogCreated.dismiss();
                        initFavourites();
                    }
                } catch (Exception e) {
                    ExceptionHelper.handleException(FavouriteActivity.this, e, "newFolderDialog", "Illegal Favourites String. Please reset the application. String is: "
                            + PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));
                }
            });
        }
    }

    private void deleteFolder() {
        try {
            // Check if totally empty.
            if (!isEmptyString(R.string.submenu_favourite_delete)) {
                final View selectChunk = this.getLayoutInflater().inflate(R.layout.chunk_favourites_select, null);
                final LinearLayout selectLayout = selectChunk.findViewById(R.id.selectLayout);
                final String[] currentStrings = getFolders(this, true);
                final int[] currentSelections = new int[currentStrings.length];
                for (int i = 0; i < currentStrings.length; i++) {
                    CheckBox thisCheckBox = new CheckBox(this);
                    thisCheckBox.setText(currentStrings[i]);
                    thisCheckBox.setChecked(false);
                    int finalI = i;
                    thisCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
                        currentSelections[finalI] = thisCheckBox.isChecked() ? 1 : 0;
                    });
                    selectLayout.addView(thisCheckBox);
                }

                // Create the dialog.
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
                deleteDialog.setTitle(R.string.submenu_favourite_delete);
                deleteDialog.setMessage(R.string.favourites_delete);
                deleteDialog.setView(selectChunk);
                deleteDialog.setPositiveButton(R.string.link_confirm, (dialog, which) -> {
                    try {
                        // Delete the folders.
                        String[] splitedString = PrefsHelper.getStringPrefs("userFavourites", this).split("││");
                        String newString = "";
                        for (int j = 1; j < splitedString.length; j++) {
                            if (currentSelections[j - 1] == 0) {
                                newString = newString.concat("││" + splitedString[j]);
                            }
                        }
                        PrefsHelper.editPrefs("userFavourites", newString, this);
                        initFavourites();
                    } catch (Exception e) {
                        ExceptionHelper.handleException(this, e, "deleteFolderConfirm", "Illegal Favourites String. Please reset the application. String is: "
                                + PrefsHelper.getStringPrefs("userFavourites", this));
                    }
                });
                deleteDialog.setNegativeButton(R.string.link_cancel, ((dialog, which) -> {
                    // Cancelled, do nothing
                }));
                deleteDialog.show();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "deleteFolder", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", this));
        }
    }

    private void renameFolder() {
        try {
            // Check if totally empty.
            if (!isEmptyString(R.string.submenu_favourite_rename)) {
                final AlertDialog.Builder renameDialog = new AlertDialog.Builder(this);
                renameDialog.setTitle(R.string.submenu_favourite_rename);
                renameDialog.setMessage(R.string.favourites_rename);
                // Setup each option in dialog.
                final View folderChunk = getLayoutInflater().inflate(R.layout.chunk_favourites_list, null);
                final RadioGroup folderOptions = folderChunk.findViewById(R.id.option);
                final String[] allFolders = getFolders(this, false);
                for (int i = 0; i < allFolders.length; i++) {
                    final RadioButton folderOption = new RadioButton(this);
                    folderOption.setText(allFolders[i]);
                    folderOption.setId(i);
                    if (i == 0) {
                        folderOption.setChecked(true);
                    }
                    folderOptions.addView(folderOption);
                }
                renameDialog.setView(folderChunk);

                // When user tapped confirm or cancel...
                renameDialog.setPositiveButton(MainActivity.getRes().getString(R.string.link_confirm),
                        (dialog, which) -> {
                            try {
                                // Adapt New Folder Dialog
                                final View newFolderChunk = getLayoutInflater().inflate(R.layout.chunk_favourites_new, null);
                                final EditText folderName = newFolderChunk.findViewById(R.id.folderName);
                                folderName.setText(allFolders[folderOptions.getCheckedRadioButtonId()]);
                                final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(this);
                                newFolderDialog.setTitle(R.string.submenu_favourite_rename);
                                newFolderDialog.setMessage(R.string.favourites_new_folder);
                                newFolderDialog.setView(newFolderChunk);
                                newFolderDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                                    // To be overwritten...
                                });
                                newFolderDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                                    // Do nothing
                                });

                                final AlertDialog newFolderDialogCreated = newFolderDialog.create();
                                newFolderDialogCreated.show();
                                // Overwrite the positive button
                                newFolderDialogCreated.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                                    try {
                                        final String inputtedName = folderName.getText().toString().trim();
                                        // Check if the input is legal
                                        if (validateFolderName(inputtedName, allFolders, this)) {
                                            // Rename the folder.
                                            PrefsHelper.editPrefs("userFavourites",
                                                    PrefsHelper.getStringPrefs("userFavourites", this)
                                                            .replace("{" + allFolders[folderOptions.getCheckedRadioButtonId()] + "}", "{" + inputtedName + "}"), this);
                                            initFavourites();
                                            newFolderDialogCreated.dismiss();
                                        }
                                    } catch (Exception e) {
                                        ExceptionHelper.handleException(FavouriteActivity.this, e, "newFolderDialog_Rename", "Illegal Favourites String. Please reset the application. String is: "
                                                + PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));
                                    }
                                });
                            } catch (Exception e) {
                                ExceptionHelper.handleException(this, e, null, null);
                            }
                        });
                renameDialog.setNegativeButton(MainActivity.getRes().getString(R.string.link_cancel),
                        (dialog, which) -> {
                            // Cancelled.
                        });
                renameDialog.show();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "renameFolder", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", this));
        }
    }
}
