package net.kwatts.android.droidcommandpro.commands;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDEmbedded;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDRegistration;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.RegisterListener;

import net.kwatts.android.droidcommandpro.ApiReceiver;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

// https://github.com/andriydruk/RxDNSSD
public class CommandDiscovery {
    public static String cmd = "discovery";
    public static String descr = "Discovers local components on the network";
    public static String args = "";
    public static String[] permissions = { "Manifest.permission.INTERNET" };

    public static void onReceive(final Context context, final Intent intent) {
        Intent discoveryService = new Intent(context, DiscoveryService.class);
        discoveryService.setAction(intent.getAction());
        discoveryService.putExtras(intent.getExtras());
        context.startService(discoveryService);
    }


    public static class DiscoveryService extends Service {

        // default scanning time in seconds
        protected static final int DEFAULT_SCANNING_LIMIT = (1000 * 60 * 30);
        protected static boolean isScanning;

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        public int onStartCommand(Intent intent, int flags, int startId) {
            // get command handler and display result
            String command = intent.getAction();
            Context context = getApplicationContext();
            DiscoveryCommandHandler handler = getDiscoveryCommandHandler(command);
            //RecorderCommandResult result = handler.handle(context, intent);
            //postRecordCommandResult(context, intent, result);

            return Service.START_NOT_STICKY;
        }

        protected static DiscoveryCommandHandler getDiscoveryCommandHandler(final String command) {
            return scanHandler;
            /*
            switch (command == null ? "" : command) {
                case "info":
                    //return infoHandler;
                case "scan":

                case "quit":
                    // return quitHandler;
                default:
                    // ?
            } */
        }

        static DiscoveryCommandHandler scanHandler = new DiscoveryCommandHandler() {
            @Override
            public DiscoveryCommandResult handle(Context context, Intent intent) {
                DiscoveryCommandResult result = new DiscoveryCommandResult();
                result.message = "blah";
                if (!isScanning)
                    context.stopService(intent);
                    return result;
                }

                //int duration = intent.getIntExtra("limit", DEFAULT_SCANNING_LIMIT);
                // allow the duration limit to be disabled with zero or negative
                //if (duration > 0 && duration < MIN_RECORDING_LIMIT)
                 //   duration = MIN_RECORDING_LIMIT;
            };
    }




    /**
     * Interface for handling discovery commands
     */
    interface DiscoveryCommandHandler {
        DiscoveryCommandResult handle(final Context context, final Intent intent);
    }

    /**
     * Simple POJO to store result of executing a Discovery command
     */
    static class DiscoveryCommandResult {
        public String message = "";
        public String error;
    }
}


    /*
    public static JSONObject run(android.content.Context ctx) {

        JSONObject res = new JSONObject();

        DNSSD dnssd = new DNSSDEmbedded(ctx);
        try {
            DNSSDService registerService = dnssd.register("service_name", "_rxdnssd._tcp", 123,
                    new RegisterListener() {

                        @Override
                        public void serviceRegistered(DNSSDRegistration registration, int flags,
                                                      String serviceName, String regType, String domain) {
                            Log.i("TAG", "Register successfully ");
                        }

                        @Override
                        public void operationFailed(DNSSDService service, int errorCode) {
                            Log.e("TAG", "error " + errorCode);
                        }
                    });
        } catch (DNSSDException e) {
            Log.e("TAG", "error", e);
        }

        try {
            DNSSDService browseService = dnssd.browse("_rxdnssd._tcp", new BrowseListener() {

                @Override
                public void serviceFound(DNSSDService browser, int flags, int ifIndex,
                                         final String serviceName, String regType, String domain) {
                    Log.i("TAG", "Found " + serviceName);
                }

                @Override
                public void serviceLost(DNSSDService browser, int flags, int ifIndex,
                                        String serviceName, String regType, String domain) {
                    Log.i("TAG", "Lost " + serviceName);
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.e("TAG", "error: " + errorCode);
                }
            });
        } catch (DNSSDException e) {
            Log.e("TAG", "error", e);
        }

        return res;

     */




