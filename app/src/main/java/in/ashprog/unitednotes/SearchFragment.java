package in.ashprog.unitednotes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static in.ashprog.unitednotes.HomeActivity.fab;

public class SearchFragment extends Fragment implements View.OnClickListener, TextWatcher {

    ArrayList<String> topics;
    ArrayList<ParseObject> posts;
    ArrayAdapter<String> arrayAdapter;
    PostsCustomAdapter postsCustomAdapter;
    RecyclerView recyclerView;
    AutoCompleteTextView autoCompleteTextView;
    CardView loadingCardView;
    Animation anim;
    TextView noResultTextView;

    ParseQuery<ParseObject> query;

    private int limit = 0;
    private boolean noMorePosts = false;

    public SearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ImageView dropImageView = view.findViewById(R.id.downImageView);
        dropImageView.setOnClickListener(this);

        loadingCardView = view.findViewById(R.id.loadingCardView);
        noResultTextView = view.findViewById(R.id.noResultTextView);
        noResultTextView.setVisibility(View.GONE);

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        topics = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item, topics);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(arrayAdapter);
        autoCompleteTextView.addTextChangedListener(this);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        posts = new ArrayList<>();
        postsCustomAdapter = new PostsCustomAdapter(getContext(), posts);
        postsCustomAdapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached() {
                if (!noMorePosts) {
                    getList();
                }
            }
        });
        recyclerView.setAdapter(postsCustomAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (fab != null && dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (fab != null && dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        posts.clear();
        topics.clear();
        getList();

        return view;
    }

    void getList() {
        final String branchName = HomeFragment.spinnerFragment.getBranchName(),
                sem = HomeFragment.spinnerFragment.getSem(),
                subjectName = HomeFragment.spinnerFragment.getSubjectName();
        if (branchName != null && sem != null && subjectName != null) {
            anim = new AnimationUtils().loadAnimation(getContext(), R.anim.loading_enter_from_left);
            anim.setFillAfter(true);
            anim.setFillEnabled(true);
            loadingCardView.startAnimation(anim);

            query = new ParseQuery<>("Posts");
            query.whereEqualTo("branch", branchName);
            query.whereEqualTo("sem", sem);
            query.whereEqualTo("subject", subjectName);
            query.orderByDescending("downloads");
            query.setSkip(limit);
            query.setLimit(limit += 10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    try {
                        if (e == null && objects.size() > 0) {
                            posts.addAll(objects);
                            postsCustomAdapter.notifyDataSetChanged();
                            for (ParseObject object : objects) {
                                topics.add(object.get("topic").toString());
                            }
                            arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item, topics);
                            autoCompleteTextView.setAdapter(arrayAdapter);
                            if (objects.size() < 10)
                                noMorePosts = true;
                        } else {
                            noMorePosts = true;
                            noResultTextView.setVisibility(View.VISIBLE);
                        }

                        anim = new AnimationUtils().loadAnimation(getContext(), R.anim.loading_exit_to_left);
                        anim.setFillAfter(true);
                        anim.setFillEnabled(true);
                        loadingCardView.startAnimation(anim);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } else {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.downImageView:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            int position = topics.indexOf(s.toString());
            if (position != -1)
                recyclerView.scrollToPosition(position);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onDestroyView() {
        if (fab != null && fab.getVisibility() != View.VISIBLE)
            fab.show();

        super.onDestroyView();
    }
}
