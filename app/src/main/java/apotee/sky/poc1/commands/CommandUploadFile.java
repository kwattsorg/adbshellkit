package apotee.sky.poc1.commands;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.JsonWriter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import apotee.sky.poc1.ApiReceiver;

import java.io.File;

import timber.log.Timber;

//TODO: this should sync the users files/home directory
// https://github.com/firebase/snippets-android/blob/7d03e65500cd63a26e5bf9b8b6e4d3ab9479806a/storage/app/src/main/java/com/google/firebase/referencecode/storage/StorageActivity.java#L194-L208
public class CommandUploadFile {
    public static int MINIMUM_APP_VERSION = 100;
    public static String cmd = "upload_file";
    public static String descr = "Sets or Retrieves a file from remote storage";
    public static String args = "--es input_method=[text|webview|confirm|checkbox|date|radio|sheet] \n" +
            "METHOD OPTIONS:\n" +
            "\ttext: --ez multiple_lines [true|false]" + "\n" +
            "\twebview: --es web_url [url to show] OR --es web_text [html text]";
    public static String[] permissions = {};


    public static void onReceive(final ApiReceiver apiReceiver, final Context context, final Intent intent) {

        final String filename = intent.getStringExtra("filename");
        ResultReturner.returnData(context, intent, new ResultReturner.ResultJsonWriter() {
            public void writeJson(JsonWriter out) throws Exception {
                out.beginObject();
                if (filename == null) {
                    out.name("API_ERROR").value("No filename given");
                } else {
                    final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    final FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();

                    StorageReference userRef = mFirebaseStorage.getReference().child("files").child(mFirebaseUser.getUid());
                    out.name("source_filename").value(filename);
                    out.name("target_firebase_path").value(userRef.getPath());
                    File uploadFile = new File(filename);
                    if (!uploadFile.exists()) {
                        out.name("API_ERROR").value("does not exist: " + filename);
                    } else {
                        Uri file = Uri.fromFile(uploadFile);
                        StorageReference riversRef = userRef.child(file.getLastPathSegment());
                        UploadTask uploadTask = riversRef.putFile(file);
                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(Timber::e).addOnSuccessListener(taskSnapshot -> {
                            Timber.d("Successfully uploaded file!%s", taskSnapshot.getMetadata().getPath());
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                        });
                        out.name("API_SUCCESS").value(filename + " uploaded!");
                    }

                }
                out.endObject();
            }
        });
    }

}

