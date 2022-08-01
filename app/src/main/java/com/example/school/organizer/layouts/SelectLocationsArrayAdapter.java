package com.example.school.organizer.layouts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.school.organizer.R;
import com.example.school.organizer.models.AddressRow;
import com.example.school.organizer.models.SelectViewHolder;

import java.util.List;

public class SelectLocationsArrayAdapter extends ArrayAdapter<AddressRow> {
    private LayoutInflater inflater;

    public SelectLocationsArrayAdapter(Context context, List<AddressRow> list) {
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
