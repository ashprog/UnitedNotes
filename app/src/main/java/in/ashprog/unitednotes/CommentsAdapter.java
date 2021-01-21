package in.ashprog.unitednotes;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.DeleteCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    Context context;
    ArrayList<ParseObject> commentsList;

    public CommentsAdapter(Context context, ArrayList<ParseObject> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_list_view, parent, false);

        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsViewHolder holder, int position) {
        ParseObject object = commentsList.get(position);
        ParseUser user = object.getParseUser("author");

        holder.nameTextView.setText(user.get("name").toString());
        holder.commentTextView.setText(object.get("comment").toString());

        ParseFile file = user.getParseFile("image");
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e == null && data != null) {
                    Bitmap bitmap = RescaleImage.getRoundedResizedBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 50);
                    holder.profileImageView.setImageBitmap(bitmap);
                }
            }
        });

        if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            holder.popupMenuImage.setEnabled(true);
            holder.popupMenuImage.setVisibility(View.VISIBLE);
        } else {
            holder.popupMenuImage.setEnabled(false);
            holder.popupMenuImage.setVisibility(View.GONE);
        }
    }

    void editComment(int position) {
        MyBottomSheet myBottomSheet = new MyBottomSheet(commentsList.get(position), this);
        myBottomSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "bottomSheet");
    }

    void deleteComment(final int position) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_delete_24dp)
                .setTitle("Delete comment")
                .setMessage("Are you sure want to delete this ?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseObject object = commentsList.get(position);
                        object.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    commentsList.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Comment deleted.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("NO", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    class CommentsViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

        final PopupMenu popupMenu;
        ImageView profileImageView, popupMenuImage;
        TextView nameTextView, commentTextView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.profileImageView3);
            nameTextView = itemView.findViewById(R.id.nameTextView7);
            commentTextView = itemView.findViewById(R.id.commentTextView8);
            popupMenuImage = itemView.findViewById(R.id.popupMenuImage);

            popupMenu = new PopupMenu(context, popupMenuImage);
            popupMenu.inflate(R.menu.comments_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenuImage.setOnClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.edit_menu:
                    editComment(getAdapterPosition());
                    return true;

                case R.id.delete_menu:
                    deleteComment(getAdapterPosition());
                    return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            popupMenu.show();
        }
    }
}
