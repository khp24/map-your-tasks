package com.example.map_your_tasks.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.map_your_tasks.Model.Task;
import com.example.map_your_tasks.R;
import com.example.map_your_tasks.fragments.AddListFragment;
import com.example.map_your_tasks.fragments.MapViewFragment;
import com.example.map_your_tasks.fragments.NotificationFragment;
import com.example.map_your_tasks.fragments.TaskListFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;

public class SecondActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    /** Step 1: create a reference to the DrawerLayout */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        /** Step 2: Get the DrawerLayout object from the layout XML file */
        mDrawerLayout = findViewById(R.id.nav_drawer_layout);

        /** Step 3: Get the NavigationView object from the layout SML file */
        mNavigationView = findViewById(R.id.nav_view);

        /** Step 4: Set the listener for the NvigationView. The Main Activity shuould
         * implement the interface NavigationView.OnNavigationItemSelectedListener */
        mNavigationView.setNavigationItemSelectedListener(this);

        /** Step 5: Set up the ActionBarDrawerToggle */
        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this, // Activity / Context
                mDrawerLayout, // DrawerLayout
                R.string.navigation_drawer_open, // String to open
                R.string.navigation_drawer_close // String to close
        );
        /** Step 6: Include the mActionBarDrawerToggle as the listener to the DrawerLayout.
         *  The synchState() method is used to synchronize the state of the navigation drawer */
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        /** Step 7:Set the default fragment to the MapFragment */
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new TaskListFragment()).commit();

        firebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(getApplicationContext(),  GoogleSignInOptions.DEFAULT_SIGN_IN);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {// Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_product_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TaskListFragment()).commit();
                break;
            case R.id.nav_add_product:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddListFragment()).commit();
                break;
            case R.id.nav_notification:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationFragment()).commit();
                break;
            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapViewFragment()).commit();
                break;
            case R.id.log_out:
                signOut();
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
                break;
        }

        /** Close the navigation drawer */
        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /** Inflate the menu; this adds items to the action bar if it is present. */
        //getMenuInflater().inflate(R.menu.nav_drawer_items, menu);
        return true;
    }


    public void signOut(){
        firebaseAuth.getInstance().signOut();
        googleSignInClient.signOut();
        Toast.makeText(SecondActivity.this, "User is successfully signed out", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void editTask(Task task) {
        AddListFragment addListFragment = new AddListFragment();
        Bundle args = new Bundle();
        args.putParcelable("task", task);
        addListFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, addListFragment).commit();
    }
}
