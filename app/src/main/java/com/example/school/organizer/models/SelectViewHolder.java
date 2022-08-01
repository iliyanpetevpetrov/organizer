package com.example.school.organizer.models;

import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by Jeff's world on 8/27/2016.
 */
public class SelectViewHolder {
    private final CheckBox checkBox;
    private final TextView textView;

    public SelectViewHolder(TextView textView, CheckBox checkBox) {
        this.checkBox = checkBox;
        this.textView = textView;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public TextView getTextView() {
        return textView;
    }

}
