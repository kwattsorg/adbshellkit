package net.kwatts.android.droidcommandpro;

/**
 * Created by kwatts on 2/22/18.
 */

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.topjohnwu.superuser.BusyBox;
import com.topjohnwu.superuser.Shell;
/**
 * This is a subclass of {@link Application} used to provide shared objects for this app.
 */
public class App extends Shell.ContainerApp  {
    private static final String TAG = "Application";
    public static App INSTANCE = null;

    public App() {
        INSTANCE = this;
    }

    private static boolean isopen = true;


    @Override
    public synchronized void onCreate() {
        super.onCreate();
        Shell.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.verboseLogging(BuildConfig.DEBUG);
        // Use internal busybox
        BusyBox.setup(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.d(TAG, "onCreate");
        this.isopen = true;

    }

}
