package com.example.school.organizer.models;

import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Location;
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
public class SearchSession implements Serializable {

    private ArrayList<Address> searchedAddresses;
    private String note;
    private boolean isSearchOn = true;
    private long endMuteTime = 0;
    private boolean[] isChecked;

    public SearchSession(ArrayList<Address> searchedAddresses, String note, boolean isSearchOn) {
        this.searchedAddresses = searchedAddresses;
        this.note = note;
        this.isSearchOn = isSearchOn;
        isChecked = new boolean[searchedAddresses.size()];
        initBooleanArrayListWithPositive();
    }

    private void initBooleanArrayListWithPositive() {
        for (int i = 0; i < isChecked.length; i++) {
            isChecked[i] = true;
        }
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
        return String.format("%s\nMarked locations: %d from %d total."
                , getNote(), countCheckAddresses() ,getSearchedAddresses().size());
    }

    public int countCheckAddresses() {
        int countCheckedAddresses = 0;
        for (int i = 0; i < isChecked.length; i++) {
            if(isChecked[i]) countCheckedAddresses++;
        }

        return countCheckedAddresses;
    }

    public CharSequence getSpannableToString() {
        String text1 = getNote();
        String text2 = "Marked locations " + countCheckAddresses() + " from "
                + getSearchedAddresses().size() +" total.";

        SpannableString span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(75), 0, text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span1.setSpan(new StyleSpan(Typeface.BOLD), 0, text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);


        SpannableString span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(45), 0, text2.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, text2.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // let's put both spans together with a separator and all
        CharSequence finalText = TextUtils.concat(span1, "\n" , span2);
        return finalText;
    }

    public void toggleChecked() {
        this.isSearchOn = !isSearchOn;
    }

    /**
     * Checks if current location is near radiusInMeter to the nearest location from
     * searchedAddresses.
     * @param location second point to calculate distance
     * @param radiusInMeter the distance limit to the location
     * @return true if radiusInMeter >= to the distance from the location, otherwise it returns false
     */
    public boolean isNear(Location location, float radiusInMeter) {
        float[] distance = new float[2];
        Address nearestAddress = getNearestAddress(location);
        if ( nearestAddress == null ) {
            return false;
        }

        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                nearestAddress.getLatitude(), nearestAddress.getLongitude(),
                distance);

        return radiusInMeter >= distance[0];
    }

    /**
     * The method is going to return the nearest address, from the checked addresses according to
     * (isChecked[]), by the location given.
     * @param location location point from which is goint to measure the distance
     * @return returns the nearest address. If there is no active addresses to search it will return
     * null.
     */
    public Address getNearestAddress(Location location) {
        if(countCheckAddresses() == 0) {
            return null;
        }
        Address nearestAddress = getFirstCheckedAddress();
        if (getSearchedAddresses().size() > 1) {
            Address adr;
            float nearestDistance[] = new float[2];
            float currentDistance[] = new float[2];

            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    nearestAddress.getLatitude(), nearestAddress.getLongitude(),
                    nearestDistance);

            for (int i = 0; i < searchedAddresses.size(); i++) {
                if(this.isChecked[i]) {
                    adr = searchedAddresses.get(i);
                } else {
                    continue;
                }

                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        adr.getLatitude(), adr.getLongitude(), currentDistance);

                if(currentDistance[0] < nearestDistance[0]) {
                    nearestAddress = adr;
                    nearestDistance[0] = currentDistance[0];
                }
            }
        }

        return nearestAddress;
    }

    /**
     * Will return the firstCheckedAddress. Returns null when there is no checked addresses.
     * @return Address - the firstCheckedAddress. Returns null when there is no checked addresses.
     */
    private Address getFirstCheckedAddress() {
        for (int i = 0; i < isChecked.length; i++) {
            if(isChecked[i]) {
                return searchedAddresses.get(i);
            }
        }
        return null;
    }

    public float[] getDistanceBetweenLocation(Location location) {
        Address nearestAddress = getNearestAddress(location);
        if(nearestAddress == null) {
            return  null;
        }
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

    public boolean[] getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean[] isChecked) {
        this.isChecked = isChecked;
    }
}
