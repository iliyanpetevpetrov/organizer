package com.example.school.organizer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static String DEFAULT_EMPTY_STRING_APPOINTMENT_LIST = "1. Click me.\n" +
            "2. Now try to delete me\nwith long click.";
    FloatingActionButton fTurnOnOff;
    TinyDB tinydb;
    ArrayList<SearchSession> appointmentsTodoList = new ArrayList<>();
    LocationManager locationManager = null;
    Bundle bndl = new Bundle();
    private ListView appointmentsListView;
    private SelectArralAdapter listAdapter;
    private boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tinydb = new TinyDB(this);
        appointmentsTodoList.clear();
        //Initialize arrayList for main list view
        appointmentsTodoList.addAll(tinydb.getListSearchSession("appointmentsTodoList", SearchSession.class));

        boolean isSuccessfulSearch = tinydb.getBoolean("isSuccessfulSearch");

        if (isSuccessfulSearch) {
            ArrayList<SearchSession> tmpSearchSession =
                    tinydb.getListSearchSession("mSearchedSession", SearchSession.class);
            appointmentsTodoList.addAll(tmpSearchSession);
//TODO: uncomment
//            tinydb.putListSearchSession("appointmentsTodoList", appointmentsTodoList);
            //Prevent adding the same searchSession when rotating the phone
            tinydb.putBoolean("isSuccessfulSearch", false);
        }


        if (appointmentsTodoList.size() == 0) {
            ArrayList<Address> tmp = new ArrayList<Address>();

            appointmentsTodoList.add(new SearchSession(
                    tmp, DEFAULT_EMPTY_STRING_APPOINTMENT_LIST, false));
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fTurnOnOff = (FloatingActionButton) findViewById(R.id.btnTurnOnOff);

        this.isOn = tinydb.getBoolean("isOn");
        changeBtnTurnOffOnColor(isOn);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mIntent);
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Snackbar.make(v, "Add new appointment", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
            }
        });

        //Get and initialize ListView
        appointmentsListView = (ListView) findViewById(R.id.mainListView);

        appointmentsListView.setVisibility(View.VISIBLE);

        appointmentsListView.setClickable(true);
        appointmentsListView.setLongClickable(true);

        appointmentsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.item_menu_title)
                        .setItems(R.array.item_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                /*
                                <item>Activate</item>
                                <item>Deactivate</item>
                                <item>Edit</item>
                                <item>Delete</item>
                                <item>Mute for 5 minutes</item>
                                <item>Unmute</item>
                                 */
                                switch (which) {
                                    case 0:
                                        appointmentsTodoList.get(position).setSearchOn(true);
                                        listAdapter.refresh(appointmentsTodoList);
                                        break;
                                    case 1:
                                        appointmentsTodoList.get(position).setSearchOn(false);
                                        listAdapter.refresh(appointmentsTodoList);
                                        break;
                                    case 2:
                                        onEditDialog(position).show();
                                        listAdapter.refresh(appointmentsTodoList);
                                        break;
                                    case 3:
                                        appointmentsTodoList.remove(position);
                                        listAdapter.refresh(appointmentsTodoList);
                                        break;
                                    case 4:
                                        appointmentsTodoList.get(position).mute();
                                        showMessageForMutedAppointment(position);
                                        listAdapter.refresh(appointmentsTodoList);
                                        break;
                                    case 5:
                                        appointmentsTodoList.get(position).unmute();
                                        Toast.makeText(MainActivity.this,
                                                "The appointment " + "\"" +
                                                        appointmentsTodoList.get(position).getNote()
                                                        + "\" is unmuted.",
                                                Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }
                        });

                builder.create().show();

                listAdapter.notifyDataSetChanged();
                listAdapter.refresh(appointmentsTodoList);

                return true;
            }
        });

        appointmentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SearchSession searchSession = listAdapter.getItem(position);
                searchSession.toggleChecked();
                SelectViewHolder viewHolder = (SelectViewHolder) view.getTag();

                viewHolder.getCheckBox().setChecked(searchSession.isSearchOn());
            }

        });

        listAdapter = new SelectArralAdapter(this, appointmentsTodoList);

        appointmentsListView.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {

                if (isOn) {
//                    for (final SearchSession adr : appointmentsTodoList) {
                    for (int counter = 0; counter < appointmentsTodoList.size(); counter++) {
                        final int innerCounter = counter;
                        final SearchSession currentAddress = appointmentsTodoList.get(counter);

                        if (currentAddress.isSearchOn() &&
                                !currentAddress.isMute() &&
                                currentAddress.isNear(location, 1000)) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            String message = String.format("You are %.0f meters away from \"%s\"" +
                                            " appointment",
                                    currentAddress.getDistanceBetweenLocation(location)[0],
                                    currentAddress.getNote());

                            builder.setPositiveButton("Drive me to", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Address nearestAdr = currentAddress.getNearestAddress(location);
                                    if (nearestAdr != null) {
                                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
                                                nearestAdr.getLatitude() + "," +
                                                nearestAdr.getLongitude());
                                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                        mapIntent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapIntent);
                                    }
                                }
                            });

                            builder.setNegativeButton("Mute for 5 minutes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    appointmentsTodoList.get(innerCounter).mute();
                                    showMessageForMutedAppointment(innerCounter);
                                }
                            });

                            builder.setNeutralButton("Mark as done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    appointmentsTodoList.get(innerCounter).toggleChecked();
                                    listAdapter.notifyDataSetChanged();
                                }
                            });

                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage(message).setTitle("Appointment near you is found.");

                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();

                            dialog.show();

                            break;
                        }
                    }
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        tinydb.putListSearchSession("appointmentsTodoList", appointmentsTodoList);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        123);
            }
            Toast.makeText(MainActivity.this, "Gps access is denied.", Toast.LENGTH_SHORT).show();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 20, 20, locationListener);
    }

    private void showMessageForMutedAppointment(int position) {
        SimpleDateFormat formatter =
                new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(appointmentsTodoList.get(position)
                .getEndMuteTime());
        Toast.makeText(MainActivity.this,
                "The appointment " + "\"" + appointmentsTodoList.get(position).getNote() + "\"" +
                        " is muted until " + formatter.format(calendar.getTime()),
                Toast.LENGTH_LONG).show();
    }

    public Dialog onEditDialog(final int position) {
        final ArrayList<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        boolean[] alreadyCheckedItems = appointmentsTodoList.get(position).getIsChecked();

        final boolean[] copiedCheckedItems = Arrays.copyOf(appointmentsTodoList.get(position).getIsChecked(),
                appointmentsTodoList.get(position).getIsChecked().length);

        ArrayList<Address> listAddresses = appointmentsTodoList.get(position).getSearchedAddresses();

        String strings[] = new String[appointmentsTodoList.get(position).getSearchedAddresses().size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new String();
        }

        for (int i = 0; i < listAddresses.size(); i++) {
            if (listAddresses.get(i).getMaxAddressLineIndex() > 1) {
                for (int j = 0; j < listAddresses.get(i).getMaxAddressLineIndex(); j++) {
                    strings[i] += listAddresses.get(i).getAddressLine(j) + "\n";
                }
            } else {
                strings[i] = listAddresses.get(i).getAddressLine(0);
            }
        }

        // Set the dialog title
        builder.setTitle("Edit")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(strings, alreadyCheckedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        if (appointmentsTodoList.get(position).countCheckAddresses() == 0) {
                            appointmentsTodoList.get(position).setSearchOn(false);
                        }
                        listAdapter.refresh(appointmentsTodoList);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        changeCheckedAddresses(copiedCheckedItems, position);
                        listAdapter.refresh(appointmentsTodoList);
                    }
                });
        return builder.create();
    }

    private void changeCheckedAddresses(boolean[] checkedNumbers, int position) {
        appointmentsTodoList.get(position).setIsChecked(checkedNumbers);
        listAdapter.refresh(appointmentsTodoList);
    }

    @Override
    protected void onResume() {
        super.onResume();

        appointmentsTodoList.clear();
        appointmentsTodoList.addAll(tinydb.getListSearchSession("appointmentsTodoList", SearchSession.class));

        boolean isSuccessfulSearch = tinydb.getBoolean("isSuccessfulSearch");

        if (isSuccessfulSearch) {
            ArrayList<SearchSession> tmpSearchSession =
                    tinydb.getListSearchSession("searchedSession", SearchSession.class);
            appointmentsTodoList.addAll(tmpSearchSession);

            //Prevent adding the same searchSession when rotating the phone
            tinydb.putBoolean("isSuccessfulSearch", false);
        }

        listAdapter.refresh(appointmentsTodoList);

    }

    @Override
    protected void onPause() {
        super.onPause();
        tinydb.putListSearchSession("appointmentsTodoList", appointmentsTodoList);
        tinydb.putBoolean("isOn", isOn);
    }

    public void makeRoute(View v) {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            123);
                }
                Toast.makeText(MainActivity.this, "Gps access is denied.", Toast.LENGTH_SHORT).show();
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location == null) {
                Toast.makeText(MainActivity.this, "Could not find you current location.", Toast
                        .LENGTH_LONG).show();
                return;
            }
            Address[] path = findShortestPathIncludingAllNodes(location);
            if (path.length >= 2) {
                String jsonURL = "https://maps.google.com/maps?";
                final StringBuffer sBuf = new StringBuffer(jsonURL);

                sBuf.append("saddr=");
                sBuf.append(path[0].getLatitude());
                sBuf.append(',');
                sBuf.append(path[0].getLongitude());
                sBuf.append("&daddr=");
                sBuf.append(path[path.length - 1].getLatitude());
                sBuf.append(',');
                sBuf.append(path[path.length - 1].getLongitude());

                if (path.length >= 3) {
                    for (int i = 3; i < path.length; i++) {
                        sBuf.append("+to:");
                        sBuf.append(path[i].getLatitude());
                        sBuf.append(',');
                        sBuf.append(path[i].getLongitude());
                    }
                }
                sBuf.append("&key=");
                sBuf.append("");

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sBuf.toString()));
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } else {
                Toast.makeText(MainActivity.this, "Please, enable one or more " +
                        "appointments.", Toast
                        .LENGTH_LONG).show();
            }
        }
    }

    public void turnOnOff(View v) {
        listAdapter.notifyDataSetChanged();
        isOn = !isOn;
        changeBtnTurnOffOnColor(isOn);

        Snackbar.make(v, "Turned " + (isOn ? "on" : "off") + '.', Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void changeBtnTurnOffOnColor(boolean isOn) {
        if (isOn) {
            fTurnOnOff.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#66B266")));
        } else {
            fTurnOnOff.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4C4C")));
        }
    }

    private Context getActivity() {
        return this;
    }

    public ArrayList<Address> getAllNearestActiveAddresses(Location location) {
        ArrayList<Address> allAddresses = new ArrayList<>();

        //Add first location to be current location
        Address firstAddress = new Address(new Locale("EN"));
        firstAddress.setLatitude(location.getLatitude());
        firstAddress.setLongitude(location.getLongitude());
        bndl.putString("0", "Current location");
        allAddresses.add(firstAddress);

        for (int i = 0; i < appointmentsTodoList.size(); i++) {
            Address tempAdr = appointmentsTodoList.get(i).getNearestAddress(location);
            bndl.putString(String.valueOf(i + 1), appointmentsTodoList.get(i).getNote());
            tempAdr.setExtras(bndl);
            if (tempAdr != null && appointmentsTodoList.get(i).isSearchOn()) {
                allAddresses.add(tempAdr);
            }
        }

        return allAddresses;
    }


    private Address[] findShortestPathIncludingAllNodes(Location location) {
        ArrayList<Address> allAddresses = getAllNearestActiveAddresses(location);
        ArrayList<Integer> freePositions = new ArrayList<>(allAddresses.size());
        Address[] shortestPath = new Address[allAddresses.size()];

        // Initialize path with starting node
        shortestPath[0] = allAddresses.get(0);

        for (int i = 0; i < allAddresses.size(); i++) {
            freePositions.add(i);
        }

        int positionCurrentNode = 0;
        int size = freePositions.size();
        for (int i = 0; i < size - 1; i++) {
            int nextPosition = getNearestAddress(positionCurrentNode, allAddresses, freePositions);
            freePositions.remove((Integer) positionCurrentNode);
            positionCurrentNode = nextPosition;
            shortestPath[i+1] = allAddresses.get(nextPosition);
        }

        return shortestPath;
    }

    private int getNearestAddress(int positionCurrentNode, ArrayList<Address> allNodes,
                                  ArrayList<Integer> freePositions) {
        ArrayList<Integer> copyOfFreePositions = new ArrayList<>(freePositions);
        if (copyOfFreePositions.contains((Integer) positionCurrentNode)) {
            copyOfFreePositions.remove((Integer) positionCurrentNode);
        }
        int positionOfNearestAddress = copyOfFreePositions.get(0);

        if (copyOfFreePositions.size() > 1) {
            Address next;
            Address currentPosition = allNodes.get(positionCurrentNode);
            Address nearestAddress = allNodes.get(copyOfFreePositions.get(0));
            float nearestDistance[] = new float[2];
            float currentDistance[] = new float[2];

            Location.distanceBetween(
                    currentPosition.getLatitude(), currentPosition.getLongitude(),
                    nearestAddress.getLatitude(), nearestAddress.getLongitude(),
                    nearestDistance);

            for (int i = 1; i < copyOfFreePositions.size(); i++) {

                next = allNodes.get(copyOfFreePositions.get(i));

                Location.distanceBetween(
                        currentPosition.getLatitude(), currentPosition.getLongitude(),
                        next.getLatitude(), next.getLongitude(), currentDistance);

                if (currentDistance[0] < nearestDistance[0]) {
                    positionOfNearestAddress = copyOfFreePositions.get(i);
                    nearestDistance[0] = currentDistance[0];
                }
            }
        }
        return positionOfNearestAddress;
    }

    public class SelectArralAdapter extends ArrayAdapter<SearchSession> {
        private LayoutInflater inflater;

        private ArrayList<SearchSession> mSearchedSession;

        public SelectArralAdapter(Context context, List<SearchSession> searchedSession) {
            super(context, R.layout.simple_row_appointment_list, R.id.noteTextView, searchedSession);
            // Cache the LayoutInflate to avoid asking for a new one each time.
            inflater = LayoutInflater.from(context);
            this.mSearchedSession = (ArrayList<SearchSession>) searchedSession;
        }

        public void refresh(ArrayList<SearchSession> items) {
            this.mSearchedSession = new ArrayList<>(items);
            this.mSearchedSession.clear();
            this.mSearchedSession = items;
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Planet to display
            SearchSession planet = (SearchSession) this.getItem(position);

            // The child views in each row.
            CheckBox checkBox;
            TextView textView;

            // Create a new row view
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.simple_row_appointment_list, null);

                // Find the child views.
                textView = (TextView) convertView.findViewById(R.id.noteTextView);
                checkBox = (CheckBox) convertView.findViewById(R.id.checkBoxIsOn);
                // Optimization: Tag the row with it's child views, so we don't
                // have to
                // call findViewById() later when we reuse the row.
                convertView.setTag(new SelectViewHolder(textView, checkBox));
                // If CheckBox is toggled, update the planet it is tagged with.
                checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        SearchSession planet = (SearchSession) cb.getTag();
                        planet.setSearchOn(cb.isChecked());
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

            // Tag the CheckBox with the Planet it is displaying, so that we can
            // access the planet in onClick() when the CheckBox is toggled.
            checkBox.setTag(planet);
            // Display planet data
            checkBox.setChecked(planet.isSearchOn());
            textView.setText(planet.getSpannableToString());
            return convertView;
        }

        @Override
        public int getCount() {
            return mSearchedSession.size();
        }
    }
}
