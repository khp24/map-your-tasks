package com.example.map_your_tasks.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.map_your_tasks.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import Model.MapTaskItem;

public class MapFragment extends Fragment {

    // Map of tasks to their respective markers on the map
    private Map<MapTaskItem, Marker> markersByTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        markersByTask = new HashMap<>();

        // Initialize view
        final View view = inflater.inflate(R.layout.map_fragment, container, false);

        return view;
    }

    public void addUserLocation(final Activity activity) {

        final FusedLocationProviderClient locationClient =
                LocationServices.getFusedLocationProviderClient(activity);

        // Check for permission to access location
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {
            // Permission is already granted, proceed with app logic
        }

        // This object is created from the parent activity and we can use it to load the user's location
        locationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            // Create the icon for the user
                            final Bitmap userLocation = convertDrawableToBitmap(R.drawable.user_location);
                            final BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(userLocation);

                            // Add it to the map
                            final LatLng latLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            operateOnMap(map -> {
                                map.addMarker(new MarkerOptions().position(latLng)
                                        .icon(descriptor).title("You!"));
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,8));
                            });
                        }
                    }
                });
    }

    /**
     * @return the drawable at the provided ID converted to a Bitmap
     */
    private Bitmap convertDrawableToBitmap(final int drawableId) {
        final Drawable drawable = AppCompatResources.getDrawable(this.getContext(), drawableId);
        // Create a bitmap of the correct size
        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        // Create a canvas and draw the drawable on it
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void makeTaskVisible(final MapTaskItem task) {
        final List<MapTaskItem> tasks = new ArrayList<>();
        tasks.add(task);
        makeAllTasksVisible(tasks);
    }

    public void makeTaskInvisible(final MapTaskItem task) {
        // We must have created the marker before to get to this state
        markersByTask.get(task).setVisible(false);
    }

    public void makeAllTasksVisible(final List<MapTaskItem> tasks) {
        final Consumer<GoogleMap> operation = googleMap -> {
            for (final MapTaskItem task : tasks) {
                // If this marker has already been created, set it to be visible
                if (markersByTask.containsKey(task)) {
                    markersByTask.get(task).setVisible(true);
                }
                // If this marker hasn't been created yet, create it (will be visible by default)
                else {
                    final Marker marker = googleMap.addMarker(
                            new MarkerOptions().position(task.getLocation()).title(task.getTitle()));
                    markersByTask.put(task, marker);
                }
            }
        };
        operateOnMap(operation);
    }

    public void makeAllTasksInvisible() {
        final Consumer<GoogleMap> operation = googleMap -> {
            for (final Map.Entry<MapTaskItem, Marker> taskEntry : markersByTask.entrySet()) {
                // Iterate over every marker and set it to be invisible
                taskEntry.getValue().setVisible(false);
            }
        };
    }

    /**
     * We can't guarantee the the map is loaded when accessing this fragment.
     * Use this method to operate on the map as it will add a callback for when
     * the map is really ready
     *
     * Example:
     * String title = "markerTitle";
     * fragment.operateOnMap(googleMap -> {
     *     googleMap.addMarker(new MarkerOptions().title(title)));
     * };
     *
     * @param operation the operation on the map to perform.
     */
    private void operateOnMap(final Consumer<GoogleMap> operation) {
        // Initialize map fragment
        final SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Save the map when it is loaded
                operation.accept(googleMap);
            }
        });
    }
}
