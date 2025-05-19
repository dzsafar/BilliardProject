package com.example.billiardproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;

public class foglalas_activity extends AppCompatActivity {
    private static final String LOG_TAG = foglalas_activity.class.getName();
    private static final String PREF_KEY = register_activity.class.getPackage().toString();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private SharedPreferences preferences;
    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemList;
    private ShopingItemAdapter mAdapter;
    private int gridNumber = 1;
    private boolean viewRow = true;

    private FirebaseFirestore mFireStore;
    private CollectionReference mItems;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private TextView locationTextView; // TextView a helyadatok megjelenítésére

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
             EdgeToEdge.enable(this);
        }
        setContentView(R.layout.activity_foglalas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Hitelesített felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem hitelesített felhasználó!");
            finish();
        }

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        mRecyclerView = findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();

        mAdapter = new ShopingItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        mFireStore = FirebaseFirestore.getInstance();
        mItems = mFireStore.collection("Tables");

        locationTextView = findViewById(R.id.locationTextView);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        checkLocationPermission();
        queryData();
    }

    private void queryData() {
        mItemList.clear();

        mItems.orderBy("asztal").limit(10).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                mItemList.add(item);
            }
            if (mItemList.size() == 0) {
                initializeData();
                queryData();
            }

            mAdapter.notifyDataSetChanged();

        });
    }

    private void initializeData() {
        String[] itemAsztal = getResources().getStringArray(R.array.foglalas_AsztalNev);
        String[] itemFoglalas = getResources().getStringArray(R.array.foglalas_FoglalasiIdopont);

        for (int i = 0; i < itemAsztal.length; i++) {
            mItems.add(new ShoppingItem(itemAsztal[i], itemFoglalas[i]));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.foglalas_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                Log.d(LOG_TAG, "LogOut");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.setting_button:
                Log.d(LOG_TAG, "Settings");
                Intent beallitasokIntent = new Intent(this, beallitasok.class);
                startActivity(beallitasokIntent);
                return true;
            case R.id.view_selector:
                Log.d(LOG_TAG, "View");

                if (viewRow) {
                    changeSpanCount(item, R.drawable.ic_view_grid, 1);
                } else {
                    changeSpanCount(item, R.drawable.ic_row_view, 2);
                }

                return true;
            case R.id.foglalasok:
                Log.d(LOG_TAG, "Foglalasok");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "A helymeghatározás nem engedélyezett!", Toast.LENGTH_SHORT).show();
                locationTextView.setText("Helymeghatározás nem engedélyezett!");
            }
        }
    }

    private void startLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Hiba a helymeghatározás indításakor: " + e.getMessage());
            Toast.makeText(this, "Hiba a helymeghatározás során!", Toast.LENGTH_SHORT).show();
            locationTextView.setText("Hiba a helymeghatározás során!");
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    Location lastLocation = locationResult.getLastLocation();
                    String locationText = "Szélesség: " + lastLocation.getLatitude() + "\n"
                            + "Hosszúság: " + lastLocation.getLongitude() + "\n"
                            + "Pontosság: " + lastLocation.getAccuracy() + " m";
                    locationTextView.setText(locationText);
                    Log.d(LOG_TAG, "Helyadatok: " + locationText);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
}

