package com.example.school.organizer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;

import android.location.Address;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        listAdapter = new SelectArralAdapter(this, addressRows);

        listLocations.setAdapter(listAdapter);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
    }

    public void onSearch(View view) {

        String location = edLocation.getText().toString();

        listLocations.setVisibility(View.VISIBLE);

        if(location != null || !location.isEmpty()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            if( addressList.size() != 0 ) {
                leaveOnlyCheckedAddresses(addressList, addressRows);
            }

            try {
                addressList.addAll(geocoder.getFromLocationName(location, 10));

                convertArrayAddressToArrayString(addressList, addressRows);

                //Update
                listAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                LatLng latLngSearch = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngSearch));
            } else {
                Toast.makeText(MapsActivity.this, "No results found for " + location, Toast.LENGTH_SHORT).show();
            }
        }
    }



    public static class SelectArralAdapter extends ArrayAdapter<AddressRow> {
        private LayoutInflater inflater;

        public SelectArralAdapter(Context context, List<AddressRow> list) {
            super(context, R.layout.simple_row_location_search, R.id.rowTextView, list);
            // Cache the LayoutInflate to avoid asking for a new one each time.
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // AddressRow to display
            AddressRow addressRow = this.getItem(position);

            // The child views in each row.
            CheckBox checkBox;
            TextView textView;

            // Create a new row view
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.simple_row_location_search, null);

                // Find the child views.
                textView = (TextView) convertView.findViewById(R.id.rowTextView);
                checkBox = (CheckBox) convertView.findViewById(R.id.CheckBox01);
                // Optimization: Tag the row with it's child views, so we don't
                // have to
                // call findViewById() later when we reuse the row.
                convertView.setTag(new SelectViewHolder(textView, checkBox));
                // If CheckBox is toggled, update the planet it is tagged with.
                checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        AddressRow addressRow = (AddressRow) cb.getTag();
                        addressRow.setChecked(cb.isChecked());
                    }
                });
            }
            // Reuse existing row view
            else {
                // Because we use a ViewHolder, we avoid having to call
                // findViewById().
                SelectViewHolder viewHolder = (SelectViewHolder) convertView.getTag();
                checkBox = viewHolder.getCheckBox();
                textView = viewHolder.getTextView();
            }

            // Tag the CheckBox with the addressRow it is displaying, so that we can
            // access the addressRow in onClick() when the CheckBox is toggled.
            checkBox.setTag(addressRow);
            // Display addressRow data
            checkBox.setChecked(addressRow.isChecked());

            textView.setText(addressRow.getTitle());
            return convertView;
        }
    }



    @Override
    protected void onStop() {
        super.onStop();

        leaveOnlyCheckedAddresses(addressList, addressRows);
        tinyDB = new TinyDB(this);
        boolean isSuccessfulSearch = !addressList.isEmpty();

        if(isSuccessfulSearch) {
            tinyDB.putListAddress("checkedAdr", addressList);

        }
        tinyDB.putBoolean("isSuccessfulSearch", isSuccessfulSearch);
    }

    @Override
    protected void onPause() {
        super.onPause();

        leaveOnlyCheckedAddresses(addressList, addressRows);

        tinyDB = new TinyDB(this);

        tinyDB.putListAddress("checkedAdr", addressList);

        ArrayList<SearchSession> ss = new ArrayList<SearchSession>();

        //TODO: To remove it
        if (!edNote.getText().toString().isEmpty())
        ss.add(new SearchSession(addressList, edNote.getText().toString(), true));

        tinyDB.putListSearchSession("searchedSession", ss);
    }

    public void save(View v){
        this.finish();
    }
}
