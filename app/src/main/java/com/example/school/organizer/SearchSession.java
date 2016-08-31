package com.example.school.organizer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.location.Address;
import android.location.Location;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jeff's world on 8/29/2016.
 */
public class SearchSession implements Serializable{

    private ArrayList<Address> searchedAddresses;
    private String note;
    private boolean isSearchOn = true;
    private long endMuteTime = 0;

    public SearchSession(ArrayList<Address> searchedAddresses, String note, boolean isSearchOn) {
        this.searchedAddresses = searchedAddresses;
        this.note = note;
        this.isSearchOn = isSearchOn;
    }

    public ArrayList<Address> getSearchedAddresses() {
        return searchedAddresses;
    }

    public void setSearchedAddresses(ArrayList<Address> searchedAddresses) {
        this.searchedAddresses = searchedAddresses;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSearchOn() {
        return isSearchOn;
    }

    public void setSearchOn(boolean searchOn) {
        isSearchOn = searchOn;
    }

    @Override
    public String toString() {
        return String.format("%s\nMarked locations: %d."
                , getNote(), getSearchedAddresses().size());
    }

    private int countCheckAddresses() {
        int countCheckedAddresses = 0;
        for (int i = 0; i < searchedAddresses.size(); i++) {
            searchedAddresses.get(i).
        }

    }

    public CharSequence getSpannableToString() {
        String text1 = getNote();
        String text2 = "Marked locations: " + getSearchedAddresses().size();

        SpannableString span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(75), 0, text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span1.setSpan(new StyleSpan(Typeface.BOLD), 0, text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);


        SpannableString span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(55), 0, text2.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, text2.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

// let's put both spans together with a separator and all
        CharSequence finalText = TextUtils.concat(span1, "\n" , span2);
        return finalText;
    }

    public void toggleChecked() {
        this.isSearchOn = !isSearchOn;
    }

    public boolean isNear(Location location, float radiusInMeter) {
        float distance[] = new float[2];
        Address nearestAddress = getNearestAddress(location);

        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                nearestAddress.getLatitude(), nearestAddress.getLongitude(),
                distance);

        return radiusInMeter >= distance[0];
    }

    public Address getNearestAddress(Location location) {
        if (getSearchedAddresses().size() > 2) {
            Address nearestAddress = getSearchedAddresses().get(0);
            float nearestDistance[] = new float[2];
            float currentDistance[] = new float[2];

            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    nearestAddress.getLatitude(), nearestAddress.getLongitude(),
                    nearestDistance);

            for (Address adr: getSearchedAddresses()) {
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        adr.getLatitude(), adr.getLongitude(), currentDistance);

                if(currentDistance[0] < nearestDistance[0]) {
                    nearestAddress = adr;
                    nearestDistance[0] = currentDistance[0];
                }
            }

            return nearestAddress;
        }

        return getSearchedAddresses().get(0);
    }

    public float[] getDistanceBetweenLocation(Location location) {
        Address nearestAddress = getNearestAddress(location);

        float distance[] = new float[2];

        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                nearestAddress.getLatitude(), nearestAddress.getLongitude(),
                distance);
        return distance;
    }

    public boolean isMute() {
        if(endMuteTime != 0) {
            long now = System.currentTimeMillis();

            if( now >= endMuteTime ) {
                endMuteTime = 0;
                return false;
            }
            return true;
        }

        return false;
    }

    public void mute() {
        endMuteTime = System.currentTimeMillis() + 300000;
    }

    public void unmute() {
        endMuteTime = 0;
    }

    public long getEndMuteTime() {
        return endMuteTime;
    }
}
