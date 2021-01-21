package in.ashprog.unitednotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends Fragment {

    private String postId;
    private ArrayList<ParseObject> commentsList;
    private CommentsAdapter commentsAdapter;
    private TextView noComments;
    private EditText commentEditText;

    public CommentsFragment() {
        commentsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        noComments = view.findViewById(R.id.noComments);

        RecyclerView commentsRCV = view.findViewById(R.id.commentsRCV);
        commentsRCV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        commentsAdapter = new CommentsAdapter(getContext(), commentsList);
        commentsRCV.setAdapter(commentsAdapter);

        ImageButton postButton = view.findViewById(R.id.postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        commentEditText = view.findViewById(R.id.commentEditText);

        return view;
    }

    public void setPostId(String postId) {
        this.postId = postId;
        getComments();
    }

    void getComments() {
        if (postId != null) {
            ParseQuery<ParseObject> query = new ParseQuery<>("Comments");
            query.whereEqualTo("postId", postId);
            query.include("author");
            query.addAscendingOrder("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects != null && objects.size() > 0) {
                        commentsList.clear();
                        for (ParseObject object : objects) {
                            commentsList.add(object);
                            commentsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        noComments.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    void postComment() {
        String comment = commentEditText.getText().toString().trim();
        if (comment.length() > 0) {
            final ParseObject parseObject = new ParseObject("Comments");
            parseObject.put("author", ParseUser.getCurrentUser());
            parseObject.put("postId", postId);
            parseObject.put("comment", comment);
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        commentsList.add(parseObject);
                        commentsAdapter.notifyDataSetChanged();
                        if (noComments.getVisibility() == View.VISIBLE)
                            noComments.setVisibility(View.GONE);
                        commentEditText.setText("");
                    }
                }
            });
        }
    }
}
