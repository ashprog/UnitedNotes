package in.ashprog.unitednotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;


public class SpinnerFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static ArrayList<BranchSem> branchList;

    static {
        branchList = new ArrayList<>();
        initializeBranchList();
    }

    String branchName, sem, subjectName;
    private int branchPosition;
    private ArrayList<String> semList;
    private ArrayList<String> subjectsList;
    private Spinner branchSpinner, semSpinner, subjectSpinner;
    private BranchSpinnerAdapter branchSpinnerAdapter;
    private SemSpinnerAdapter semSpinnerAdapter;
    private SubjectSpinnerAdapter subjectSpinnerAdapter;

    public SpinnerFragment() {
    }

    static void initializeBranchList() {
        ArrayList<ArrayList<String>> subjects = new ArrayList<>();
        ArrayList<String> subject = new ArrayList<>();

        branchList.clear();
        branchList.add(new BranchSem("Select your branch...", 0, null));

        subjects.clear();
        subject.clear();
        subject.add("Mathematics");
        subject.add("Computer Fundamentals");
        subject.add("Programming in C");
        subject.add("Basic Electronics");
        subject.add("Communication Skills");
        subjects.add((ArrayList<String>) subject.clone());
        subject.clear();
        subject.add("Statistics");
        subject.add("Data and File Structure");
        subject.add("Business System");
        subject.add("Digital Electronics");
        subject.add("C++");
        subjects.add((ArrayList<String>) subject.clone());
        subject.clear();
        subject.add("Discrete Mathematics");
        subject.add("Design & Analysis of Algorithms");
        subject.add("Java");
        subject.add("Computer Organizations");
        subject.add("DBMS");
        subjects.add((ArrayList<String>) subject.clone());
        subject.clear();
        subject.add("Numerical Methods");
        subject.add("Operating System");
        subject.add("Dot NET");
        subject.add("Cyber Law");
        subject.add("Software Engineering");
        subjects.add((ArrayList<String>) subject.clone());
        subject.clear();
        subject.add("Computer Graphics");
        subject.add("Multimedia Systems");
        subject.add("Networks");
        subject.add("Web Design");
        subjects.add((ArrayList<String>) subject.clone());
        subject.clear();
        subject.add("Subjects Not Available");
        subjects.add((ArrayList<String>) subject.clone());
        branchList.add(new BranchSem("BCA", 6, (ArrayList<ArrayList<String>>) subjects.clone()));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spinner, container, false);

        branchSpinner = view.findViewById(R.id.branchSpinner);
        branchSpinnerAdapter = new BranchSpinnerAdapter(getContext(), branchList);
        branchSpinner.setAdapter(branchSpinnerAdapter);
        branchSpinner.setOnItemSelectedListener(this);

        semSpinner = view.findViewById(R.id.semSpinner);
        semList = new ArrayList<>();
        semList.add("Sem");
        semSpinnerAdapter = new SemSpinnerAdapter(getContext(), semList);
        semSpinner.setAdapter(semSpinnerAdapter);
        semSpinner.setOnItemSelectedListener(this);

        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        subjectsList = new ArrayList<>();
        subjectsList.add("Select subject...");
        subjectSpinnerAdapter = new SubjectSpinnerAdapter(getContext(), subjectsList);
        subjectSpinner.setAdapter(subjectSpinnerAdapter);
        subjectSpinner.setOnItemSelectedListener(this);

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.branchSpinner:
                if (position != 0) {
                    branchPosition = position;
                    branchName = branchList.get(position).getBranchName();
                    initializeSemList(branchList.get(position).getNoOfSems());
                } else {
                    semList.clear();
                    semList.add("Sem");
                    semSpinnerAdapter.notifyDataSetChanged();
                    subjectsList.clear();
                    subjectsList.add("Select subject...");
                    subjectSpinnerAdapter.notifyDataSetChanged();
                }
                sem = null;
                subjectName = null;
                semSpinner.setSelection(0, true);
                subjectSpinner.setSelection(0, true);
                break;

            case R.id.semSpinner:
                if (position != 0) {
                    sem = semList.get(position);
                    initializeSubjectList(branchPosition, Integer.parseInt(semList.get(position)));
                } else {
                    subjectsList.clear();
                    subjectsList.add("Select subject...");
                    subjectSpinnerAdapter.notifyDataSetChanged();
                }
                subjectName = null;
                subjectSpinner.setSelection(0, true);
                break;

            case R.id.subjectSpinner:
                if (position != 0)
                    subjectName = subjectsList.get(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    void initializeSemList(int sem) {
        semList.clear();
        semList.add("Sem");
        for (int i = 1; i <= sem; i++)
            semList.add(Integer.toString(i));

        semSpinnerAdapter.notifyDataSetChanged();
    }

    void initializeSubjectList(int branchPosition, int sem) {
        subjectsList.clear();
        subjectsList.add("Select subject...");
        subjectsList.addAll(branchList.get(branchPosition).getSubjectsList(sem));
        subjectSpinnerAdapter.notifyDataSetChanged();
    }

    public String getBranchName() {
        return branchName;
    }

    public String getSem() {
        return sem;
    }

    public String getSubjectName() {
        return subjectName;
    }
}
