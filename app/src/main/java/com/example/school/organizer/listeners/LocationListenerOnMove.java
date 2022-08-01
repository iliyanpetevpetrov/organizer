package com.example.school.organizer.listeners;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.example.school.organizer.activities.MainActivity;
import com.example.school.organizer.models.SearchSession;


public class LocationListenerOnMove implements LocationListener {
    private final MainActivity mainActivity;

    public LocationListenerOnMove(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (mainActivity.isOn()) {
//                    for (final SearchSession adr : appointmentsTodoList) {
            for (int counter = 0; counter < mainActivity.getAppointmentsTodoList().size(); counter++) {
                final int innerCounter = counter;
                final SearchSession currentAddress = mainActivity.getAppointmentsTodoList().get(counter);

                if (currentAddress.isSearchOn() &&
                        !currentAddress.isMute() &&
                        currentAddress.isNear(location, 1000)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity.getActivity());

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
                                mainActivity.startActivity(mapIntent);
                            }
                        }
                    });

                    builder.setNegativeButton("Mute for 5 minutes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mainActivity.getAppointmentsTodoList().get(innerCounter).mute();
                            mainActivity.showMessageForMutedAppointment(innerCounter);
                        }
                    });

                    builder.setNeutralButton("Mark as done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mainActivity.getAppointmentsTodoList().get(innerCounter).toggleChecked();
                            mainActivity.getListAdapter().notifyDataSetChanged();
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
}
