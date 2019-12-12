package net.kwatts.android.droidcommandpro.commands;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.util.SparseIntArray;

import net.kwatts.android.droidcommandpro.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED;

/* to use the service approach (steps at https://gist.github.com/tniessen/ea3d68e7d572ed7c607b81d715798800)
1. find the service first, in this case "audio"
$ service list | grep audio
55	audio: [android.media.IAudioService]

2. find the class source, using the dropdown branch for specific android release:
https://github.com/aosp-mirror/platform_frameworks_base/blob/pie-release/media/java/android/media/IAudioService.aidl

3. count the method you want to call, starting with 1 (not 0!)
    void setMasterMute(boolean mute, int flags, String callingPackage, int user
it's the 11th method, to call boolean mute use i32 1 (true)
$ service call audio 11 i32 1 i32 0
then verify
    boolean isMasterMute();
$ service call audio 10
Result: Parcel(00000000 00000001   '........')
00000001 = true

--
4

$ cd /sys/kernel/debug/tracing
$ echo > set_event  # clear all unrelated events
$ echo 1 > events/binder/enable
$ echo 1 > tracing_on

# .. do your test jobs ..

$ cat trace

ALSO per https://source.android.com/devices/tech/debug/ftrace

$ cat /sys/kernel/debug/tracing/available_tracers
if it returns values, means dynamic tracing is available!

AND per https://www.kernel.org/doc/Documentation/trace/events.txt
$ cat /sys/kernel/debug/tracing/available_events
... returns list
$ echo raw_syscalls:sys_enter >> /sys/kernel/debug/tracing/set_event
OR
$ echo *:* > /sys/kernel/debug/tracing/set_event

And you can use the systrace helper,
python ~/Library/Android/sdk/platform-tools/systrace/systrace.py --list-categories

python ~/Library/Android/sdk/platform-tools/systrace/systrace.py -o mynewtrace.html sched am audio network

https://github.com/DespairFactor/marlin/blob/f238d87879a2121f6936a1d831b6cc8783f03b69/kernel/trace/trace.c

to read as user, you need to setenforce 0 - 'ls -Z ./trace' shows "u:object_r:debugfs_tracing:s0 trace"
 */

public class CommandMicrophoneRecorder {

    public static String cmd = "cmd_microphone_recorder";
    public static String[] permissions = {android.Manifest.permission.READ_CONTACTS};

    /**
     * Starts our MicRecorder service
     */
    public static void onReceive(final Context context, final Intent intent) {
        Intent recorderService = new Intent(context, MicRecorderService.class);
        recorderService.setAction(intent.getAction());
        recorderService.putExtras(intent.getExtras());
        context.startService(recorderService);
    }

    /**
     * Converts time in seconds to a formatted time string: HH:MM:SS
     * Hours will not be included if it is 0
     */
    public static String getTimeString(int totalSeconds) {
        int hours = (totalSeconds / 3600);
        int mins = (totalSeconds % 3600) / 60;
        int secs = (totalSeconds % 60);

        String result = "";

        // only show hours if we have them
        if (hours > 0) {
            result += String.format("%02d:", hours);
        }
        result += String.format("%02d:%02d", mins, secs);
        return result;
    }

    /**
     * Interface for handling recorder commands
     */
    interface RecorderCommandHandler {
        RecorderCommandResult handle(final Context context, final Intent intent);
    }

    /**
     * All recording functionality exists in this background service
     */
    public static class MicRecorderService extends Service implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
        protected static final int MIN_RECORDING_LIMIT = 1000;

        // default max recording duration in seconds
        protected static final int DEFAULT_RECORDING_LIMIT = (1000 * 60 * 15);

        protected static MediaRecorder mediaRecorder;

        // are we currently recording using the microphone?
        protected static boolean isRecording;

        // file we're recording too
        protected static File file;
        /**
         * -----
         * Recorder Command Handlers
         * -----
         */

        static RecorderCommandHandler infoHandler = new RecorderCommandHandler() {
            @Override
            public RecorderCommandResult handle(Context context, Intent intent) {
                RecorderCommandResult result = new RecorderCommandResult();
                result.message = getRecordingInfoJSONString();
                if (!isRecording)
                    context.stopService(intent);
                return result;
            }
        };
        static RecorderCommandHandler recordHandler = new RecorderCommandHandler() {
            @Override
            public RecorderCommandResult handle(Context context, Intent intent) {
                RecorderCommandResult result = new RecorderCommandResult();

                int duration = intent.getIntExtra("limit", DEFAULT_RECORDING_LIMIT);
                // allow the duration limit to be disabled with zero or negative
                if (duration > 0 && duration < MIN_RECORDING_LIMIT)
                    duration = MIN_RECORDING_LIMIT;

                String sencoder = intent.hasExtra("encoder") ? intent.getStringExtra("encoder") : "";
                ArrayMap<String, Integer> encoder_map = new ArrayMap<>(3);
                encoder_map.put("aac", MediaRecorder.AudioEncoder.AAC);
                encoder_map.put("amr_nb", MediaRecorder.AudioEncoder.AMR_NB);
                encoder_map.put("amr_wb", MediaRecorder.AudioEncoder.AMR_WB);

                Integer encoder = encoder_map.get(sencoder.toLowerCase());
                if (encoder == null)
                    encoder = MediaRecorder.AudioEncoder.AAC;

                int format = intent.getIntExtra("format", MediaRecorder.OutputFormat.DEFAULT);
                if (format == MediaRecorder.OutputFormat.DEFAULT) {
                    SparseIntArray format_map = new SparseIntArray(3);
                    format_map.put(MediaRecorder.AudioEncoder.AAC,
                            MediaRecorder.OutputFormat.MPEG_4);
                    format_map.put(MediaRecorder.AudioEncoder.AMR_NB,
                            MediaRecorder.OutputFormat.THREE_GPP);
                    format_map.put(MediaRecorder.AudioEncoder.AMR_WB,
                            MediaRecorder.OutputFormat.THREE_GPP);
                    format = format_map.get(encoder, MediaRecorder.OutputFormat.DEFAULT);
                }

                SparseArray<String> extension_map = new SparseArray<>(2);
                extension_map.put(MediaRecorder.OutputFormat.MPEG_4, ".m4a");
                extension_map.put(MediaRecorder.OutputFormat.THREE_GPP, ".3gp");
                String extension = extension_map.get(format);

                String filename = intent.hasExtra("file") ? intent.getStringExtra("file") : getDefaultRecordingFilename() + (extension != null ? extension : "");

                int source = intent.getIntExtra("source", MediaRecorder.AudioSource.MIC);

                int bitrate = intent.getIntExtra("bitrate", 0);
                int srate = intent.getIntExtra("srate", 0);
                int channels = intent.getIntExtra("channels", 0);

                file = new File(filename);

                Timber.i("MediaRecording file is: %s", file.getAbsolutePath());

                if (file.exists()) {
                    result.error = String.format("File: %s already exists! Please specify a different filename", file.getName());
                } else {
                    if (isRecording) {
                        result.error = "Recording already in progress!";
                    } else {
                        try {
                            mediaRecorder.setAudioSource(source);
                            mediaRecorder.setOutputFormat(format);
                            mediaRecorder.setAudioEncoder(encoder);
                            mediaRecorder.setOutputFile(filename);
                            mediaRecorder.setMaxDuration(duration);
                            if (bitrate > 0)
                                mediaRecorder.setAudioEncodingBitRate(bitrate);
                            if (srate > 0)
                                mediaRecorder.setAudioSamplingRate(srate);
                            if (channels > 0)
                                mediaRecorder.setAudioChannels(channels);
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                            isRecording = true;
                            result.message = String.format("Recording started: %s \nMax Duration: %s\n" +
                                            "Audio source: %d\n" + "Audio encoder: %d",
                                    file.getAbsolutePath(),
                                    duration <= 0 ?
                                            "unlimited" :
                                            getTimeString(duration / 1000),
                                    source, encoder);

                        } catch (IllegalStateException | IOException e) {
                            Timber.e("MediaRecorder error");
                            result.error = "Recording error: " + e.getMessage();
                        }
                    }
                }
                if (!isRecording)
                    context.stopService(intent);
                return result;
            }
        };
        static RecorderCommandHandler quitHandler = new RecorderCommandHandler() {
            @Override
            public RecorderCommandResult handle(Context context, Intent intent) {
                RecorderCommandResult result = new RecorderCommandResult();

                if (isRecording) {
                    result.message = "Recording finished: " + file.getAbsolutePath();
                } else {
                    result.message = "No recording to stop";
                }
                context.stopService(intent);
                return result;
            }
        };

        protected static RecorderCommandHandler getRecorderCommandHandler(final String command) {
            switch (command == null ? "" : command) {
                case "info":
                    return infoHandler;
                case "record":
                    return recordHandler;
                case "quit":
                    return quitHandler;
                default:
                    return (context, intent) -> {
                        RecorderCommandResult result = new RecorderCommandResult();
                        result.error = "Unknown command: " + command;
                        if (!isRecording)
                            context.stopService(intent);
                        return result;
                    };
            }
        }

        protected static void postRecordCommandResult(final Context context, final Intent intent,
                                                      final RecorderCommandResult result) {

            ResultReturner.returnData(context, intent, out -> {
                out.append(result.message).append("\n");
                if (result.error != null) {
                    out.append(result.error).append("\n");
                }
                out.flush();
                out.close();
            });
        }

        /**
         * Returns our MediaPlayer instance and ensures it has all the necessary callbacks
         */
        protected static void getMediaRecorder(MicRecorderService service) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setOnErrorListener(service);
            mediaRecorder.setOnInfoListener(service);
        }

        /**
         * Releases MediaRecorder resources
         */
        protected static void cleanupMediaRecorder() {
            if (isRecording) {
                mediaRecorder.stop();
                isRecording = false;
            }
            mediaRecorder.reset();
            mediaRecorder.release();
        }

        protected static String getDefaultRecordingFilename() {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            Date date = new Date();
            return App.FILES_PATH + "/AudioRecording_" + dateFormat.format(date);
            //return Environment.getExternalStorageDirectory().getAbsolutePath() + "/TermuxAudioRecording_" + dateFormat.format(date);
        }

        protected static String getRecordingInfoJSONString() {
            String result = "";
            JSONObject info = new JSONObject();
            try {
                info.put("isRecording", isRecording);
                if (isRecording) {
                    info.put("outputFile", file.getAbsolutePath());
                } else {
                    info.put("getaudiosourcemax", MediaRecorder.getAudioSourceMax());
                    //How to check microphones and apps using it
                    // - "lsof | grep msm_pcm_in" or "lsof | grep pcm" then to find the file "lsof | grep ^media | grep data"
                    // - dumpsys media.player
                    // - dump Settings > Apps > Gear symbol (or menu button depending on phone) > App Permissions > Microphone.
                    //   + pull a list of apps with microphone permission then see which are running
                    //SDK 28 ONLY: List<MicrophoneInfo> microphoneInfoList = mediaRecorder.getActiveMicrophones()
                }
                result = info.toString(2);
            } catch (JSONException e) {
                Timber.e(e);
            }
            return result;
        }

        public void onCreate() {
            getMediaRecorder(this);
        }

        public int onStartCommand(Intent intent, int flags, int startId) {
            // get command handler and display result
            String command = intent.getAction();
            Context context = getApplicationContext();
            RecorderCommandHandler handler = getRecorderCommandHandler(command);
            RecorderCommandResult result = handler.handle(context, intent);
            postRecordCommandResult(context, intent, result);

            return Service.START_NOT_STICKY;
        }

        public void onDestroy() {
            cleanupMediaRecorder();
            Timber.i("MicRecorderAPI MicRecorderService onDestroy()");
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            isRecording = false;
            this.stopSelf();
            Timber.e("MicRecorderService onError() %s", what);
        }

        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            switch (what) {
                case MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED: // intentional fallthrough
                case MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    this.stopSelf();
            }
            Timber.i("MicRecorderService onInfo() %s", what);
        }
    }

    /**
     * Simple POJO to store result of executing a Recorder command
     */
    static class RecorderCommandResult {
        public String message = "";
        public String error;
    }
}
