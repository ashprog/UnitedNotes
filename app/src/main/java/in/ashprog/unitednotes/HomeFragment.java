package in.ashprog.unitednotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment implements View.OnClickListener {

    static SpinnerFragment spinnerFragment;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView textView4 = view.findViewById(R.id.textView4);
        textView4.setText("Hii " + HomeActivity.name.split(" ")[0] + ",");
        Button searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

        spinnerFragment = (SpinnerFragment) getChildFragmentManager().findFragmentById(R.id.spinnerContainer);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchButton:
                if (spinnerFragment.getBranchName() != null && spinnerFragment.getSem() != null && spinnerFragment.getSubjectName() != null) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
                    transaction.add(R.id.fragmentContainer, new SearchFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    Snackbar.make(v, "Please select all the fields.", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
