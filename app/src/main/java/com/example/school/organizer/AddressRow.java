package com.example.school.organizer;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Jeff's world on 8/27/2016.
 */
public class AddressRow implements Serializable{

    private String title = "";
    private boolean isChecked = false;

    AddressRow() {

    }

    public AddressRow(String title) {
        this.title = title;
    }

    public AddressRow(String title, boolean isChecked) {
        this.title = title;
        this.isChecked = isChecked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return this.title;
    }

    public void toggleChecked() {
        isChecked = !isChecked;
    }

}
