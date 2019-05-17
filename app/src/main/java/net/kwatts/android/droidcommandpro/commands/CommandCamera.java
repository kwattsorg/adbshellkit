package net.kwatts.android.droidcommandpro.commands;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.androidhiddencamera.HiddenCameraService;

import net.kwatts.android.droidcommandpro.ApiReceiver;
import net.kwatts.android.droidcommandpro.App;
import net.kwatts.android.droidcommandpro.R;

import org.json.JSONException;
import org.json.JSONObject;


import timber.log.Timber;


import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
//import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

// https://github.com/kevalpatel2106/android-hidden-camera
// https://github.com/termux/termux-api/blob/master/app/src/main/java/com/termux/api/PhotoAPI.java
//TODO: it right... https://github.com/termux/termux-api/blob/master/app/src/main/java/com/termux/api/MicRecorderAPI.java
//            CameraCommandHandler handler = getCameraCommandHandler(intent.getAction());
//            /CameraCommandResult result1 = handler.handle(context, intent);
public class CommandCamera {

    public static String cmd = "cmd_camera";
    public static String[] permissions = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public static void onReceive(final Context context, final Intent intent) {
        Intent cameraService = new Intent(context, HiddenCameraCommandService.class);
        cameraService.setAction(intent.getAction());
        cameraService.putExtras(intent.getExtras());
        context.startService(cameraService);
    }

    public static class HiddenCameraCommandService extends HiddenCameraService {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }


        public static boolean storegallery = true;
        //f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/adbshellkit/" + saveFilename));
        //if (!f.exists())
        //    f.mkdirs();
        public static String IMAGESTOREDIR = "/storage/emulated/0/DCIM/adbshellkit";


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            String command = intent.getAction();
            Context context = getApplicationContext();

            Timber.d("CommandCamera intent with command:" + command);

            CameraCommandResult result = new CameraCommandResult();


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                    Timber.i("Setting up picture from options...");

                    int camerafacing = intent.getIntExtra("camerafacing", CameraFacing.REAR_FACING_CAMERA); // 0 = rear, 1 = front
                    int cameraresolution = intent.getIntExtra("cameraresolution", CameraResolution.MEDIUM_RESOLUTION);
                    int camerafocus = intent.getIntExtra("camerafocus", CameraFocus.AUTO);
                    int imageformat = intent.getIntExtra("imageformat", CameraImageFormat.FORMAT_JPEG);
                    int imagerotation = intent.getIntExtra("imagerotation", CameraRotation.ROTATION_0);
                    storegallery = intent.getBooleanExtra("storegallery", true);

                    CameraConfig cameraConfig = new CameraConfig();


                    String storageFilename =  File.separator
                            + "IMG_" + System.currentTimeMillis()   //IMG_214515184113123.png
                            + (imageformat == CameraImageFormat.FORMAT_JPEG ? ".jpeg" : ".png");
                    File storageFile;
                    File storageFileFull;

                    if (storegallery) {
                        File storageDir=new File(IMAGESTOREDIR);
                        if(!storageDir.exists()){
                            storageDir.mkdir();
                        }
                        storageFileFull = new File(IMAGESTOREDIR + storageFilename);

                        //storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera" + saveFilename);
                        //if (!storageDir.exists())
                        //    storageDir.mkdirs();

                    } else {
                        String cacheDir = context.getExternalCacheDir() == null ?
                                context.getCacheDir().getAbsolutePath() : context.getExternalCacheDir().getAbsolutePath();
                        storageFileFull = new File(cacheDir + storageFilename);
                    }

                    cameraConfig.getBuilder(this)
                            .setCameraFacing(camerafacing)
                            .setImageFile(storageFileFull)
                            .setCameraResolution(cameraresolution)
                            .setCameraFocus(camerafocus)
                            .setImageFormat(imageformat)
                            .setImageRotation(imagerotation)
                            .build();

                    result.message = "Image capture started: " + storageFileFull.getAbsolutePath() +
                            "\ncamerafacing: " + camerafacing +
                            "\ncameraresolution: " + cameraresolution +
                            "\ncamerafocus: " + camerafocus +
                            "\nimageformat: " + imageformat +
                            "\nimagerotation: " + imagerotation +
                            "\nstoregallery: " + storegallery;

                    startCamera(cameraConfig);

                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Timber.d("CommandCamera.onStartCommand: Taking picture!");
                            takePicture();
                        }
                    }, 2000L);
                } else {
                    //Open settings to grant permission for "Draw other apps".
                    result.error = "You need to grant permission allowing drawing over other apps";
                    HiddenCameraUtils.openDrawOverPermissionSetting(this);
                }
            } else {
                //TODO Ask your parent activity for providing runtime permission
                result.error = "Camera permission not available";
            }



            postCameraCommandResult(context, intent, result);

            return Service.START_NOT_STICKY;
        }

        @Override
        public void onImageCapture(@NonNull File imageFile) {
            Timber.d("CommandCamera.onImageCapture: Captured the Picture! " + "filename=" + imageFile.getAbsolutePath()
                        + ",length=" + imageFile.length());
/*
            String moveFileName = imageFile.getAbsolutePath() + "/" + getDefaultCameraImageFilename();
            boolean isMoved = imageFile.renameTo(new File(moveFileName));
            Timber.d("Renamed file: isMoved=" + isMoved + ",moveFileName=" + moveFileName); */

            Timber.d("Adding to media scanner...");
            if (storegallery) {
                addToGallery(getApplicationContext(),imageFile);
            }
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
            stopSelf();
        }

        @Override
        public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
            Timber.i("CommandCamera.onCameraError errorCode: " + errorCode);
            switch (errorCode) {
                case CameraError.ERROR_CAMERA_OPEN_FAILED:
                    //Camera open failed. Probably because another application
                    //is using the camera
                    Timber.e("Unable to open camera, is another application using it?");
                    break;
                case CameraError.ERROR_IMAGE_WRITE_FAILED:
                    //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                    Timber.e("Unable to write image to storage");
                    break;
                case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                    //camera permission is not available
                    //Ask for the camera permission before initializing it.
                    Timber.e("Unable to get camera permission");
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                    //Display information dialog to the user with steps to grant "Draw over other app"
                    //permission for the app.
                    HiddenCameraUtils.openDrawOverPermissionSetting(this);
                    break;
                case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                    Timber.e("Unable to get access to front camera");
                    break;
            }

            stopSelf();
        }

        public static void addToGallery(Context context, File photoPath) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photoPath);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        }

        public static String getDefaultCameraImageFilename() {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            Date date = new Date();
            return "CameraImage_" + dateFormat.format(date) + ".jpeg";
        }

        public static void postCameraCommandResult(final Context context, final Intent intent, final CameraCommandResult result) {
            ResultReturner.returnData(context, intent, out -> {
                out.append(result.message).append("\n");
                if (result.error != null) {
                    out.append(result.error).append("\n");
                    Timber.d("CameraCommand error: " + result.error);
                }
                Timber.d("CameraCommand message: "+ result.message);
                out.flush();
                out.close();
            });
        }

        /**
         * Simple POJO to store result of executing a Camera command
         */
        static class CameraCommandResult {
            public String message = "";
            public String error;
        }



    }
}
