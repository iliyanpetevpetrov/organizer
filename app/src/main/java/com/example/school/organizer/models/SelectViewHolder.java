package com.example.school.organizer.models;

import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by Jeff's world on 8/27/2016.
 */
public class SelectViewHolder {
    private CheckBox checkBox;
    private TextView textView;

    public SelectViewHolder() {
    }

    public SelectViewHolder(TextView textView, CheckBox checkBox) {
        this.checkBox = checkBox;
        this.textView = textView;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }
}