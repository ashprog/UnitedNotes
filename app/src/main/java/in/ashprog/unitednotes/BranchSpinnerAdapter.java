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

public class BranchSpinnerAdapter extends ArrayAdapter<BranchSem> {

    ArrayList<BranchSem> branchList;

    public BranchSpinnerAdapter(@NonNull Context context, @NonNull List<BranchSem> objects) {
        super(context, 0, objects);

        branchList = (ArrayList<BranchSem>) objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_layout, parent, false);

        BranchSem item = getItem(position);
        TextView textView = view.findViewById(R.id.branchSemText);
        textView.setText(item.getBranchName());

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dropdown_layout, parent, false);

        BranchSem item = getItem(position);
        TextView textView = view.findViewById(R.id.branchSemText);
        textView.setText(item.getBranchName());

        return view;
    }
}
