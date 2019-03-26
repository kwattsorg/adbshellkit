package net.kwatts.android.droidcommandpro.commands;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import android.content.*;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.JsonWriter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.kwatts.android.droidcommandpro.AdbshellkitApiReceiver;

import timber.log.Timber;
//TODO: this should sync the users files/home directory
// https://github.com/firebase/snippets-android/blob/7d03e65500cd63a26e5bf9b8b6e4d3ab9479806a/storage/app/src/main/java/com/google/firebase/referencecode/storage/StorageActivity.java#L194-L208
public class CommandUploadFile  {

    public static String cmd = "cmd_upload_file";
    public static String[] permissions = { "" };

    public static void onReceive(final AdbshellkitApiReceiver apiReceiver, final Context context, final Intent intent) {

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
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Timber.e(exception);
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Timber.d("Successfully uploaded file!" + taskSnapshot.getMetadata().getPath());
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                            }
                        });
                        out.name("API_SUCCESS").value(filename + " uploaded!");
                    }


                }
                out.endObject();
            }
        });
    }

}

