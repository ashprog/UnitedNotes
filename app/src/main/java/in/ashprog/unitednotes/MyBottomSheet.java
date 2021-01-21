package in.ashprog.unitednotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class MyBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

    ParseObject comment;

    TextInputLayout editCommentText;
    ImageButton editCommentSubmit;
    CommentsAdapter currentAdapter;

    public MyBottomSheet(ParseObject comment, CommentsAdapter currentAdapter) {
        this.comment = comment;
        this.currentAdapter = currentAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_bottom_sheet, container, false);

        editCommentText = view.findViewById(R.id.editCommentText);
        editCommentSubmit = view.findViewById(R.id.editCommentSubmit);

        editCommentText.getEditText().setText(comment.get("comment").toString());
        editCommentSubmit.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.editCommentSubmit) {
            String editedComment = editCommentText.getEditText().getText().toString().trim();
            if (editedComment.length() > 0) {
                comment.put("comment", editedComment);
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            currentAdapter.notifyDataSetChanged();
                            dismiss();
                        }
                    }
                });
            }
        }
    }
}
