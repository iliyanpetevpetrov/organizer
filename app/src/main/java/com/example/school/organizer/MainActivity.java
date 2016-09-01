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


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView appointmentsListView;

    private ArrayAdapter<SearchSession> listAdapter;

    FloatingActionButton fTurnOnOff;

    public static String DEFAULT_EMPTY_STRING_APPOINTMENT_LIST = "1. Click me.\n" +
            "2. Now try to delete me\nwith long click.";

    private boolean isOn = false;

    TinyDB tinydb;

    ArrayList<SearchSession> searchSession = new ArrayList<>();

    //TODO: try without initialization
    ArrayList<SearchSession> appointmentsTodoList;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tinydb = new TinyDB(this);

        //Initialize arrayList for main list view
        appointmentsTodoList = tinydb.getListSearchSession("appointmentsTodoList", SearchSession.class);

        boolean isSuccessfulSearch = tinydb.getBoolean("isSuccessfulSearch");

        if (isSuccessfulSearch) {
            ArrayList<SearchSession> tmpSearchSession =
                    tinydb.getListSearchSession("searchedSession", SearchSession.class);
            appointmentsTodoList.addAll(tmpSearchSession);

            //Prevent adding the same searchSession when rotating the phone
            tinydb.putBoolean("isSuccessfulSearch", false);
        }
        tinydb.putListSearchSession("appointmentsTodoList", appointmentsTodoList);

        if (appointmentsTodoList.size() == 0) {
            appointmentsTodoList.add(new SearchSession(
                    new ArrayList<Address>(), DEFAULT_EMPTY_STRING_APPOINTMENT_LIST, false));
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
                                                "The appointment "+"\"" +
                                                appointmentsTodoList.get(position).getNote()
                                                + "\" is unmuted.",
                                                Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }
                        });

                builder.create().show();
                listAdapter.refresh(appointmentsTodoList);
                return true;
            }
        });

        appointmentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SearchSession searchSession = listAdapter.getItem(position);
                if (!searchSession.getNote().equals(DEFAULT_EMPTY_STRING_APPOINTMENT_LIST)) {
                    searchSession.toggleChecked();
                }
                SelectViewHolder viewHolder = (SelectViewHolder) view.getTag();

                viewHolder.getCheckBox().setChecked(searchSession.isSearchOn());
            }

        });


        listAdapter = new SelectArralAdapter(this, appointmentsTodoList);

        appointmentsListView.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {

                if (isOn) {
//                    for (final SearchSession adr : appointmentsTodoList) {
                    for (int counter = 0; counter < appointmentsTodoList.size(); counter++) {
                        final int innerCounter = counter;
                        final SearchSession currentAddress = appointmentsTodoList.get(counter);

                        if (currentAddress.isSearchOn() && !currentAddress.isMute()
                                && currentAddress.isNear(location, 1000)) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            String message = String.format("You are %.0f meters away from \"%s\"" +
                                    " appointment",
                                    currentAddress.getDistanceBetweenLocation(location)[0],
                                    currentAddress.getNote());

                            builder.setPositiveButton("Drive me to", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Address nearestAdr = currentAddress.getNearestAddress(location);
                                    if(nearestAdr != null) {

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

                            Toast.makeText(MainActivity.this,
                                    "Location is " + currentAddress.getDistanceBetweenLocation(location)[0] +
                                            " away.",
                                    Toast.LENGTH_LONG).show();
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // 
            Toast.makeText(MainActivity.this, "Gps access is denied.", Toast.LENGTH_SHORT).show();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 20, locationListener);
    }

    private void showMessageForMutedAppointment(int position) {
        SimpleDateFormat formatter =
                new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(appointmentsTodoList.get(position)
                .getEndMuteTime());
        Toast.makeText(MainActivity.this,
                "The appointment " +"\"" + appointmentsTodoList.get(position).getNote() + "\"" +
                        " is muted until " + formatter.format(calendar.getTime()),
                Toast.LENGTH_LONG).show();
    }

    public Dialog onEditDialog(int position) {
        final ArrayList<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        boolean[] alreadyCheckedItems = appointmentsTodoList.get(position).getIsChecked();

        ArrayList<Address> listAddresses = appointmentsTodoList.get(position).getSearchedAddresses();

        String strings[] = new String[appointmentsTodoList.get(position).getSearchedAddresses().size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new String();
        }

        for (int i = 0; i <listAddresses.size(); i++) {
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

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        for (Integer i:mSelectedItems) {
            alreadyCheckedItems[i] = true;
        }
        appointmentsTodoList.get(position).setIsChecked(alreadyCheckedItems);

        return builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();

        appointmentsTodoList = tinydb.getListSearchSession("appointmentsTodoList", SearchSession.class);
        appointmentsListView.invalidateViews();

        listAdapter = new SelectArralAdapter(this, appointmentsTodoList);

        listAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onPause() {
        super.onPause();
        tinydb.putListSearchSession("appointmentsTodoList", appointmentsTodoList);

        appointmentsListView.invalidateViews();

        tinydb.putBoolean("isOn", isOn);
    }


    public void turnOnOff(View v) {
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


    public class SelectArralAdapter extends ArrayAdapter<SearchSession> {
        private LayoutInflater inflater;

        private ArrayList<SearchSession> searchedSession;

        public SelectArralAdapter(Context context, List<SearchSession> planetList) {
            super(context, R.layout.simple_row_appointment_list, R.id.noteTextView, planetList);
            // Cache the LayoutInflate to avoid asking for a new one each time.
            inflater = LayoutInflater.from(context);
            this.searchedSession = (ArrayList<SearchSession>) planetList;
        }

        public void refresh(ArrayList<SearchSession> items) {
            this.searchedSession = new ArrayList<>(items);
            this.searchedSession.clear();
            this.searchedSession = items;
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
            return appointmentsTodoList.size();
        }
    }

    private Context getActivity() {
        return this;
    }
}
