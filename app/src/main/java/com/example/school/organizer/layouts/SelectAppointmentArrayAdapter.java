package com.example.school.organizer.layouts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.school.organizer.R;
import com.example.school.organizer.models.SearchSession;
import com.example.school.organizer.models.SelectViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SelectAppointmentArrayAdapter extends ArrayAdapter<SearchSession> {
    private final LayoutInflater inflater;
    private ArrayList<SearchSession> mSearchedSession;

    public SelectAppointmentArrayAdapter(Context context, List<SearchSession> searchedSession) {
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
            // Reuse existing row view
        } else {
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
