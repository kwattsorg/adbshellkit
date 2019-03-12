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
import android.support.annotation.NonNull;

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

import timber.log.Timber;
//TODO: this should sync the users files/home directory
// https://github.com/firebase/snippets-android/blob/7d03e65500cd63a26e5bf9b8b6e4d3ab9479806a/storage/app/src/main/java/com/google/firebase/referencecode/storage/StorageActivity.java#L194-L208
public class CommandUploadFile implements Command {

    public static String cmd = "cmd_upload_file";


    public String getCommandName() {
        return cmd;
    }
    public String[] getPermissions() {
        return new String[]{""};
    }


    public JSONObject execute (android.content.Context ctx, List <String> args){
            JSONObject res = new JSONObject();
            String fileName = args.get(0);

            final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();

            StorageReference userRef = mFirebaseStorage.getReference().child("files").child(mFirebaseUser.getUid());


            try {
                res.put("filename", fileName);

                File uploadFile = new File(fileName);


                if (!uploadFile.exists()) {
                    //throw new Exception("does not exist!");
                    Timber.d("file does not exist, not uploading:" + fileName);
                    res.put("error", "does not exist: " + fileName);
                    return res;
                }



                Uri file = Uri.fromFile(uploadFile);
                StorageReference riversRef = userRef.child(file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Timber.e(exception);
                        //res.put("exception_failure", exception.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Timber.d("Successfully uploaded file!" + taskSnapshot.getMetadata().getPath());
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });

                //uploadTask.wait();
                res.put("file_uploaded", fileName);
            } catch (JSONException e) {
                Timber.e(e);

            } catch (Exception e) {
                Timber.e(e);
            }

            return res;
    }
}

