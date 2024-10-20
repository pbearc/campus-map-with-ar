package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static final String BASE_URL = "http://10.192.57.160:5002";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_PERMISSIONS = 1;
    private DestinationAdapter adapter;
    private List<PointOfInterest> allDestinations;
    private BLEScanner bleScanner;
    private Fragment cameraFragment;
    private PointOfInterest currentDestination;
    private RecyclerView destinationView;
    private List<PointOfInterest> filteredDestinations;
    private FragmentManager fragmentManager;
    private NavApi navApiInterface;
    private OrientationSensor orientationSensor;
    private Retrofit retrofit;
    private Fragment twoDViewFragment;
    private String itemText; // Class variable to hold the passed itemText
    private boolean shouldSubmitQuery = false; // Flag to indicate whether to auto-submit the search query

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView bottomSheetDirection;
    private TextView bottomSheetTimeAndDistLeft;
    private TextView bottomSheetArrivalTime;
    private TextView bottomSheetDestination;


    private Button bottomSheetButton;
    public PointOfInterest getCurrentDestination() {
        return this.currentDestination;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.drawer_layout);

        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0) {
            openCamera();
        } else {
            requestCameraPermission();
        }
        Direction.setContext(this);

        checkPermissions();
        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.navApiInterface = this.retrofit.create(NavApi.class);
        this.cameraFragment = new CameraViewFragment();
        this.twoDViewFragment = new TwoDViewFragment();
        this.fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frag_container, this.cameraFragment);
        fragmentTransaction.add(R.id.frag_container, this.twoDViewFragment);
        fragmentTransaction.show(this.cameraFragment);
        fragmentTransaction.hide(this.twoDViewFragment);
        fragmentTransaction.commit();
        this.orientationSensor = new OrientationSensor(this, (TwoDViewFragment) this.twoDViewFragment, this.navApiInterface);
        this.bleScanner = new BLEScanner(this, this, this.navApiInterface, (TwoDViewFragment) this.twoDViewFragment, orientationSensor);
        this.orientationSensor.setBleScanner(this.bleScanner);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        toggle.syncState();
        this.destinationView = findViewById(R.id.recyclerView);
        this.destinationView.setLayoutManager(new LinearLayoutManager(this));
        this.allDestinations = new ArrayList();
        this.navApiInterface.getPOIs().enqueue(new Callback<List<PointOfInterest>>() {
            public void onResponse(Call<List<PointOfInterest>> call, Response<List<PointOfInterest>> response) {
                MainActivity.this.allDestinations.addAll(response.body());
            }

            public void onFailure(Call<List<PointOfInterest>> call, Throwable throwable) {
                Log.e("POI", throwable.getMessage());
            }
        });
        this.filteredDestinations = new ArrayList(this.allDestinations);
        this.adapter = new DestinationAdapter(this.filteredDestinations, this::onDestinationSelected);
        this.destinationView.setAdapter(this.adapter);
        NavigationView navigationView = findViewById(R.id.navview);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id==R.id.voice_nav) {
                handleVoiceNavClick(item);
                // Perform action for turning off voice navigation
            }else if (id==R.id.twod_view){
                // Perform action for opening 2D map
                handle2dViewClick(item);
            }else if (id==R.id.change_language){
                changeLanguage();
            }
            // Close the drawer after handling item click
            drawer.closeDrawers();
            return true;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if there is any data passed from ProfileActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("item_text")) {
            itemText = extras.getString("item_text"); // Extract the text
            shouldSubmitQuery = true; // Set the flag to true since we have received the text

            // Use Handler to delay the search view expansion to ensure the menu is ready
            new android.os.Handler().post(() -> {
                expandSearchViewWithQuery();
            });
        }

        // Initialize the bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN); // Start hidden


        // Find views within the bottom sheet
        bottomSheetDirection = bottomSheet.findViewById(R.id.bottom_sheet_direction);
        bottomSheetTimeAndDistLeft = bottomSheet.findViewById(R.id.bottom_sheet_time_dist);
        bottomSheetArrivalTime = bottomSheet.findViewById(R.id.bottom_sheet_arrivaltime);
        bottomSheetDestination = bottomSheet.findViewById(R.id.bottom_sheet_destination);
        bottomSheetButton = bottomSheet.findViewById(R.id.bottom_sheet_button);

        // Set up button click listener
        bottomSheetButton.setOnClickListener(v -> {
            currentDestination = null;
            ((TwoDViewFragment) twoDViewFragment).removeDestination();
            ((TwoDViewFragment) twoDViewFragment).removeRoute();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
    }

    private void expandSearchViewWithQuery() {
        // Use findViewById to get the Toolbar and find the search menu item
        Toolbar toolbar = findViewById(R.id.toolbar);
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();

            // Expand the search view and set the query
            searchItem.expandActionView();
            searchView.setQuery(itemText, true); // Set the text and submit the query
            searchView.clearFocus(); // Clear focus to avoid bringing up the keyboard
            shouldSubmitQuery = false; // Reset the flag
        }
    }



    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d("hey", "BLUETOOTH_SCAN");
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("hey", "BLUETOOTH_CONNECT");
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Log.d("hey", "BLUETOOTH_ADMIN");

                permissionsNeeded.add(Manifest.permission.BLUETOOTH);
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("hey", "ACCESS_FINE_LOCATION");

            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    // Method to request camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                openCamera();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }

        else if (requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "Permission granted: " + permissions[i]);
                } else {
                    Log.d("Permissions", "Permission denied: " + permissions[i]);
                }
            }
        }
    }

    // Method to open the camera (example method)
    private void openCamera() {
        // Implement your logic to open the camera here
        Toast.makeText(this, "Camera permission granted. Now you can use the camera.", Toast.LENGTH_SHORT).show();
    }

    private void changeLanguage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Language")
                .setItems(new String[]{"English", "French", "Indonesian", "Chinese", "Malay"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // English
                            setAppLanguage("en");
                            break;
                        case 1: // French
                            setAppLanguage("fr");
                            break;
                        case 2: // Indonesian
                            setAppLanguage("id");
                            break;
                        case 3: // Chinese
                            setAppLanguage("zh");
                            break;
                        case 4: // Malay
                            setAppLanguage("ms");
                            break;
                    }
                });
        builder.create().show();
        Direction.setContext(this);
    }

    private void setAppLanguage(String lang) {
        // Save the selected language in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language", lang);
        editor.apply();

        // Update the locale and restart the activity
        LocaleHelper.setLocale(this, lang);
        recreate(); // Restart the activity to apply the new language
    }

    private void loadLanguage() {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String language = preferences.getString("language", "en"); // Default to English
        LocaleHelper.setLocale(this, language);
    }

    private void handleVoiceNavClick(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.menu_turn_off_voice))) {
            // Perform action for turning off voice navigation
            Toast.makeText(MainActivity.this, "Turning off voice navigation", Toast.LENGTH_SHORT).show();
            item.setTitle(getString(R.string.menu_turn_on_voice));
        } else {
            // Perform action for turning on voice navigation
            Toast.makeText(MainActivity.this, "Turning on voice navigation", Toast.LENGTH_SHORT).show();
            item.setTitle(getString(R.string.menu_turn_off_voice));
        }
    }

    private void handle2dViewClick(MenuItem item) {
        FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();
        if (item.getTitle().equals(getString(R.string.menu_open_2d_map))) {
            if (this.cameraFragment.isVisible()) {
                fragmentTransaction.hide(this.cameraFragment);
            }
            if (this.twoDViewFragment.isHidden()) {
                fragmentTransaction.show(this.twoDViewFragment);
            }
            Toast.makeText(this, "Opening 2D map", Toast.LENGTH_SHORT).show();
            item.setTitle(getString(R.string.menu_close_2d_map));
        } else {
            if (this.twoDViewFragment.isVisible()) {
                fragmentTransaction.hide(this.twoDViewFragment);
            }
            if (this.cameraFragment.isHidden()) {
                fragmentTransaction.show(this.cameraFragment);
            }
            Toast.makeText(this, "Closing 2D map", Toast.LENGTH_SHORT).show();
            item.setTitle(getString(R.string.menu_open_2d_map));
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Handle SearchView setup
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

            // Customize the SearchView's appearance and behavior
            searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.typehint));
            searchEditText.setTextColor(ContextCompat.getColor(this, R.color.white));
            searchView.setQueryHint("Enter desired destination");

            // Set close button's color
            Drawable closeDrawable = ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn)).getDrawable();
            if (closeDrawable != null) {
                closeDrawable.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
            }

            // Set listener for text changes
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filter(query); // Filter using the submitted query
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filter(newText); // Filter as text changes
                    return false;
                }
            });

            // Handle action expand/collapse
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    destinationView.setVisibility(View.VISIBLE);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    destinationView.setVisibility(View.GONE);
                    // Refresh the options menu to ensure all menu items are visible
                    invalidateOptionsMenu();
                    return true;
                }
            });

            // Automatically expand the SearchView if shouldSubmitQuery is true
            if (itemText != null && shouldSubmitQuery) {
                searchItem.expandActionView(); // Expand the SearchView
                searchView.setQuery(itemText, true); // Set the text and submit the query
                shouldSubmitQuery = false; // Reset the flag to avoid repeated submissions
                searchView.clearFocus(); // Clear focus to avoid bringing up the keyboard
            }
        }

        return true;
    }


    private void filter(String text) {
        this.filteredDestinations.clear();
        if (text.isEmpty()) {
            this.filteredDestinations.addAll(this.allDestinations);
        } else {
            for (PointOfInterest destination : this.allDestinations) {
                if (destination.getName().toLowerCase().contains(text.toLowerCase())) {
                    this.filteredDestinations.add(destination);
                }
            }
        }
        this.adapter.filterList(this.filteredDestinations);
    }

    public void updateBottomSheetInfo(String direction, String timeAndDistance, String arrivalTime) {
        bottomSheetDirection.setText(direction);
        bottomSheetTimeAndDistLeft.setText(timeAndDistance);
        bottomSheetArrivalTime.setText(arrivalTime);

        // Show the bottom sheet if it is hidden
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void updateBottomSheetInfo(String direction) {
        bottomSheetDirection.setText(direction);

        // Show the bottom sheet if it is hidden
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void onDestinationSelected(PointOfInterest destination) {
//        Toast.makeText(this, "Selected: " + destination.getName(), Toast.LENGTH_SHORT).show();
        destinationView.setVisibility(View.GONE);
        MenuItem searchItem = ((Toolbar) findViewById(R.id.toolbar)).getMenu().findItem(R.id.action_search);
        if (searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchItem.getActionView().getWindowToken(), 0);
            }
        }
        currentDestination = destination;
        ((TwoDViewFragment) twoDViewFragment).addDestination(destination);

//        // Update and show the bottom sheet
//        bottomSheetDirection.setText("Additional bottomSheetDirection"); // Update as needed
//        bottomSheetTimeAndDistLeft.setText("Additional bottomSheetTimeAndDistLeft"); // Update as needed
//        bottomSheetArrivalTime.setText("Additional bottomSheetArrivalTime"); // Update as needed

        bottomSheetDestination.setText(destination.getName());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

//    private void onDestinationSelected(PointOfInterest destination) {
//        Toast.makeText(this, "Selected: " + destination.getName(), Toast.LENGTH_SHORT).show();
//        this.destinationView.setVisibility(View.GONE);
//        MenuItem searchItem = ((Toolbar) findViewById(R.id.toolbar)).getMenu().findItem(R.id.action_search);
//        if (searchItem.isActionViewExpanded()) {
//            searchItem.collapseActionView();
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm != null) {
//                imm.hideSoftInputFromWindow(searchItem.getActionView().getWindowToken(), 0);
//            }
//        }
//        this.currentDestination = destination;
//        ((TwoDViewFragment) this.twoDViewFragment).addDestination(destination);
//        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), destination.getName(), Snackbar.LENGTH_INDEFINITE);
//        snackbar.setAction("STOP", v -> {
//            this.currentDestination = null;
//            ((TwoDViewFragment) this.twoDViewFragment).removeDestination();
//            ((TwoDViewFragment) this.twoDViewFragment).removeRoute();
//            snackbar.dismiss();
//        });
//        (snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action)).setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
//        ColorStateList whiteTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white));
//        int green = ContextCompat.getColor(this, R.color.colorPrimary);
//        ColorStateList valueOf = ColorStateList.valueOf(green);
//        snackbar.setTextColor(whiteTextColor);
//        snackbar.setActionTextColor(whiteTextColor);
//        snackbar.setBackgroundTint(green);
//        snackbar.show();
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();

        // If action_search is clicked
        if (id == R.id.action_search) {
            // SearchView setup is handled in onCreateOptionsMenu()
            return true;
        }

        // If action_profile is clicked
        if (id == R.id.action_profile) {
            // Open the ProfileActivity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        Log.d("state", "resume");
        super.onResume();
        this.bleScanner.startScan();
        if (this.orientationSensor != null) this.orientationSensor.startSensor();
    }

    protected void onPause() {
        Log.d("state", "pause");
        super.onPause();
        this.bleScanner.stopScan();
        if (this.orientationSensor != null) this.orientationSensor.stopSensor();
    }

    protected void onDestroy() {
        Log.d("state", "destroy");
        super.onDestroy();
        this.bleScanner.stopScan();
        if (this.orientationSensor != null) this.orientationSensor.stopSensor();
    }
}