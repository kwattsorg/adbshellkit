package apotee.sky.poc1.commands;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.JsonWriter;
import android.util.Size;
import android.util.SizeF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

//import com.androidhiddencamera.config.CameraFocus;

// https://github.com/kevalpatel2106/android-hidden-camera
// https://github.com/termux/termux-api/blob/master/app/src/main/java/com/termux/api/PhotoAPI.java
//TODO: it right... https://github.com/termux/termux-api/blob/master/app/src/main/java/com/termux/api/MicRecorderAPI.java
//            CameraCommandHandler handler = getCameraCommandHandler(intent.getAction());
//            /CameraCommandResult result1 = handler.handle(context, intent);
public class CommandCamera {
    public static int MINIMUM_APP_VERSION = 100;
    public static String cmd = "camera";
    public static String descr = "Silent camera storing pictures in gallery by default";
    public static String args = "--ei camerafacing [0 back, 1 for front]\n" +
            "\t--ei cameraresolution [7821(LOW)|7895(MED)|2006(HIGH)]\n" +
            "\t--ei camerafocus [0 (AUTO)]\n" +
            "\t--ei imageformat [849 (JPEG) | 545 (PNG)]\n" +
            "\t--ei imagerotation [] \n" +
            "\t--ez storegallery [true to add picture to gallery]";
    public static String[] permissions = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public static void onReceive(final Context context, final Intent intent) {
        Intent cameraService = new Intent(context, HiddenCameraCommandService.class);
        cameraService.setAction(intent.getAction());
        cameraService.putExtras(intent.getExtras());
        context.startService(cameraService);
    }

    public static class HiddenCameraCommandService extends HiddenCameraService {
        public static boolean storegallery = true;
        //f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/adbshellkit/" + saveFilename));
        //if (!f.exists())
        //    f.mkdirs();
        public static String IMAGESTOREDIR = "/storage/emulated/0/DCIM/adbshellkit";

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
                    Timber.d("CameraCommand error: %s", result.error);
                }
                Timber.d("CameraCommand message: %s", result.message);
                out.flush();
                out.close();
            });
        }

        public static String getCameraInfo(Context context) throws CameraAccessException, IOException {
            final CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            StringWriter stringWriter = new StringWriter();
            JsonWriter out = new JsonWriter(stringWriter);

            out.beginObject();
            out.beginArray();
            for (String cameraId : manager.getCameraIdList()) {
                out.beginObject();
                out.name("id").value(cameraId);

                CameraCharacteristics camera = manager.getCameraCharacteristics(cameraId);

                out.name("facing");
                int lensFacing = camera.get(CameraCharacteristics.LENS_FACING);
                switch (lensFacing) {
                    case CameraMetadata.LENS_FACING_FRONT:
                        out.value("front");
                        break;
                    case CameraMetadata.LENS_FACING_BACK:
                        out.value("back");
                        break;
                    default:
                        out.value(lensFacing);
                }

                StreamConfigurationMap map = camera.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                out.name("jpeg_output_sizes").beginArray();
                for (Size size : map.getOutputSizes(ImageFormat.JPEG)) {
                    out.beginObject().name("width").value(size.getWidth()).name("height").value(size.getHeight()).endObject();
                }
                out.endArray();

                out.name("focal_lengths").beginArray();
                for (float f : camera.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS))
                    out.value(f);
                out.endArray();

                out.name("auto_exposure_modes").beginArray();
                int[] flashModeValues = camera.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                for (int flashMode : flashModeValues) {
                    switch (flashMode) {
                        case CameraMetadata.CONTROL_AE_MODE_OFF:
                            out.value("CONTROL_AE_MODE_OFF");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON:
                            out.value("CONTROL_AE_MODE_ON");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH:
                            out.value("CONTROL_AE_MODE_ON_ALWAYS_FLASH");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH:
                            out.value("CONTROL_AE_MODE_ON_AUTO_FLASH");
                            break;
                        case CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE:
                            out.value("CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE");
                            break;
                        default:
                            out.value(flashMode);
                    }
                }
                out.endArray();

                SizeF physicalSize = camera.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                out.name("physical_size").beginObject().name("width").value(physicalSize.getWidth()).name("height")
                        .value(physicalSize.getHeight()).endObject();

                out.name("capabilities").beginArray();
                for (int capability : camera.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)) {
                    switch (capability) {
                        case CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR:
                            out.value("manual_sensor");
                            break;
                        case CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING:
                            out.value("manual_post_processing");
                            break;
                        case CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE:
                            out.value("backward_compatible");
                            break;
                        case CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW:
                            out.value("raw");
                            break;
                        default:
                            out.value(capability);
                    }
                }
                out.endArray();

                out.endObject();
            }
            out.endArray();
            out.endObject();


            return out.toString();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            String command = intent.getAction();
            Context context = getApplicationContext();

            Timber.d("CommandCamera intent with command:%s", command);

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


                    String storageFilename = File.separator
                            + "IMG_" + System.currentTimeMillis()   //IMG_214515184113123.png
                            + (imageformat == CameraImageFormat.FORMAT_JPEG ? ".jpeg" : ".png");
                    File storageFile;
                    File storageFileFull;

                    if (storegallery) {
                        File storageDir = new File(IMAGESTOREDIR);
                        if (!storageDir.exists()) {
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

                    new Handler().postDelayed(() -> {
                        Timber.d("CommandCamera.onStartCommand: Taking picture!");
                        takePicture();
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
            Timber.d("Renamed file: isMoved=" + isMoved + ",moveFileName=" + moveFileName);
*/

            Timber.d("Adding to media scanner...");
            if (storegallery) {
                addToGallery(getApplicationContext(), imageFile);
            }
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
            stopSelf();
        }

        @Override
        public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
            Timber.i("CommandCamera.onCameraError errorCode: %s", errorCode);
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

        /**
         * Simple POJO to store result of executing a Camera command
         */
        static class CameraCommandResult {
            public String message = "";
            public String error;
        }

    }
}
