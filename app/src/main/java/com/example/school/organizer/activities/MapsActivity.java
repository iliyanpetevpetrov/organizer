package com.example.school.organizer.activities;

import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.location.Address;
import android.widget.ListView;
import android.widget.Toast;

import com.example.school.organizer.R;
import com.example.school.organizer.layouts.SelectLocationsArrayAdapter;
import com.example.school.organizer.models.SearchSession;
import com.example.school.organizer.models.SelectViewHolder;
import com.example.school.organizer.settings.TinyDB;
import com.example.school.organizer.models.AddressRow;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.example.school.organizer.Utils.Utils.convertArrayAddressToArrayString;
import static com.example.school.organizer.Utils.Utils.leaveOnlyCheckedAddresses;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ArrayAdapter<AddressRow> listAdapter;

    //ListView with locations
    ListView listLocations;

    //List with addressRows that were searched
    ArrayList<Address> addressList = new ArrayList<>();

    // List with addressRow for the custom listView + checkbox
    ArrayList<AddressRow> addressRows = new ArrayList<>();

    EditText edLocation;

    HashMap<LatLng,Marker> markers = new HashMap<>();

    TinyDB tinyDB;

    EditText edNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        edLocation = (EditText) findViewById(R.id.tbAddress);

        edNote = (EditText) findViewById(R.id.edNote);

        listLocations = (ListView) findViewById(R.id.lwLocations);

        listLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                double latitude = 0;
                double longitude = 0;
                LatLng currentLatLng = null;

                AddressRow addressRow = listAdapter.getItem(position);
                addressRow.toggleChecked();

                SelectViewHolder viewHolder = (SelectViewHolder) view.getTag();

                viewHolder.getCheckBox().setChecked(addressRow.isChecked());

                if (addressList != null) {
                    latitude = addressList.get(position).getLatitude();
                    longitude = addressList.get(position).getLongitude();
                    currentLatLng = new LatLng(latitude, longitude);

                    if (addressRow.isChecked()) {
                        markers.put(currentLatLng,
                                mMap.addMarker(
                                        new MarkerOptions()
                                        .position(currentLatLng)
                                        .title(latitude + " " + longitude)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    } else {
                        if (markers.containsKey(currentLatLng)) {
                            markers.get(currentLatLng).remove();
                            markers.remove(currentLatLng);
                        }
                    }
                }
            }

        });
        listAdapter = new SelectLocationsArrayAdapter(this, addressRows);
        listLocations.setAdapter(listAdapter);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng latLng = new LatLng(42.658183, 23.357363);
        float zoom = 15;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void onSearch(View view) {

        String location = edLocation.getText().toString();

        listLocations.setVisibility(View.VISIBLE);

        if(location != null && !location.isEmpty()) {
            Geocoder geocoder = new Geocoder(this, new Locale("BG", "bg"));

            if( addressList.size() != 0 ) {
                leaveOnlyCheckedAddresses(addressList, addressRows);
            }

            try {
                addressList.addAll(
                        geocoder.getFromLocationName(location,
                                20, 42.631786, 23.213425,
                                42.727697, 23.448257));
                convertArrayAddressToArrayString(addressList, addressRows);

                //Update
                listAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                LatLng latLngSearch = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngSearch, 15));
            } else {
                Toast.makeText(MapsActivity.this, "No results found for " + location, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        tinyDB = new TinyDB(this);
        ArrayList<SearchSession> searchedSession = new ArrayList<SearchSession>();

        leaveOnlyCheckedAddresses(addressList, addressRows);

        // The note filed is required and if it is empty the appointment won't be saved
        if (!edNote.getText().toString().isEmpty() && !addressList.isEmpty()) {
            searchedSession.add(new SearchSession(addressList, edNote.getText().toString(), true));
            tinyDB.putListSearchSession("searchedSession", searchedSession);
        } else {
            Toast.makeText(MapsActivity.this, "Appointment not saved because empty note or " +
                    "not selected addresses.",
                    Toast.LENGTH_LONG).show();
        }

        tinyDB.putBoolean("isSuccessfulSearch", !searchedSession.isEmpty());
    }

    public void save(View v){
        this.finish();
    }
}
