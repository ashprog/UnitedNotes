package in.ashprog.unitednotes;

import java.util.ArrayList;

public class BranchSem {

    ArrayList<ArrayList<String>> subjectsList;
    private String branchName;
    private int numOfSems;

    public BranchSem(String branchName, int noOfSems, ArrayList<ArrayList<String>> subjectsList) {
        this.branchName = branchName;
        this.numOfSems = noOfSems;
        if (subjectsList != null)
            this.subjectsList = subjectsList;
        else
            this.subjectsList = new ArrayList<>();
    }

    public String getBranchName() {
        return branchName;
    }

    public int getNoOfSems() {
        return numOfSems;
    }

    public ArrayList<String> getSubjectsList(int sem) {
        return subjectsList.get(sem - 1);
    }
}
