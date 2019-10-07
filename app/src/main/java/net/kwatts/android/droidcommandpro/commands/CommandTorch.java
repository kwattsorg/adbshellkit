package net.kwatts.android.droidcommandpro.commands;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraManager.TorchCallback;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.JsonWriter;
import android.widget.Toast;
import android.util.Size;
import android.util.SizeF;
import java.io.StringWriter;

import net.kwatts.android.droidcommandpro.ApiReceiver;


import org.json.JSONObject;

import java.io.IOException;


import timber.log.Timber;

public class CommandTorch {
    private static Camera legacyCamera;

    public static String cmd = "torch";
    public static String descr = "Turns the camera flash on/off by default";
    public static String args = "--ez camerainfo [true|false] --ez enabled [true|false]";
    public static String[] permissions = { "Manifest.permission.CAMERA" };

    public static boolean flashState;
    public static boolean camerainfo;

    @TargetApi(Build.VERSION_CODES.M)
    public static void onReceive(ApiReceiver apiReceiver, final Context context, final Intent intent) {


        if (intent.hasExtra("camerainfo")) {
            camerainfo = intent.getBooleanExtra("camerainfo", false);
        } else {
            camerainfo = false;

        }
        // if enabled isn't sent just turn on/off, still need to update to work w/ pre-marshmallow
        if (!intent.hasExtra("enabled")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraManager.TorchCallback torchCallback = new TorchCallback() {
                    @Override
                    public void onTorchModeUnavailable(String cameraId) {
                        super.onTorchModeUnavailable(cameraId);
                    }

                    @Override
                    public void onTorchModeChanged(String cameraId, boolean enabled) {
                        super.onTorchModeChanged(cameraId, enabled);
                        flashState = enabled;
                    }
                };
                CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                manager.registerTorchCallback(torchCallback, null);// (callback, handler), runs immediate
                if (flashState) {
                    toggleTorch(context, false);
                } else {
                    toggleTorch(context, true);
                }


            } else {
                // use legacy api for pre-marshmallow
                legacyToggleTorch(true);
            }

        } else {

            boolean enabled = intent.getBooleanExtra("enabled", false);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                toggleTorch(context, enabled);
            } else {
                // use legacy api for pre-marshmallow
                legacyToggleTorch(enabled);
            }
        }

        ResultReturner.returnData(context, intent, new ResultReturner.ResultJsonWriter() {
            @Override
            public void writeJson(JsonWriter out) throws Exception {
                out.beginObject();
                out.name("flashState").value(flashState);
                if (camerainfo) {
                    try {
                        out.name("camerainfo").value(getCameraInfo(context));
                    } catch (Exception e) {
                        Timber.e(e);
                        out.name("camerainfo").value(e.getMessage());
                    }
                }
                out.endObject();
                out.close();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void toggleTorch(Context context, boolean enabled) {
        try {
            final CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String torchCameraId = getTorchCameraId(cameraManager);

            if (torchCameraId != null) {
                cameraManager.setTorchMode(torchCameraId, enabled);
            } else {
                Toast.makeText(context, "Torch unavailable on your device", Toast.LENGTH_LONG).show();
            }
        } catch (CameraAccessException e) {
            Timber.e("Error toggling torch");
        }
    }

    public static void legacyToggleTorch(boolean enabled) {
        Timber.i("Using legacy camera api to toggle torch");

        if (legacyCamera == null) {
            legacyCamera = Camera.open();
        }

        Camera.Parameters params = legacyCamera.getParameters();

        if (enabled) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            legacyCamera.setParameters(params);
            legacyCamera.startPreview();
        } else {
            legacyCamera.stopPreview();
            legacyCamera.release();
            legacyCamera = null;
        }
    }


    public static String getTorchCameraId(CameraManager cameraManager) throws CameraAccessException {
        String[] cameraIdList =  cameraManager.getCameraIdList();
        String result = null;

        for (String id : cameraIdList) {
            if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                result = id;
                break;
            }
        }
        return result;
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

}
