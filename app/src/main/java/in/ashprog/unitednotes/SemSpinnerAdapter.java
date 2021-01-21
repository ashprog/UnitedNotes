package in.ashprog.unitednotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SemSpinnerAdapter extends ArrayAdapter<String> {

    ArrayList<String> semList;

    public SemSpinnerAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, 0, objects);

        semList = (ArrayList<String>) objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_layout, parent, false);

        TextView textView = view.findViewById(R.id.branchSemText);
        textView.setText(semList.get(position));

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dropdown_layout, parent, false);

        TextView textView = view.findViewById(R.id.branchSemText);
        textView.setText(semList.get(position));

        return view;
    }
}
