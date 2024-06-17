package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentContainerView fragmentContainerView;
    private Fragment cameraFragment;
    private Fragment twoDViewFragment;
    private Fragment activeFragment;
    // Define the permission request code
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.drawer_layout);

        // Check if the camera permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // You can use the camera here
            openCamera();
        } else {
            // Request the camera permission
            requestCameraPermission();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fragmentContainerView = findViewById(R.id.frag_container);

        cameraFragment = new CameraViewFragment();
        twoDViewFragment = new TwoDViewFragment();

        // Set default fragment
        activeFragment = cameraFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, activeFragment).commit();

        NavigationView navigationView = findViewById(R.id.navview);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id==R.id.voice_nav) {
                handleVoiceNavClick(item);
                // Perform action for turning off voice navigation
            }else if (id==R.id.twod_view){
                    // Perform action for opening 2D map
                handle2dViewClick(item);
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
    }

    // Method to open the camera (example method)
    private void openCamera() {
        // Implement your logic to open the camera here
        Toast.makeText(this, "Camera permission granted. Now you can use the camera.", Toast.LENGTH_SHORT).show();
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

    private void handle2dViewClick(MenuItem item){
        if (item.getTitle().equals(getString(R.string.menu_open_2d_map))) {
            // Switch to 2D view
            switchFragment(twoDViewFragment);
            Toast.makeText(MainActivity.this, "Opening 2D map", Toast.LENGTH_SHORT).show();
            item.setTitle(getString(R.string.menu_close_2d_map));
//            inflate TwoDViewFragment
        } else {
            // Switch to camera view
            switchFragment(cameraFragment);
            Toast.makeText(MainActivity.this, "Closing 2D map", Toast.LENGTH_SHORT).show();
            item.setTitle(getString(R.string.menu_open_2d_map));
//            inflate CameraViewFragment

        }
    }

    private void switchFragment(Fragment fragment) {
        if (fragment != activeFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frag_container, fragment);
            fragmentTransaction.commit();
            activeFragment = fragment;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // Handle search action
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}