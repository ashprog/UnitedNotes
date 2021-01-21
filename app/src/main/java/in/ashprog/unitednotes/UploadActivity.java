package in.ashprog.unitednotes;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class UploadActivity extends AppCompatActivity implements TextWatcher {

    TextView pathTextView, topicTextView;
    TextInputLayout topicTextLayout, descTextLayout;
    ImageView fileTypeImageView;
    Button uploadButton;
    SpinnerFragment spinnerFragment;

    byte[] fileData = null;
    String fileNameAndType, fileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        spinnerFragment = (SpinnerFragment) getSupportFragmentManager().findFragmentById(R.id.uploadSpinnerFragment);

        pathTextView = findViewById(R.id.pathTextView);
        topicTextView = findViewById(R.id.topicTextView);
        topicTextLayout = findViewById(R.id.topicTextLayout);
        descTextLayout = findViewById(R.id.descTextLayout);
        fileTypeImageView = findViewById(R.id.fileTypeImageView);
        uploadButton = findViewById(R.id.uploadButton);

        topicTextLayout.getEditText().addTextChangedListener(this);
        uploadButton.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2 && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            pickFileIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
            initializeFileData(data.getData());
            uploadButton.setEnabled(true);
            uploadButton.setText("UPLOAD");
        }
    }

    void initializeFileData(Uri uri) {
        String fullPath = uri.getPath();
        if (fullPath.endsWith(".pdf") || fullPath.endsWith(".doc") || fullPath.endsWith(".docx") || fullPath.endsWith(".ppt") || fullPath.endsWith(".pptx")) {
            File file = new File(fullPath);
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileInputStream fis;
                fis = (FileInputStream) getContentResolver().openInputStream(uri);
                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buf)))
                    baos.write(buf, 0, n);
                fileData = baos.toByteArray();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            fileNameAndType = file.getName();
            String[] splitFileNameAndType = fileNameAndType.split("\\.");
            fileType = splitFileNameAndType[splitFileNameAndType.length - 1];
            pathTextView.setText("Selected file: " + fileNameAndType);
            topicTextLayout.getEditText().setText(splitFileNameAndType[0]);
            switch (fileType) {
                case "pdf":
                    fileTypeImageView.setImageResource(R.drawable.pdf_100);
                    break;
                case "doc":
                case "docx":
                    fileTypeImageView.setImageResource(R.drawable.doc_100);
                    break;
                case "ppt":
                case "pptx":
                    fileTypeImageView.setImageResource(R.drawable.ppt_100);
                    break;
            }
        } else {
            Toast.makeText(this, "Invalid file format. Supported formats are pdf/doc/ppt.", Toast.LENGTH_SHORT).show();
        }
    }

    public void upload(final View view) {
        String topic = topicTextLayout.getEditText().getText().toString(),
                desc = descTextLayout.getEditText().getText().toString();
        if (fileData != null && topic.length() > 0 && spinnerFragment.getBranchName() != null && spinnerFragment.getSem() != null && spinnerFragment.getSubjectName() != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading file...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();

            final ParseObject parseObject = new ParseObject("Posts");
            parseObject.put("authorName", ParseUser.getCurrentUser().get("name"));
            parseObject.put("author", ParseUser.getCurrentUser());
            parseObject.put("branch", spinnerFragment.getBranchName());
            parseObject.put("sem", spinnerFragment.getSem());
            parseObject.put("subject", spinnerFragment.getSubjectName());
            parseObject.put("topic", topic);
            parseObject.put("description", desc);
            parseObject.put("fileType", fileType);

            final ParseFile parseFile = new ParseFile(fileNameAndType, fileData);
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        parseObject.put("file", parseFile);
                        try {
                            parseObject.save();
                            Snackbar.make(view, "Note added successfully.", BaseTransientBottomBar.LENGTH_SHORT).show();
                            uploadButton.setText("UPLOADED");
                            uploadButton.setEnabled(false);
                        } catch (ParseException ex) {
                            Snackbar.make(view, ex.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(view, e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer percentDone) {
                    progressDialog.setProgress(percentDone);
                }
            });
        } else {
            if (fileData == null)
                Snackbar.make(view, "Please select the file.", BaseTransientBottomBar.LENGTH_SHORT).show();
            else
                Snackbar.make(view, "Please fill all the required fields.", BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    public void pickFile(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        } else {
            pickFileIntent();
        }
    }

    void pickFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 3);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        topicTextView.setText(s);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
