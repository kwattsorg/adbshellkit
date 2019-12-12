package net.kwatts.android.droidcommandpro;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.TabStopSpan;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Splitter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import net.kwatts.android.droidcommandpro.data.Command;
import net.kwatts.android.droidcommandpro.data.GoogleUser;
import net.kwatts.android.droidcommandpro.data.User;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;


//TODO:
// Package support - https://github.com/termux/termux-packages
// notasecret
// https://greenify.uservoice.com/knowledgebase/articles/749142-how-to-grant-permissions-required-by-some-features
// adb -d shell pm grant net.kwatts.android.droidcommandpro android.permission.DUMP
// adb -d shell pm grant net.kwatts.android.droidcommandpro android.permission.WRITE_SECURE_SETTINGS


//TODO: FIX
// CANNOT LINK EXECUTABLE "./aapt": "/data/data/net.kwatts.android.droidcommandpro/files/lib.aarch64/libc++_shared.so" is 32-bit instead of 64-bit
public class MainActivity extends AppCompatActivity implements OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String CHANNEL_ID = "main";
    public static final int RC_SIGN_IN = 10;
    // app id: ca-app-pub-2189980367471582~1443964910
    // ad banner unit id: ca-app-pub-2189980367471582/2916172142
    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.
    public static final int COMMAND_PERMISSION = 20; // code you want.
    // For intents
    static final String EXTRA_COMMAND = "net.kwatts.android.droidcommandpro.EXTRA_COMMAND";
    static final String EXTRA_COMMAND_KEY = "net.kwatts.android.droidcommandpro.EXTRA_COMMAND_KEY";
    private static final int MSG_NEWLINE = 1;
    private static final int MSG_CMD_TERMINATED = 2;
    private static final int MSG_CMD_COMPLETE = 3;
    public static SharedPreferences mSharedPref;
    public static List<Command> mCommandQueue = new LinkedList<>();
    public static TextView mTextViewState;
    public static EditText mTopCommandView;
    public static StringBuffer mTopOutString = new StringBuffer();
    public static StringBuffer mTopOutStringError = new StringBuffer();
    public static ScrollView mScrollView;
    public static LinearLayout mLines;
    public static int mTextSize = 23;
    public static List<Command> mGlobalCommands = new ArrayList<>();
    public static List<Command> mUserCommands = new ArrayList<>();
    public static FirebaseAnalytics mFirebaseAnalytics;
    public static Map<String, String> mUserMapVars = new HashMap<>();
    public static ToggleButton mToggleButtonVariables;
    public static NotificationCompat.Builder mNotificationBuilder;
    public static NotificationManagerCompat mNotificationManager;
    public static Command lastPublicCommandUsed;
    public static boolean lastPermissionStatus;
    public static boolean isAdminUserClaim = false;
    static long startTime;
    static int mExecState = 0;
    public ProgressDialog mProgressDialog;
    public Button mRunButton;
    public WebView mWebView;
    public FirebaseDatabase mFirebaseDB;
    public FirebaseUser mFirebaseUser;
    public FirebaseStorage mFirebaseStorage;
    public Chronometer mChronometer;
    public Button mButtonFileSelectedVariables;
    public EditText mDialogFileSelectedVars;
    public String[] permissionsList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            //,Manifest.permission.READ_CONTACTS
    };
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEWLINE:
                    handleMessageNewline(msg);
                    break;
                case MSG_CMD_TERMINATED:
                    //setTextState("Command terminated after " + msg.arg2 + " lines",(String) msg.obj);
                    break;
                //handleMessageNewline(msg);
                //break;
                case MSG_CMD_COMPLETE:
                    //setTextState("Command finished after " + msg.arg2 + " lines",(String) msg.obj);
                    break;
                //handleMessageNewline(msg);
                default:
                    super.handleMessage(msg);
            }
        }
    };
    public LinearLayout addCommandLinearLayoutAdmin;
    public CheckBox tagAdminIsPublicCheckBox;
    public CheckBox tagAdminOnboardingCheckBox;
    //public View updateAction;
    //public View createNewAction;
    public Spinner spinnerAddPermission;
    //com.topjohnwu.superuser.Shell mShell;
    public Spinner spinnerRemovePermission;
    MenuItem shareMenuItem;
    MenuItem addCommandMenuItem;
    MenuItem changeCommandMenuItem;
    MenuItem removeCommandMenuItem;
    Boolean isAdmin = Boolean.FALSE;
    TextView mTextStatus;
    Spinner mSpinnerCommands;
    Spinner mPackagesSpinner;
    Spinner mNetworkInterfaceSpinner;
    EditText mDialogEditUserVars;
    List<String> mAppPackagesList;
    List<NetworkInterface> mNetworkInterfaceList;
    View mAdmobAds;
    AdView mAdView;
    String mFullCommand = "";
    boolean mRunAsSuperUser = true;
    boolean mDisableAds = false;
    GoogleSignInClient mGoogleSignInClient;
    GoogleUser mGoogleUser;
    CustomAdapterCommands mCustomCmdsAdapter;
    CustomAdapterVars mCustomVarsAdapter;
    CustomAdapterNetworkInterfaceVars mCustomVarsNetworkInterfaceAdapter;
    int mApplicationId;
    com.topjohnwu.superuser.Shell.Job mJob;
    com.topjohnwu.superuser.Shell mShell;
    String mCurrentUserVars;
    private ShareActionProvider shareActionProvider;
    private ImageButton mGoogleUserSignedInImageButton;
    private FirebaseAuth mAuth;
    private EditText dialogEditDescription;
    private EditText dialogEditCommand;
    private CheckBox tagSuperUserCheckBox;
    private CheckBox tagPinnedCheckBox;

    public static boolean isUserAdmin() {
        return isAdminUserClaim;
    }

    private static void writeCommand(Command c) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String key;
        String uid;

        if (c.isPublic) {
            uid = "global";
        } else {
            uid = c.getUid();
        }

        if (c.key != null) {
            key = c.key; //already exists
        } else {
            key = db.child("commands_v2").push().getKey();

        }

        Map<String, Object> cmdValues = c.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/commands_v2/" + uid + "/" + key, cmdValues);
        db.updateChildren(childUpdates);

    }

    private static void removeCommand(Command c) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String uid;

        // only remove user commands unless admin
        if (c.key != null) {
            if (c.isPublic) {
                if (isUserAdmin()) {
                    Toast.makeText(App.INSTANCE.getApplicationContext(), "An admin and your removing a global command. Boo.", Toast.LENGTH_SHORT).show();
                    db.child("/commands_v2/" + "global" + "/" + c.key).removeValue();
                } else {
                    Toast.makeText(App.INSTANCE.getApplicationContext(), "No can do, this command is public and read only.", Toast.LENGTH_SHORT).show();
                }
            } else {
                uid = c.getUid();
                db.child("/commands_v2/" + uid + "/" + c.key).removeValue();
            }

        }
    }

    private static void logEvent(String id, String command, String status) {
        Bundle params = new Bundle();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            params.putString("user", firebaseUser.getEmail());
        } else {
            params.putString("user", "anonymous");
        }
        params.putString("command", command);
        params.putString("status", status);
        mFirebaseAnalytics.logEvent(id, params);
    }

    private static void setTextState(String title, String status, String output) {
        try {
            if (mSharedPref.getBoolean("showNotifications", false)) {
                mNotificationBuilder.setSubText(title);
                mNotificationBuilder.setContentText(status + output);
                mNotificationBuilder.setContentTitle(mTopCommandView.getText().toString());
                mNotificationManager.notify(1, mNotificationBuilder.build());
            }
            SpannableString spanString = new SpannableString(title + status);
            spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
            mTextViewState.setText(spanString);
        } catch (Exception e) {
            Timber.e("Unable to set status with setTextState!");
        }

        // send analytics
    }

    private void handleMessageNewline(Message msg) {
        int cmd_state = msg.arg1;
        String line = (String) msg.obj;

        final boolean autoscroll = mScrollView.getScrollY() + mScrollView.getHeight() >= mLines.getBottom();
        TextView lineView = new TextView(App.INSTANCE.getApplicationContext());

        lineView.setTextIsSelectable(true);


        ColorStateList displayTextColor;
        if (cmd_state < 0) {
            displayTextColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.RED});
        } else {
            displayTextColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.GREEN});
        }


        //TODO: Custom span to handle ANSI color codes, fix to make default green instead of black
        //android.text.Spannable parsedLine = new AnsiParser().parse(line);
        SpannableString spanString = new SpannableString(line);
        TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan("monospace", Typeface.NORMAL, mTextSize, displayTextColor, null);
        spanString.setSpan(textAppearanceSpan, 0, spanString.length(), 0);
        // Display "\t", offset by 100 pixels
        spanString.setSpan(new TabStopSpan.Standard(100), 0, spanString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        lineView.setText(spanString);


        mLines.addView(lineView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        // Really shouldn't support more then 5000 lines, wiping...
        if (mLines.getChildCount() > 5000)
            mLines.removeViewAt(0);

        mScrollView.post(() -> {
            if (autoscroll) {
                mScrollView.scrollTo(0, mLines.getBottom() - mScrollView.getHeight());

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, "ca-app-pub-2189980367471582~1443964910");

        /* To get trace times:
            // $ adb shell setprop log.tag.droidcommander VERBOSE
            isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
            TimingLogger timingLogger = new TimingLogger("droidcommander", "onCreate");
        */


        // Support for running commands from intents:
        // 'am start -n "net.kwatts.android.droidcommandpro/net.kwatts.android.droidcommandpro.MainActivity"
        //          -a "android.intent.action.MAIN" --es net.kwatts.android.droidcommandpro.EXTRA_COMMAND id'

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mSharedPref.registerOnSharedPreferenceChangeListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("476590821360-kp6v3tum1rjofutcea5fjulu1an14ivk.apps.googleusercontent.com")
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance(); // gs://adb-shell.appspot.com
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new GsonJsonProvider();
            private final MappingProvider mappingProvider = new GsonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });


        try {
            setContentView(R.layout.main);


            LinearLayout topLinearLayout = findViewById(R.id.topLinearLayout);
            mTextStatus = findViewById(R.id.textViewStatus);
            mTextViewState = findViewById(R.id.textViewState);
            mAdmobAds = findViewById(R.id.adMobView);
            mSpinnerCommands = findViewById(R.id.spinnerGlobalCommands);
            mTopCommandView = findViewById(R.id.topCommandView);
            mScrollView = findViewById(R.id.topOutputView);
            mWebView = findViewById(R.id.webview);
            mLines = findViewById(R.id.lines);
            if (mSharedPref.getBoolean("disableAds", false)) {
                topLinearLayout.removeView(mAdmobAds);
            } else {
                mAdView = findViewById(R.id.adMobView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

            mGoogleUserSignedInImageButton = findViewById(R.id.signed_in_image_button);
            SignInButton mGoogleUserSignInButton = findViewById(R.id.sign_in_button);

            AdapterView.OnItemSelectedListener myListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getId() == R.id.spinnerGlobalCommands) {
                        Command c = mCustomCmdsAdapter.getItem(position);
                        if (c != null) {
                            mTopCommandView.setText(c.getCommand());
                            //hide keyboard
                            InputMethodManager imm = (InputMethodManager) App.INSTANCE.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mTopCommandView.getWindowToken(), 0);
                            //mTopCommandView.setSelection(c.getCommand().length());
                        }
                    }
                    invalidateOptionsMenu();
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            };
            mSpinnerCommands.setOnItemSelectedListener(myListener);
            mCustomCmdsAdapter = new CustomAdapterCommands(MainActivity.this, new ArrayList<>());
            mSpinnerCommands.setAdapter(mCustomCmdsAdapter);

            mRunButton = findViewById(R.id.runButton);
            mRunButton.setOnClickListener(this);

            mChronometer = findViewById(R.id.chronometer);


            mGoogleUserSignedInImageButton.setOnClickListener(v -> {
                if (mFirebaseUser != null) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
            mGoogleUserSignInButton.setOnClickListener(v -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });

            // Notifications
            createNotificationChannel();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("Status")
                    .setContentText("ADB Toolkit is ready for commands...")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false);
            mNotificationManager = NotificationManagerCompat.from(this);


            loadVariables();


            mFirebaseUser = mAuth.getCurrentUser();

            if (mFirebaseUser != null) {
                if (mFirebaseUser.isAnonymous()) {
                    refreshUserUI(false);
                } else {
                    refreshUserUI(true);
                }

            } else {
                refreshUserUI(false);
            }

            try {
                String extra_cmd_key = getIntent().getStringExtra(EXTRA_COMMAND_KEY);
                if (extra_cmd_key != null) {
                    Timber.d("Intent.getStringExtra(" + EXTRA_COMMAND_KEY + "):" + extra_cmd_key);
                    Command c = getUserCommandByKey(extra_cmd_key);
                    if (c != null) {
                        mCommandQueue.add(c);
                        Timber.d("Added command " + c.key + " to queue from intent");
                    } else {
                        Timber.d("Command from intent not found!");
                        Toast.makeText(getApplicationContext(), "Pinned command not found! command key requested:" + extra_cmd_key, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }

        } catch (Exception e) {
            Timber.e(e, "MainActivity onCreate() failed:%s", e.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Just support one for now
        if (mCommandQueue.size() > 0) {
            Command c = mCommandQueue.get(0);
            if (c != null) {
                runCommand(c);
            }
            mCommandQueue.clear();
        }
    }

    public void refreshUserUI(boolean isLoggedIn) {


        invalidateOptionsMenu();

        if (isLoggedIn) {
            setTextUserStatus(mFirebaseUser.getEmail() + " logged in");
            if (isUserAdmin()) {
                setTextUserStatus(mFirebaseUser.getEmail() + " as admin");
            }
            Glide.with(getApplicationContext()).load(mFirebaseUser.getPhotoUrl().toString())
                    .apply(new RequestOptions().circleCrop()).into(mGoogleUserSignedInImageButton);
            initFirebase();
        } else {
            setTextUserStatus("Showing public commands only, please login to access and save your own commands.");
            Glide.with(getApplicationContext()).load(R.drawable.ic_account_circle)
                    .apply(new RequestOptions().circleCrop()).into(mGoogleUserSignedInImageButton);
            initFirebase();
        }

        setTextState("Select a command to run...", "", "");

    }

    public void onClick(View v) {
        try {
            //TODO: Check permissions needed by command. Right now just request
            Command c = mCustomCmdsAdapter.getItem(mSpinnerCommands.getSelectedItemPosition());

            // Drrty, setting this here in case command is changed.
            String ic = mTopCommandView.getText().toString();
            if (!ic.equals(c.getCommand())) {
                Command tc = new Command();
                tc.setCommand(ic);
                runCommand(tc);
            } else {
                runCommand(c);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Logging in...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=XYZ123
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Timber.e(e, "signInResult:failed code=%s", e.getStatusCode());
                Toast.makeText(getApplicationContext(), "Unable to sign in with Google Play Services (status_code: " + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Timber.d("firebaseAuthWithGoogle:%s", acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    mProgressDialog.cancel();
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Timber.d("signInWithCredential:success");
                        mFirebaseUser = mAuth.getCurrentUser();
                        refreshUserUI(true);
                    } else {
                        Timber.w("signInWithCredential:failure:%s", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void runCommand(Command c) {

        // Are we already running a job?
        if (mJob != null) {
            Toast.makeText(getApplicationContext(), "The last command was still running and force closed!", Toast.LENGTH_SHORT).show();

            if (mShell != null) {
                try {
                    mShell.close();
                } catch (Exception e) {
                    Timber.e(e);
                }

            }

            //return;
        }

        if (mSharedPref.getBoolean("disableShellSharing", false)) {
            mShell = Shell.newInstance();
        } else {
            mShell = Shell.getShell();
        }


        mJob = mShell.newJob();

        setTextState("Command started as " + ((mShell.getStatus() > 0) ? "superuser" : "normal user" + "..."), "", "");

        //clear window...
        mTopOutString.setLength(0);
        mTopOutStringError.setLength(0);
        mLines.removeAllViews();

        if (c.isPublic) {
            lastPublicCommandUsed = c;
        } else {
            lastPublicCommandUsed = null;
        }

        //Ask for permissions needed by the command to execute
        checkCommandPermissions(c);
        // set last command for ui selection and add command to run counts
        addToCommandRuncounts(c);


        String coreCommand = c.getCommand();
        String runCommand;

        mTextSize = Integer.parseInt(mSharedPref.getString("textSize", "23"));

        StringBuilder vars = new StringBuilder();
        for (Map.Entry<String, String> entry : mUserMapVars.entrySet()) {
            vars.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }

        if (mToggleButtonVariables != null) {
            if (mToggleButtonVariables.isChecked()) {
                runCommand = vars.toString() + coreCommand;
            } else {
                runCommand = coreCommand;
            }
        } else {
            runCommand = vars.toString() + coreCommand;
        }

        // NOW WE RUN!!!
        Timber.d("Running:%s", runCommand);
        commandTimer(true);

        List<String> consoleList;
        List<String> consoleListError;
        consoleList = new CallbackList<String>() {
            //@MainThread
            @Override
            public void onAddElement(String s) {
                Message msg = mHandler.obtainMessage(MSG_NEWLINE);
                msg.arg1 = 0;
                msg.obj = s;
                mHandler.sendMessage(msg);
                mExecState = 0;
            }
        };
        consoleListError = new CallbackList<String>() {
            @Override
            public void onAddElement(String s) {
                Message msg = mHandler.obtainMessage(MSG_NEWLINE);
                msg.arg1 = -1;
                msg.obj = s;
                mHandler.sendMessage(msg);
                mExecState = -1;
            }
        };

        startTime = SystemClock.elapsedRealtime();

        Shell.ResultCallback runResultCallback = out -> {
            commandTimer(false);
            CharSequence last_line = "";
            if (mLines.getChildCount() > 0) {
                last_line = ((TextView) mLines.getChildAt(mLines.getChildCount() - 1)).getText();
            }
            long ms = SystemClock.elapsedRealtime() - startTime;
            double s = ms / 1000.0;

            String state = "Command finished after " + s + "secs (lines=" + mLines.getChildCount() +
                    ",state=" + ((out.getCode() < 0) ? "fail(" + out.getCode() + ")" : "success") + ")";

            setTextState(state, "", last_line.toString());
            // clear the job...
            mJob = null;

            setShareData(state, runCommand);

            // ... and close the shell after a couple of seconds if sharing is off
            /*
            if (mSharedPref.getBoolean("disableShellSharing",false)) {
                try {
                    mShell.waitAndClose(2, TimeUnit.SECONDS);
                } catch (Exception e)  {
                    Timber.e(e);
                }
            } */

        };

        mJob.add(runCommand);
        mJob.to(consoleList, consoleListError);
        mJob.submit(runResultCallback);

        invalidateOptionsMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        shareMenuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
        if (shareActionProvider == null) {
            shareActionProvider = new ShareActionProvider(this);
            MenuItemCompat.setActionProvider(shareMenuItem, shareActionProvider);
        }
        shareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

        addCommandMenuItem = menu.findItem(R.id.user_add_command);
        changeCommandMenuItem = menu.findItem(R.id.user_change_command);
        removeCommandMenuItem = menu.findItem(R.id.user_remove_command);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();

        shareMenuItem.setVisible(true);

        // User is logged in!
        if ((FirebaseAuth.getInstance().getCurrentUser() != null)) {
            menu.findItem(R.id.action_login).setVisible(false);
            menu.findItem(R.id.action_logout).setVisible(true);
            addCommandMenuItem.setVisible(true);

            if (mCustomCmdsAdapter.getCount() > 0) {

                Command c = mCustomCmdsAdapter.getItem(mSpinnerCommands.getSelectedItemPosition());
                if (c != null) {
                    if (c.isPublic && isAdmin == false) {
                        changeCommandMenuItem.setVisible(false);
                        removeCommandMenuItem.setVisible(false);
                    } else {
                        changeCommandMenuItem.setVisible(true);
                        removeCommandMenuItem.setVisible(true);
                    }
                }
            } else {
                changeCommandMenuItem.setVisible(false);
                removeCommandMenuItem.setVisible(false);
            }

        } else {
            menu.findItem(R.id.action_login).setVisible(true);
            menu.findItem(R.id.action_logout).setVisible(false);
            addCommandMenuItem.setVisible(false);
            changeCommandMenuItem.setVisible(false);
            removeCommandMenuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_add_command:
                showCommandView(FirebaseAuth.getInstance().getCurrentUser(), new Command());
                return true;
            case R.id.user_change_command:
                showCommandView(FirebaseAuth.getInstance().getCurrentUser(),
                        mCustomCmdsAdapter.getItem(mSpinnerCommands.getSelectedItemPosition()));
                return true;
            case R.id.user_remove_command:
                showRemoveCommandView();
                return true;
            case R.id.user_variables:
                showVariableView();
                return true;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut();
                mUserCommands.clear();
                //mCustomCmdsAdapter.removeUserCommands();
                refreshUserUI(false);
                return true;
            case R.id.action_login:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                return true;
            case R.id.action_setting:
                Intent i = new Intent(this, SettingsActivity.class);
                MainActivity.this.startActivity(i);
                return true;
            case R.id.action_about:
                com.eggheadgames.aboutbox.activity.AboutActivity.launch(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.i("onSharedPreferenceChanged callback");
        switch (key) {
            case "textSize":
                mTextSize = Integer.parseInt(sharedPreferences.getString("textSize", "23"));
                break;
            case "runAsSuperUser":
                mRunAsSuperUser = sharedPreferences.getBoolean(key, true);
                break;
            case "disableAds":
                mDisableAds = sharedPreferences.getBoolean(key, false);
                break;
            case "variablesEditText":
                String v = mSharedPref.getString(key, "");
                Map<String, String> m = splitVariables(v);
                if (m != null) {
                    mUserMapVars.putAll(m);
                }


            default:
                try {
                    //boolean checkState = sharedPreferences.getBoolean(key, false);
                } catch (Exception e) {
                }

        }

    }

    public void initFirebase() {
        DatabaseReference mFbaseDBCommandsRef = mFirebaseDB.getReference();
        final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mFirebaseUser != null) {
            if (!mFirebaseUser.isAnonymous()) {
                User user = new User();
                user.username = mFirebaseUser.getDisplayName();
                user.email = mFirebaseUser.getEmail();
                user.photo_url = mFirebaseUser.getPhotoUrl().toString();

                FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid()).setValue(user);


                mFirebaseUser.getIdToken(false).addOnSuccessListener(result -> {
                    //Map<String, Object> res_claims = result.getClaims();
                    isAdmin = Boolean.FALSE;
                    try {

                        //Object obj = result.getClaims().getOrDefault("admin", Boolean.FALSE);
                        Map<String, Object> res = result.getClaims();
                        for (Map.Entry<String, Object> entry : res.entrySet()) {
                            String k = entry.getKey();
                            Object v = entry.getValue();

                            if (k.equals("admin")) {
                                if (v instanceof Boolean) {
                                    isAdmin = (Boolean) v;
                                }
                            }
                        }

                        if (isAdmin) {
                            isAdminUserClaim = true;
                            setTextUserStatus(mFirebaseUser.getEmail() + " as admin");
                        } else {
                            isAdminUserClaim = false;
                            setTextUserStatus(mFirebaseUser.getEmail() + " as user");
                        }


                    } catch (Exception e) {
                        isAdminUserClaim = false;
                        setTextUserStatus("Logged in not as user or admin?");
                        Timber.e(e, "Exception checking for user permission claims");
                    }
                });

                mFbaseDBCommandsRef.child("commands_v2").child(mFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUserCommands.clear();
                        removeDynamicShortcuts();

                        for (DataSnapshot cmdSnapshot : dataSnapshot.getChildren()) {
                            Command cmd = cmdSnapshot.getValue(Command.class);
                            if (cmd != null) {
                                cmd.key = cmdSnapshot.getKey();
                                //mUserCommands.add(cmd);

                                if (cmd.isPinned()) {
                                    addDynamicShortcut(cmd.key, cmd.getCommand(), cmd.getDescription());
                                }

                                mUserCommands.add(cmd);

                            }
                        }

                        List<Command> allCommands = new ArrayList<>();
                        allCommands.addAll(mUserCommands);
                        allCommands.addAll(mGlobalCommands);


                        mCustomCmdsAdapter.addAllCommands(allCommands,
                                mSharedPref.getBoolean("hideSuperUserCommands", false));
                        mCustomCmdsAdapter.notifyDataSetChanged();
                        mSpinnerCommands.setSelection(0);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        } else {
            // setTextUserStatus("Logged in as anonymous");
        }
/*
        //BEGIN NEW
        // https://firebase.googleblog.com/2017/12/using-android-architecture-components.html
        // https://firebase.googleblog.com/2017/12/using-android-architecture-components_20.html

        // https://android.jlelse.eu/android-architecture-components-with-firebase-907b7699f6a0

        CommandGlobalModel commandGlobalModel = ViewModelProviders.of(this).get(CommandGlobalModel.class);
        LiveData<DataSnapshot> liveGlobalCommandData = commandGlobalModel.getDataSnapshotLiveData();
        liveGlobalCommandData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mGlobalCommands.clear();
                    for (DataSnapshot cmdSnapshot : dataSnapshot.getChildren()) {
                        Command cmd = cmdSnapshot.getValue(Command.class);
                        if (cmd != null) {
                            cmd.key = cmdSnapshot.getKey();
                            mGlobalCommands.add(cmd);
                        }
                    }
                    List<Command> allCommands = new ArrayList<>();
                    allCommands.addAll(mUserCommands);
                    allCommands.addAll(mGlobalCommands);

                    boolean hideSuperCommands = mSharedPref.getBoolean("hideSuperUserCommands", false);
                    mCustomCmdsAdapter.addAllCommands(allCommands, hideSuperCommands);
                    mCustomCmdsAdapter.notifyDataSetChanged();
                }
            }
        });
*/

        //global commands
        mFbaseDBCommandsRef.child("commands_v2").child("global").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGlobalCommands.clear();

                for (DataSnapshot cmdSnapshot : dataSnapshot.getChildren()) {
                    Command cmd = cmdSnapshot.getValue(Command.class);

                    if (cmd != null) {
                        cmd.key = cmdSnapshot.getKey();
                        mGlobalCommands.add(cmd);
                    }
                }

                List<Command> allCommands = new ArrayList<>();
                allCommands.addAll(mUserCommands);
                allCommands.addAll(mGlobalCommands);
                mCustomCmdsAdapter.addAllCommands(allCommands,
                        mSharedPref.getBoolean("hideSuperUserCommands", false));
                mCustomCmdsAdapter.notifyDataSetChanged();

                onBoarding();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    //Onboarding steps...
    private void onBoarding() {
        for (Command c : mCustomCmdsAdapter.spinnerCmds) {
            if (c != null && c.isOnboarding()) {
                String onboardingPrefName = App.USER_IS_ONBOARD_PREF_NAME + "_" + c.key.toLowerCase();
                if (!mSharedPref.getBoolean(onboardingPrefName, false)) {
                    SharedPreferences.Editor sharedPreferencesEditor = mSharedPref.edit();
                    sharedPreferencesEditor.putBoolean(onboardingPrefName, true);
                    sharedPreferencesEditor.apply();
                    runCommand(c);
                }
            }
        }
    }

    private Command getUserCommandByKey(String key) {
        for (Command c : mUserCommands) {
            if (c.key != null && c.key.equalsIgnoreCase(key)) {
                return c;
            }
        }
        return null;
    }

    public void addToCommandRuncounts(Command c) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        if ((!c.isPublic) && (c.key != null)) {
            c.addToRuncounts();
            c.setLastused(System.currentTimeMillis());
            Map<String, Object> cmdValues = c.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/commands_v2/" + c.getUid() + "/" + c.key, cmdValues);
            db.updateChildren(childUpdates);
        }

    }

    public void addDynamicShortcut(String cmd_key, String cmd, String label) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutManager smgr = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
                Intent di = new Intent(App.INSTANCE.getApplicationContext(), MainActivity.class);
                di.setAction(Intent.ACTION_MAIN);
                di.putExtra(EXTRA_COMMAND, cmd);
                if (cmd_key != null) {
                    di.putExtra(EXTRA_COMMAND_KEY, cmd_key);
                    ShortcutInfo dynamicShortcut = new ShortcutInfo.Builder(App.INSTANCE.getApplicationContext(), cmd_key)
                            .setShortLabel(label)
                            .setLongLabel(label)
                            .setIcon(Icon.createWithResource(App.INSTANCE.getApplicationContext(), R.mipmap.ic_launcher))
                            .setIntent(di)
                            .build();


                    smgr.addDynamicShortcuts(Collections.singletonList(dynamicShortcut));
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Unable to add dynamic shortcut!");
        }
    }

    public void removeDynamicShortcuts() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutManager smgr = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
                smgr.removeAllDynamicShortcuts();
            }
        } catch (Exception e) {
            Timber.e(e, "Unable to remove dynamic shortcut!");
        }
    }

    private void setShareData(String state, String cmd) {
        if (shareActionProvider != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            StringBuilder cmdOut = new StringBuilder();
            cmdOut.append(state).append("\n\n");
            cmdOut.append("=== COMMAND ===" + "\n");
            cmdOut.append(cmd).append("\n");
            cmdOut.append("===============" + "\n\n");

            for (int x = 0; x < mLines.getChildCount(); x++) {
                TextView currentTextView = (TextView) mLines.getChildAt(x);
                cmdOut.append(currentTextView.getText()).append("\r\n");
            }
            share.putExtra(android.content.Intent.EXTRA_SUBJECT, "ADB Shellkit Command Results");
            share.putExtra(Intent.EXTRA_TEXT, cmdOut.toString());

            shareActionProvider.setShareIntent(share);
        }
    }

    private void setTextUserStatus(String status) {
        SpannableString spanString = new SpannableString(status);
        spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
        mTextStatus.setText(spanString);
    }

    public void commandTimer(final boolean start) {
        runOnUiThread(() -> {
            if (start) {
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.setOnChronometerTickListener(chronometer -> {
                    //long systemCurrTime = SystemClock.elapsedRealtime();
                    //long chronometerBaseTime = mChronometer.getBase();
                    //long deltaTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(systemCurrTime - chronometerBaseTime);
                    //if (deltaTimeSeconds % 15L == 0) { }
                });

                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
            } else {
                mChronometer.setVisibility(View.INVISIBLE);
                mChronometer.stop();
            }
        });


    }

    public void checkCommandPermissions(Command c) {
        // Commands requiring superuser
        if (c.isSuperUser()) {
            // Check if we are superuser
            if (!Shell.rootAccess()) {
                Toast.makeText(getApplicationContext(),
                        "Superuser/root access not detected and required to run this command. You can hide superuser commands in settings",
                        Toast.LENGTH_LONG).show();
            }
        }
        // Command defined permissions
        // full list: adb shell pm list permissions -d -g
        // app state: adb shell dumpsys package net.kwatts.android.droidcommandpro
        // grant/revoke: adb shell pm (grant|revoke) net.kwatts.android.droidcommandpro <permission>
        //TODO: this is user defined data, need to sanitize/make sure it doesn't lead to crashes. For now wrapping in try/catch block
        try {
            for (String p : c.getPermissionList()) {
                checkCommandPermission(p);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        //TODO: remove default permissions for all commands after UI updates to allow users to set
        //for (int x = 0; x < permissionsList.length;x++) {
        //    checkCommandPermission(permissionsList[x]);
        //}


    }

    public void checkCommandPermission(String permission) {
        int result;

        result = ContextCompat.checkSelfPermission(MainActivity.this, permission);
        if (result != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                //Toast.makeText(getApplicationContext(), "This command needs permission " + permission + " and may not run properly without it granted",
                //     Toast.LENGTH_LONG).show();
                showPermissionExplanation("This command needs " + permission
                        + " and may not run properly without it granted", permission);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, COMMAND_PERMISSION);
            }

        }

    }

    private void showPermissionExplanation(String message, final String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Needed")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, id) ->
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, COMMAND_PERMISSION));
        builder.create().show();
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissionsList) {
            result = ContextCompat.checkSelfPermission(MainActivity.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                    lastPermissionStatus = true;
                } else {
                    StringBuilder permsNoGrant = new StringBuilder();
                    for (String per : permissionsList) {
                        permsNoGrant.append("\n").append(per);
                    }
                    lastPermissionStatus = false;
                    // permissions list of don't granted permission
                }
                return;
            }

            case COMMAND_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                    lastPermissionStatus = true;
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();

                } else {
                    StringBuilder permsNoGrant = new StringBuilder();
                    for (String per : permissionsList) {
                        permsNoGrant.append("\n").append(per);
                    }
                    lastPermissionStatus = false;
                    //Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    // permissions list of don't granted permission
                }

            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notify";
            String description = "show last running command output";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Map<String, String> splitVariables(String v) {
        try {
            return Splitter.on(',')
                    .omitEmptyStrings()
                    .trimResults()
                    .withKeyValueSeparator("=")
                    .split(v);
        } catch (Exception e) {
            Timber.e(e, "Unable to parse variables!");
        }

        return null;
    }

    private void loadVariables() {
        mAppPackagesList = new ArrayList<>();
        mNetworkInterfaceList = new ArrayList<>();

        String thisPackageName = getPackageName();
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> app_list = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : app_list) {
            try {
                if (info.packageName.equals(thisPackageName)) {
                    mApplicationId = info.uid;
                }
                mAppPackagesList.add(info.packageName);
            } catch (Exception e) {
                Timber.e(e, "Unable to get list of packages!");
            }
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp()) {
                    mNetworkInterfaceList.add(networkInterface);
                }
                //Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            }
        } catch (Exception e) {
            Timber.e(e, "Unable to get list of network interfaces!");
        }

        //Defaults to helper variables avail to commands
        mUserMapVars.put("NETWORK_INTERFACE", "wlan0");
        mUserMapVars.put("APP_PACKAGE", App.INSTANCE.getApplicationContext().getPackageName());
        String v = mSharedPref.getString("variablesEditText", "");
        Map<String, String> m = splitVariables(v);
        if (m != null) {
            mUserMapVars.putAll(m);
        }

        mCustomVarsAdapter = new CustomAdapterVars(MainActivity.this, mAppPackagesList);
        mCustomVarsNetworkInterfaceAdapter = new CustomAdapterNetworkInterfaceVars(MainActivity.this, mNetworkInterfaceList);
    }

    public void showVariableView() {
        loadVariables();

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Variable Editor")
                .customView(R.layout.custom_addvariables_dialog, true)
                .positiveText("DONE")
                .onPositive(
                        (dialog1, which) ->
                        {
                            try {
                                if (mPackagesSpinner.getSelectedItemPosition() >= 0) {
                                    mUserMapVars.put("APP_PACKAGE", mAppPackagesList.get(mPackagesSpinner.getSelectedItemPosition()));
                                }

                                if (mNetworkInterfaceSpinner.getSelectedItemPosition() >= 0) {
                                    mUserMapVars.put("NETWORK_INTERFACE", mNetworkInterfaceList.get(mNetworkInterfaceSpinner.getSelectedItemPosition()).getDisplayName());

                                }
                                mCurrentUserVars = mDialogEditUserVars.getText().toString();
                                Map<String, String> m = splitVariables(mCurrentUserVars);
                                if (m != null) {
                                    mUserMapVars.putAll(m);
                                }

                                String chosenFileName = mDialogFileSelectedVars.getText().toString();
                                if (TextUtils.isEmpty(chosenFileName)) {
                                    mUserMapVars.put("FILE_CHOOSER", "");

                                } else {
                                    mUserMapVars.put("FILE_CHOOSER", chosenFileName);
                                }

                            } catch (Exception e) {
                                Timber.e(e);
                            }


                        })
                .build();

        mPackagesSpinner = dialog.getCustomView().findViewById(R.id.spinnerAppPackageVar);
        mPackagesSpinner.setAdapter(mCustomVarsAdapter);
        TextView networkInterfaceVarDetailsTextView = dialog.getCustomView().findViewById(R.id.tvNetworkInterfaceVarDetails);
        mNetworkInterfaceSpinner = dialog.getCustomView().findViewById(R.id.spinnerNetworkInterfaceVar);
        mNetworkInterfaceSpinner.setAdapter(mCustomVarsNetworkInterfaceAdapter);
        mDialogEditUserVars = dialog.getCustomView().findViewById(R.id.dialogEditUserVars);
        mToggleButtonVariables = dialog.getCustomView().findViewById(R.id.toggleButtonVariables);

        mButtonFileSelectedVariables = dialog.getCustomView().findViewById(R.id.buttonFileSelectedVariables);
        mDialogFileSelectedVars = dialog.getCustomView().findViewById(R.id.dialogFileSelectedVars);

        mButtonFileSelectedVariables.setOnClickListener(v -> {

            String startPath;

            // first see if directory is entered
            String inputFileName = mDialogFileSelectedVars.getText().toString();
            if (!TextUtils.isEmpty(inputFileName)) {
                startPath = new File(inputFileName).getAbsolutePath();
            } else {
                if (Shell.getShell().isRoot()) {
                    startPath = "/";
                } else {
                    startPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
            }


            //@Override
            new ChooserDialog().with(MainActivity.this)
                    .withStartFile(startPath)
                    .withChosenListener((path, pathFile) -> mDialogFileSelectedVars.setText(path))
                    .build()
                    .show();
        });

        // Set to previously set user vars
        if (mCurrentUserVars != null) {
            mDialogEditUserVars.setText(mCurrentUserVars);
        }
        mPackagesSpinner.setSelection(mAppPackagesList.indexOf(mUserMapVars.get("APP_PACKAGE")));
        mNetworkInterfaceSpinner.setSelection(mNetworkInterfaceList.indexOf(mUserMapVars.get("NETWORK_INTERFACE")));

        AdapterView.OnItemSelectedListener myListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getId() == R.id.spinnerNetworkInterfaceVar) {
                    NetworkInterface netInterface = mCustomVarsNetworkInterfaceAdapter.spinnerVars.get(position);
                    Timber.d("networkinterface selected: %s", netInterface.getDisplayName());
                    try {
                        StringBuilder n = new StringBuilder();
                        Enumeration ee = netInterface.getInetAddresses();
                        while (ee.hasMoreElements()) {
                            InetAddress i = (InetAddress) ee.nextElement();
                            n.append(i.getHostAddress()).append(" ");
                        }

                        networkInterfaceVarDetailsTextView.setText(netInterface.getDisplayName() + ": " +
                                "isUp=" + netInterface.isUp() + "," +
                                "addresses=" + n.toString());
                    } catch (Exception e) {
                        networkInterfaceVarDetailsTextView.setText(netInterface.getDisplayName() + ": " + e.getMessage());

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
        mNetworkInterfaceSpinner.setOnItemSelectedListener(myListener);


        dialog.show();
        //positiveAction.setEnabled(true);  //default to false if there are watchers

    }

    public void showRemoveCommandView() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        if (mSpinnerCommands.getSelectedItemPosition() >= 0) {
            Command c = mCustomCmdsAdapter.getItem(mSpinnerCommands.getSelectedItemPosition());

            if (c.isPublic && !isUserAdmin()) {
                Toast.makeText(getApplicationContext(), "This is a public command, maybe you meant to select/remove a private command?",
                        Toast.LENGTH_SHORT).show();
            }

            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title("Remove command")
                    .customView(R.layout.custom_removecommand_dialog, true)
                    .positiveText("REMOVE")
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog1, which) -> {
                        if (firebaseUser != null) {
                            try {
                                removeCommand(c);
                            } catch (Exception e) {
                                Timber.e(e);
                            }
                            Toast.makeText(getApplicationContext(), "Command removed for " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please Login to remove commands!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build();

            View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
            TextView tvRemoveCommandDetails = dialog.getCustomView().findViewById(R.id.tvRemoveCommandDetails);


            if (c.key != null) {

                String[] permString = c.getPermissionList().toArray(new String[0]);

                tvRemoveCommandDetails.setText("key: " + c.key +
                        "\nuid: " + c.getUid() +
                        "\nuser: " + c.getEmail() +
                        "\nisAdmin: " + isUserAdmin() +
                        "\nisPublic: " + c.isPublic +
                        "\nisPinned: " + c.isPinned() +
                        "\nruncounts: " + c.getRuncounts() +
                        "\nlastused: " + new Date(c.getLastused()).toString() +
                        "\npermissions: " + Arrays.toString(permString) +
                        "\ndescription: " + c.getDescription() +
                        "\ncommand: " + c.getCommand());

                if (c.isPublic && !isUserAdmin()) {
                    Toast.makeText(getApplicationContext(), "No can do, this command is public and read only.", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                    positiveAction.setEnabled(true);
                }


            } else {
                Toast.makeText(getApplicationContext(), "Not removing, command doesn't exist for you.", Toast.LENGTH_SHORT).show();
            }
//
        }

    }

    public void showCommandView(FirebaseUser user, Command c) {
        //If command is empty, we are creating a new one.
        final boolean isNewCommand = c.getCommand().isEmpty();


        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Command editor")
                .customView(R.layout.custom_addcommand_dialog, true)
                .negativeText(android.R.string.cancel)
                .positiveText("DONE")
                .onPositive((dialog1, which) -> {
                    if (user != null) {

                        List<String> tags = new ArrayList<>();
                        if (isUserAdmin()) {
                            try {
                                String addPermissionSelected = spinnerAddPermission.getSelectedItem().toString();
                                String removePermissionSelected = spinnerRemovePermission.getSelectedItem().toString();
                                if (!addPermissionSelected.equals("-")) {
                                    c.addPermission(addPermissionSelected);
                                }
                                if (!removePermissionSelected.equals("-")) {
                                    c.removePermission(removePermissionSelected);
                                }
                            } catch (Exception e) {
                                Timber.e(e);
                            }

                            c.isPublic = tagAdminIsPublicCheckBox.isChecked();
                            if (tagAdminOnboardingCheckBox.isChecked()) {
                                tags.add("onboarding");
                            }
                        }

                        if (tagSuperUserCheckBox.isChecked()) {
                            tags.add("superuser");
                        }
                        if (tagPinnedCheckBox.isChecked()) {
                            tags.add("pinned");
                        }
                        c.setUid(user.getUid());
                        c.setEmail(user.getEmail());
                        c.setTagList(tags);
                        c.setDescription(dialogEditDescription.getText().toString());
                        c.setCommand(dialogEditCommand.getText().toString());
                        c.setLastused(System.currentTimeMillis());

                        try {
                            writeCommand(c);
                        } catch (Exception e) {
                            Timber.e(e);
                        }


                        Toast.makeText(getApplicationContext(), "Command saved for " + user.getEmail()
                                + ", isNewCommand=" + isNewCommand, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Please Login to create commands!", Toast.LENGTH_SHORT).show();
                    }


                })
                .build();

        // Custom options only available for admins
        if (isUserAdmin()) {
            addCommandLinearLayoutAdmin = dialog.getCustomView().findViewById(R.id.addCommandLinearLayoutAdmin);

            tagAdminIsPublicCheckBox = dialog.getCustomView().findViewById(R.id.tagAdminIsPublicCheckBox);
            tagAdminIsPublicCheckBox.setChecked(c.isPublic);
            tagAdminOnboardingCheckBox = dialog.getCustomView().findViewById(R.id.tagAdminOnboardingCheckBox);
            tagAdminOnboardingCheckBox.setChecked(c.isOnboarding());

            spinnerAddPermission = dialog.getCustomView().findViewById(R.id.spinnerAddPermission);
            String[] permAddList = Util.getPermissions();  //TODO: get perms server side
            final List<String> permsAddStringList = new ArrayList<>(Arrays.asList(permAddList));
            final ArrayAdapter<String> spinnerArrayPermsAddAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, permsAddStringList);
            spinnerArrayPermsAddAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAddPermission.setAdapter(spinnerArrayPermsAddAdapter);
            spinnerRemovePermission = dialog.getCustomView().findViewById(R.id.spinnerRemovePermission);

            List<String> removePermissionList = new ArrayList<>(c.getPermissionList());
            removePermissionList.add(0, "-");

            final ArrayAdapter<String> spinnerArrayPermsRemoveAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, removePermissionList);
            spinnerArrayPermsRemoveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRemovePermission.setAdapter(spinnerArrayPermsRemoveAdapter);


            addCommandLinearLayoutAdmin.setVisibility(View.VISIBLE);
        }


        String[] showPermissionsArray;
        String showPermissionsString;
        List<String> showPermissions = c.getPermissionList();
        if (showPermissions != null) {
            showPermissionsArray = showPermissions.toArray(new String[0]);
            showPermissionsString = Arrays.toString(showPermissionsArray);
        } else {
            showPermissionsString = "[]";
        }


        TextView tvAddCommandAttributes = dialog.getCustomView().findViewById(R.id.tvAddCommandAttributes);


        if (isNewCommand) {
            tvAddCommandAttributes.setVisibility(View.GONE);
        } else {
            tvAddCommandAttributes.setText("key: " + c.key +
                    "\nuser: " + c.getEmail() +
                    "\nisAdmin:" + isUserAdmin() +
                    "\nruncounts: " + c.getRuncounts() +
                    "\nisPublic=" + c.isPublic +
                    "\nneeds_superuser=" + c.isSuperUser() +
                    "\nneeds_permissions=" + showPermissionsString
            );
        }


        tagSuperUserCheckBox = dialog.getCustomView().findViewById(R.id.tagSuperUserCheckBox);
        tagPinnedCheckBox = dialog.getCustomView().findViewById(R.id.tagPinnedCheckBox);
        dialogEditDescription = dialog.getCustomView().findViewById(R.id.dialogEditDescription);
        dialogEditCommand = dialog.getCustomView().findViewById(R.id.dialogEditCommand);
        dialogEditCommand.setText(c.getCommand());

        if (c.key != null) {
            dialogEditDescription.setText(c.getDescription());
            if (c.getTagList() != null) {
                for (String tag : c.getTagList()) {
                    if (tag.equals("superuser")) {
                        tagSuperUserCheckBox.setChecked(true);
                    } else if (tag.equals("pinned")) {
                        tagPinnedCheckBox.setChecked(true);
                    }
                }
            }

        }

        dialog.show();
    }
}
