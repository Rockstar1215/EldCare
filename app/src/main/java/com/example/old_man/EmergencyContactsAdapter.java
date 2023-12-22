package com.example.old_man;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.HashSet;
import java.util.Set;


import java.util.List;

public class EmergencyContactsAdapter extends ArrayAdapter<String> {
    private final List<String> contacts;
    private final Context context;
    private final Set<Integer> selectedPositions;
    public EmergencyContactsAdapter(Context context, int resource, List<String> contacts) {
        super(context, resource, contacts);
        this.context = context;
        this.contacts = contacts;
        this.selectedPositions = new HashSet<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_emergency_contact, parent, false);
        }

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        TextView textView = convertView.findViewById(R.id.textView);

        String contact = contacts.get(position);

        checkBox.setChecked(false);
        textView.setText(contact);


        // Set a tag to identify the position of the item
        checkBox.setTag(position);

        // Set a click listener to update the selected positions
        checkBox.setOnClickListener(v -> {
            Integer pos = (Integer) v.getTag();
            if (pos != null) {
                if (selectedPositions.contains(pos)) {
                    selectedPositions.remove(pos);
                } else {
                    selectedPositions.add(pos);
                }
            }
        });

        return convertView;
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }
}