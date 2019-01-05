package net.kwatts.android.droidcommandpro;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.MainThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import net.kwatts.android.droidcommandpro.commands.CommandGetContacts;
import net.kwatts.android.droidcommandpro.commands.Engine;
import net.kwatts.android.droidcommandpro.model.Command;
import net.kwatts.android.droidcommandpro.model.GoogleUser;
import net.kwatts.android.droidcommandpro.model.User;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;



public class MainActivity extends AppCompatActivity implements OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static SharedPreferences mSharedPref;
    private ShareActionProvider shareActionProvider;
    // For intents
    static final String EXTRA_COMMAND = "net.kwatts.android.droidcommandpro.EXTRA_COMMAND";
    static final String EXTRA_COMMAND_KEY = "net.kwatts.android.droidcommandpro.EXTRA_COMMAND_KEY";

    public static final String CHANNEL_ID = "main";
    public static final int RC_SIGN_IN = 10;


    public static List<Command> mCommandQueue = new LinkedList<Command>();
    TextView mTextStatus;
    public static TextView mTextViewState;
    Spinner mSpinnerCommands;
    Spinner mPackagesSpinner;
    Spinner mNetworkInterfaceSpinner;
    EditText mDialogEditUserVars;

    List<String> mAppPackagesList;
    List<NetworkInterface> mNetworkInterfaceList;


	public static EditText mTopCommandView;
    View mAdmobAds;
    AdView mAdView;
    public static StringBuffer mTopOutString = new StringBuffer();
    public static StringBuffer mTopOutStringError = new StringBuffer();
    String mFullCommand = "";


    boolean mRunAsSuperUser = true;
    boolean mDisableAds = false;
    public Button mRunButton;
    public static ScrollView mScrollView;
    public static LinearLayout mLines;
    public static int mTextSize = 22;

    public WebView mWebView;
    public String mWebViewData;

    //public android.content.Context mAppContext;
    public FirebaseDatabase mFirebaseDB;
    public FirebaseUser mFirebaseUser;

    public static List<Command> mGlobalCommands = new ArrayList<>();
    public static List<Command> mUserCommands = new ArrayList<>();

    private ImageButton mGoogleUserSignedInImageButton;
    private com.google.android.gms.common.SignInButton mGoogleUserSignInButton;
    GoogleSignInClient mGoogleSignInClient;
    GoogleUser mGoogleUser;
    private FirebaseAuth mAuth;

    public static FirebaseAnalytics mFirebaseAnalytics;
    //com.topjohnwu.superuser.Shell mShell;


    public static Map<String, String> mUserMapVars = new HashMap<String, String>();
    CustomAdapterCommands mCustomCmdsAdapter;
    CustomAdapterVars mCustomVarsAdapter;
    CustomAdapterNetworkInterfaceVars mCustomVarsNetworkInterfaceAdapter;

    public static ToggleButton mToggleButtonVariables;
    public Button mButtonFileSelectedVariables;
    public EditText mDialogFileSelectedVars;

    private EditText dialogEditDescription;
    private EditText dialogEditCommand;
    private View positiveAction;

    // app id: ca-app-pub-2189980367471582~1443964910
    // ad banner unit id: ca-app-pub-2189980367471582/2916172142
    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    public static NotificationCompat.Builder mNotificationBuilder;
    public static NotificationManagerCompat mNotificationManager;
    public String mLastOutputLine = "";
    int mApplicationId;

    boolean isDebuggable;

    public static Command lastCommandUsed;

    public String[] permissionsList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.READ_CONTACTS
    };


    public static boolean isAdminUserClaim = false;
    public static boolean isUserAdmin() {
        return isAdminUserClaim;
    }


    private static final int MSG_NEWLINE = 1;
    private static final int MSG_CMD_TERMINATED = 2;
    private static final int MSG_CMD_COMPLETE = 3;
    public  Handler mHandler = new Handler()
    {
            public void handleMessage(Message msg)
            {
                    switch (msg.what)
                    {
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


    private void handleMessageNewline(Message msg)
    {
        int cmd_state = msg.arg1;
        String line = (String) msg.obj;

        final boolean autoscroll = mScrollView.getScrollY() + mScrollView.getHeight() >= mLines.getBottom();
        TextView lineView = new TextView(App.INSTANCE.getApplicationContext());
        lineView.setTypeface(Typeface.MONOSPACE);
        lineView.setTextIsSelectable(true);
        if (cmd_state < 0) {
            lineView.setText(new FormattedString(line, mTextSize, Color.RED));
        } else {
            lineView.setText(new FormattedString(line, mTextSize, Color.GREEN));
        }

        mLines.addView(lineView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        // Really shouldn't support more then 5000 lines, wiping...
        if (mLines.getChildCount() > 5000)
                mLines.removeViewAt(0);

        mScrollView.post(new Runnable() {
                public void run()
                {
                        if (autoscroll)
                        {
                        	mScrollView.scrollTo(0, mLines.getBottom() - mScrollView.getHeight());

                        }
                }
        });                            
    }

    private static class FormattedString extends SpannableString
    {
        public FormattedString(String line, int size, int color)
        {
            super(line);
            setSpan(new TextAppearanceSpan("monospace", 0, size, null, null), 0, length(), 0);
            setSpan(new ForegroundColorSpan(color), 0, length(), 0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //mAppContext = getApplicationContext();
        Timber.plant(new DebugTree());

        // am start -n "net.kwatts.android.droidcommandpro/net.kwatts.android.droidcommandpro.MainActivity"
        //          -a "android.intent.action.MAIN" --es net.kwatts.android.droidcommandpro.EXTRA_COMMAND id

        String extra_cmd_key = getIntent().getStringExtra(EXTRA_COMMAND_KEY);
        if (extra_cmd_key != null) {
            Timber.d("Intent.getStringExtra(" + EXTRA_COMMAND_KEY + "):" + extra_cmd_key);
            Command c = getUserCommandByKey(extra_cmd_key);
            mCommandQueue.add(c);
            Timber.d("Added command " + c.key + " to queue!");
        }



        if (checkPermissions()) { /* permissions granted */ }

        MobileAds.initialize(this, "ca-app-pub-2189980367471582~1443964910");

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mSharedPref.registerOnSharedPreferenceChangeListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //TODO if debug
        isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );


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
            // EXPERIMENTAL: Contacts
            //org.json.JSONObject c = CommandGetContacts.getAllContacts(App.INSTANCE.getContentResolver());
            // EXPERIMENTAL: SMali
            //Engine.processCommand(null, "cmd_smali net.kwatts.android.droidcommandpro");
        } catch (Exception e) {
                Timber.d("Exception with experimental calls:" + e.getMessage());
        }


        try {
        	setContentView(R.layout.main);


            LinearLayout topLinearLayout = findViewById(R.id.topLinearLayout);
            mTextStatus = findViewById(R.id.textViewStatus);
            mTextViewState = findViewById(R.id.textViewState);
            mAdmobAds = findViewById(R.id.adMobView);
            mSpinnerCommands = findViewById(R.id.spinnerGlobalCommands);
    		mTopCommandView = findViewById(R.id.topCommandView);
        	mScrollView = findViewById(R.id.topOutputView);
            mWebView  = findViewById(R.id.webview);
        	mLines = findViewById(R.id.lines);
            if (mSharedPref.getBoolean("disableAds", false)) {
                topLinearLayout.removeView(mAdmobAds);
            } else {
                mAdView = findViewById(R.id.adMobView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

            mGoogleUserSignedInImageButton = findViewById(R.id.signed_in_image_button);
            mGoogleUserSignInButton = findViewById(R.id.sign_in_button);


            new AsyncTask<Void, Void, Integer>() {
                protected Integer doInBackground(Void... params) {
                    int c1 = Util.copyAssetsToCacheDirectory(App.INSTANCE.getApplicationContext(),true,"bin");
                    int c2 = Util.copyAssetsToCacheDirectory(App.INSTANCE.getApplicationContext(),true,"scripts");
                    int c3 = Util.copyAssetsToCacheDirectory(App.INSTANCE.getApplicationContext(),true,"share");
                    int c = c1 + c2 + c3;

                    //if (c > 0) {
                        Shell.sh("/system/bin/chmod -R 755 " + getCacheDir().getAbsolutePath() + "/bin").submit();
                        Shell.sh("/system/bin/chmod -R 755 " + getCacheDir().getAbsolutePath() + "/scripts").submit();
                        Shell.sh("/system/bin/chmod -R 755 " + getCacheDir().getAbsolutePath() + "/share").submit();
                    //}

                    return c;
                }
                protected void onPostExecute(Integer count) {
                    Timber.d(count + " files copied!");
                }
            }.execute();


            android.widget.AdapterView.OnItemSelectedListener myListener = new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (parent.getId()) {
                        case R.id.spinnerGlobalCommands:
                            Command c =  mCustomCmdsAdapter.getItem(position);
                            if (c != null) {
                                mTopCommandView.setText(c.getCommand());
                                //hide keyboard
                                InputMethodManager imm = (InputMethodManager) App.INSTANCE.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(mTopCommandView.getWindowToken(), 0);
                                //mTopCommandView.setSelection(c.getCommand().length());
                            }
                            break;
                        default:
                            break;
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {}
            };
            mSpinnerCommands.setOnItemSelectedListener(myListener);
            mRunButton = findViewById(R.id.runButton);
            mRunButton.setOnClickListener(this);



            mGoogleUserSignedInImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFirebaseUser != null) {
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, RC_SIGN_IN);
                    }
                }
            });
            mGoogleUserSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });

            mFirebaseUser = mAuth.getCurrentUser();
            if (mFirebaseUser != null) {
                if (mFirebaseUser.isAnonymous()) {
                    refreshUserUI(false );
                } else {
                    refreshUserUI(true);
                }

            } else {
                refreshUserUI(false );
            }


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


            // EXPERIMENTAL: Smali Command
            //CommandSmali.execute(getApplicationContext(), "com.bose.monet");
            // END

            // EXPERIMENTAL: Services
            //Intent i = new Intent(this, CommandIntentService.class);
            //i.putExtra("cmd_request", "id");
            //startService(i);
            // END





        } catch (Exception e) { 
         	Timber.e( "MainActivity onCreate() failed:" + e.getMessage());
         	e.printStackTrace();
        }

    }

    @Override
    protected void onResume()
    {
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

        setTextState("Command ready" +
                "... is_superuser=" + Shell.getShell().isRoot(), "","");
    }



    public void onClick(View v) {

        /*
            Shell tShell = com.topjohnwu.superuser.Shell.getShell();
            try {
                tShell.close();
            } catch (IOException e) {
                Timber.e("exception killing shell");
            } */


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
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=XYZ123
                GoogleSignInAccount account = task.getResult(com.google.android.gms.common.api.ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (com.google.android.gms.common.api.ApiException e) {
                Timber.d( "signInResult:failed code=" + e.getStatusCode());
                Toast.makeText(getApplicationContext(), "Unable to sign in with Google Play Services (status_code: " + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
            }
        }



    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        mGoogleUser = new GoogleUser();
        mGoogleUser.email = acct.getEmail();
        mGoogleUser.displayName = acct.getDisplayName();
        mGoogleUser.photoUrl = acct.getPhotoUrl().toString();
        mGoogleUser.serverAuthCode = acct.getServerAuthCode();
        mGoogleUser.gId = acct.getId();
        mGoogleUser.gIdToken = acct.getIdToken();
        StringBuffer tmpScopes = new StringBuffer();
        for(com.google.android.gms.common.api.Scope s : acct.getGrantedScopes()) {
            tmpScopes.append(s.getScopeUri() + " ");
        }
        mGoogleUser.oauthScopes = tmpScopes.toString();
        mGoogleUser.expirationTime = acct.getExpirationTimeSecs();

        Timber.d( "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Timber.d( "signInWithCredential:success");
                            mFirebaseUser = mAuth.getCurrentUser();
                            FirebaseDatabase.getInstance().getReference().child("google_users").child(mFirebaseUser.getUid()).setValue(mGoogleUser);
                            refreshUserUI(true);
                        } else {
                            Timber.d( "signInWithCredential:failure:" + task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    static long startTime;
    static int mExecState = 0;
    public void runCommand(Command c) {
        //clear window
        mTopOutString.setLength(0);
        mTopOutStringError.setLength(0);
        mLines.removeAllViews();

        lastCommandUsed = c;
        addToCommandRuncounts(c);

        String coreCommand = c.getCommand();




        mTextSize = Integer.parseInt(mSharedPref.getString("textSize","22"));

        StringBuffer vars = new StringBuffer();

        if (mSharedPref.getBoolean("includeToolsPath", true)) {
            vars.append("PATH=$PATH:" + App.INSTANCE.getCacheDir().getAbsolutePath() + "/scripts:" +
                                        App.INSTANCE.getCacheDir().getAbsolutePath() + "/bin" + ";");
        }

        for (Map.Entry<String, String> entry : mUserMapVars.entrySet()) {
            vars.append(entry.getKey() + "=" + entry.getValue() + ";");
        }

        Timber.d( "User Variables:" + vars.toString());

        String runCommand;

        if (mToggleButtonVariables != null) {
            if (mToggleButtonVariables.isChecked()) {
                runCommand = vars.toString() + coreCommand;
            } else {
                runCommand = coreCommand;
            }
        } else {
            runCommand = vars.toString() + coreCommand;
        }

/* Experimental...
        String res_command = Engine.processCommand(mUserMapVars, coreCommand);
        if (res_command != null) {
            Toast.makeText(App.INSTANCE.getApplicationContext(), "Code commands are not supported yet.", Toast.LENGTH_SHORT).show();
            return;
        }
*/


        // NOW WE RUN!!!
        Timber.d( "Running:" + runCommand);
        int shell_status = Shell.getShell().getStatus();
        setTextState("Command running as " + ((shell_status > 0) ? "root" : "user") +
                           "... shell_status=" + shell_status +
                           ",is_superuser=" + Shell.getShell().isRoot(),
                      "","");
        logEvent("command_run", runCommand, "root_available=" + shell_status);


        List<String> consoleList;
        List<String> consoleListError;
        consoleList = new CallbackList<String>() {
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


        //https://github.com/topjohnwu/libsu/blob/master/superuser/src/main/java/com/topjohnwu/superuser/internal/PendingJob.java
        Shell.ResultCallback runResultCallback = new Shell.ResultCallback() {
            @MainThread
            @Override
            public void onResult(Shell.Result out) {
                CharSequence l = "";
                if (mLines.getChildCount() > 0) {
                    l =  ((TextView) mLines.getChildAt(mLines.getChildCount() -1 )).getText();
                }
                long ms = SystemClock.elapsedRealtime() - startTime;
                double s = ms / 1000.0;
                logEvent("command_end", coreCommand, "complete in " + s + "s lines=" + mLines.getChildCount() + ",state=" + ((mExecState < 0) ? "fail" : "success"));
                setTextState("Command finished after " + s + "secs... lines=" + mLines.getChildCount() + ",state=" + ((mExecState < 0) ? "fail" : "success") +
                        ",shell_code=" + out.getCode(),"",l.toString());
            }
        };


        if (Shell.getShell().isRoot()) {
            com.topjohnwu.superuser.Shell.Job j = Shell.su(runCommand).to(consoleList, consoleListError);
            j.submit(runResultCallback);
        }
        else {
            com.topjohnwu.superuser.Shell.Job j2 = Shell.sh(runCommand).to(consoleList, consoleListError);
            j2.submit(runResultCallback);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        MenuItem actionItem = menu.findItem(R.id.action_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(actionItem);
        if (shareActionProvider == null) {
            shareActionProvider = new ShareActionProvider(this);
            MenuItemCompat.setActionProvider(actionItem, shareActionProvider);
        }
        shareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        refreshData();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(Build.VERSION.SDK_INT > 11) {
            invalidateOptionsMenu();
            if ((FirebaseAuth.getInstance().getCurrentUser() != null)) {
                menu.findItem(R.id.user_saved_command).setVisible(true);
                menu.findItem(R.id.user_remove_command).setVisible(true);
                menu.findItem(R.id.action_logout).setVisible(true);
                menu.findItem(R.id.action_login).setVisible(false);
            } else {
                menu.findItem(R.id.action_login).setVisible(true);
                menu.findItem(R.id.user_saved_command).setVisible(false);
                menu.findItem(R.id.user_remove_command).setVisible(false);
                menu.findItem(R.id.action_logout).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.user_saved_command:
            showAddCommandView();
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
        case R.id.action_logs:
            openLynxActivity();
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
        Timber.i( "onSharedPreferenceChanged callback");
        switch (key) {
            case "textSize":
                mTextSize = Integer.parseInt(sharedPreferences.getString("textSize","22"));
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
                } catch (Exception e) {}

        }

    }

    public void initFirebase() {
        DatabaseReference mFbaseDBCommandsRef = mFirebaseDB.getReference();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserCommands.clear();
        mGlobalCommands.clear();
        lastCommandUsed = null;

        if (mFirebaseUser != null) {
            if (!mFirebaseUser.isAnonymous()) {
                User user = new User();
                user.username = mFirebaseUser.getDisplayName();
                user.email = mFirebaseUser.getEmail();
                user.phone_number = mFirebaseUser.getPhoneNumber();
                user.photo_url = mFirebaseUser.getPhotoUrl().toString();

                FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid()).setValue(user);

                mFirebaseUser.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(GetTokenResult result) {
                        //Map<String, Object> res_claims = result.getClaims();
                        Boolean isAdmin = Boolean.FALSE;
                        try {

                            Object obj = result.getClaims().getOrDefault("admin", Boolean.FALSE);
                            if (obj instanceof Boolean) {
                                isAdmin = (Boolean) obj;
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
                        }
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
                                mUserCommands.add(cmd);

                                if (cmd.isPinned()) {
                                    addDynamicShortcut(cmd.key, cmd.getCommand(), cmd.getDescription());
                                }
                            }
                        }

                        if (lastCommandUsed != null) {
                            mUserCommands.remove(lastCommandUsed);
                            mUserCommands.add(0, lastCommandUsed);
                        }

                        List<Command> allCommands = new ArrayList<>();
                        allCommands.addAll(mUserCommands);
                        allCommands.addAll(mGlobalCommands);

                        mCustomCmdsAdapter = new CustomAdapterCommands(MainActivity.this, allCommands);
                        mSpinnerCommands.setAdapter(mCustomCmdsAdapter);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        } else {
            // setTextUserStatus("Logged in as anonymous");
        }

        /* global commands */
        mFbaseDBCommandsRef.child("commands_v2").child("global").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGlobalCommands.clear();

                for (DataSnapshot cmdSnapshot: dataSnapshot.getChildren()) {
                    Command cmd = cmdSnapshot.getValue(Command.class);

                    if (cmd != null) {
                        cmd.key = cmdSnapshot.getKey();
                        mGlobalCommands.add(cmd);
                    }
                }

                List<Command> allCommands = new ArrayList<>();
                allCommands.addAll(mUserCommands);
                allCommands.addAll(mGlobalCommands);

                mCustomCmdsAdapter = new CustomAdapterCommands(MainActivity.this, allCommands);
                mSpinnerCommands.setAdapter(mCustomCmdsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    
    private static Command getUserCommandByKey(String key) {
        for(Command c : mUserCommands){
            if(c.key != null && c.key.equalsIgnoreCase(key)) {
                return c;
            }
        }
        return null;
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

    public static void addToCommandRuncounts(Command c) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        if ((!c.isPublic) && (c.key != null)) {
            c.addToRuncounts();
            Map<String, Object> cmdValues = c.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/commands_v2/" + c.getUid() + "/" + c.key, cmdValues);
            db.updateChildren(childUpdates);
        }

    }


    public void addDynamicShortcut(String cmd_key, String cmd, String label) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
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
            Timber.e("Unable to add dynamic shortcut!");
            e.printStackTrace();
        }
    }

    public void removeDynamicShortcuts() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                ShortcutManager smgr = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
                smgr.removeAllDynamicShortcuts();
            }
        } catch (Exception e) {
            Timber.e("Unable to remove dynamic shortcut!");
            e.printStackTrace();
        }
    }

    private void refreshData() {
        if (shareActionProvider != null) {
            Intent share = createShareIntent();
            shareActionProvider.setShareIntent(share);
        }
    }
    private Intent createShareIntent() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        StringBuffer dataToSend = new StringBuffer();
        if (mWebView.getVisibility() == View.VISIBLE) {
            if (mWebViewData != null) {
                share.setType("text/html");
                share.putExtra(android.content.Intent.EXTRA_SUBJECT, "Results of ADB Toolkit");
                dataToSend.append(mWebViewData);
            }
        } else {
            for (int x = 0; x < mLines.getChildCount(); x++) {
                TextView currentTextView = (TextView) mLines.getChildAt(x);
                dataToSend.append(currentTextView.getText() + "\r\n");
            }
        }
        share.putExtra(Intent.EXTRA_TEXT, dataToSend.toString());
        return share;
    }

    private void openLynxActivity() {
        LynxConfig lynxConfig = new LynxConfig();
        if (Shell.getShell().getStatus() > 0) {
            lynxConfig.setRunAsSuperUser(true);
        } else { lynxConfig.setRunAsSuperUser(false); }
        lynxConfig.setTextSizeInPx(9);
        lynxConfig.setMaxNumberOfTracesToShow(4000).setFilter("");
        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
    }

    private void setTextUserStatus(String status) {
        SpannableString spanString = new SpannableString(status);
        spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
        mTextStatus.setText(spanString);
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
            e.printStackTrace();

        }

        // send analytics
    }

    private static class ReleaseTree extends Timber.Tree {
        @Override
        protected void log(final int priority, final String tag, final String message, final Throwable throwable) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
        }
    }
    private static class DebugTree extends Timber.DebugTree {
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return String.format("net.kwatts.android.droidcommander [C:%s] [M:%s] [L:%s] ",
                    super.createStackElementTag(element),
                    element.getMethodName(),
                    element.getLineNumber());
        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissionsList) {
            result = ContextCompat.checkSelfPermission(MainActivity.this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted.
                } else {
                    String permsNoGrant = "";
                    for (String per : permissionsList) {
                        permsNoGrant += "\n" + per;
                    }
                    // permissions list of don't granted permission
                }
                return;
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
            Map<String, String> map = com.google.common.base.Splitter.on(',')
                    .omitEmptyStrings()
                    .trimResults()
                    .withKeyValueSeparator("=")
                    .split(v);
            return map;
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }
    private void loadVariables() {
        mAppPackagesList = new ArrayList<String>();
        mNetworkInterfaceList = new ArrayList<NetworkInterface>();

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
                Timber.e("Unable to get list of packages!");
                e.printStackTrace();
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
            Timber.e("Unable to get list of network interfaces!");
            e.printStackTrace();
        }

        //Defaults
        mUserMapVars.put("DIR_SCRIPTS",App.INSTANCE.getApplicationContext().getCacheDir().getAbsolutePath() + "/scripts");
        mUserMapVars.put("DIR_TOOLS",App.INSTANCE.getApplicationContext().getCacheDir().getAbsolutePath() + "/bin");
        mUserMapVars.put("NETWORK_INTERFACE","wlan0");
        mUserMapVars.put("APP_PACKAGE",App.INSTANCE.getApplicationContext().getPackageName());

        String v = mSharedPref.getString("variablesEditText", "");
        Map<String, String> m = splitVariables(v);
        if (m != null) {
            mUserMapVars.putAll(m);
        }

        mCustomVarsAdapter = new CustomAdapterVars(MainActivity.this, mAppPackagesList);
        mCustomVarsNetworkInterfaceAdapter = new CustomAdapterNetworkInterfaceVars(MainActivity.this, mNetworkInterfaceList);
    }


    String mCurrentUserVars;
    public void showVariableView() {
        loadVariables();

        MaterialDialog dialog =
                new MaterialDialog.Builder(this)
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

                                    } catch(Exception e ) {
                                        Timber.e( e.getMessage());
                                    }


                                })
                        .build();

        mPackagesSpinner = (Spinner) dialog.getCustomView().findViewById(R.id.spinnerAppPackageVar);
        mPackagesSpinner.setAdapter(mCustomVarsAdapter);
        TextView networkInterfaceVarDetailsTextView = (TextView) dialog.getCustomView().findViewById(R.id.tvNetworkInterfaceVarDetails);
        mNetworkInterfaceSpinner = (Spinner) dialog.getCustomView().findViewById(R.id.spinnerNetworkInterfaceVar);
        mNetworkInterfaceSpinner.setAdapter(mCustomVarsNetworkInterfaceAdapter);
        mDialogEditUserVars = dialog.getCustomView().findViewById(R.id.dialogEditUserVars);
        mToggleButtonVariables = dialog.getCustomView().findViewById(R.id.toggleButtonVariables);

        mButtonFileSelectedVariables = dialog.getCustomView().findViewById(R.id.buttonFileSelectedVariables);
        mDialogFileSelectedVars = dialog.getCustomView().findViewById(R.id.dialogFileSelectedVars);

        mButtonFileSelectedVariables.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new ChooserDialog().with(MainActivity.this)
                        .withStartFile("/sdcard")
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                mDialogFileSelectedVars.setText(path);
                                //Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build()
                        .show();
            }
        });

        // Set to previously set user vars
        if (mCurrentUserVars != null) {
            mDialogEditUserVars.setText(mCurrentUserVars);
        }
        mPackagesSpinner.setSelection(mAppPackagesList.indexOf(mUserMapVars.get("APP_PACKAGE")));
        mNetworkInterfaceSpinner.setSelection(mNetworkInterfaceList.indexOf(mUserMapVars.get("NETWORK_INTERFACE")));

        android.widget.AdapterView.OnItemSelectedListener myListener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getId()) {
                    case R.id.spinnerNetworkInterfaceVar:
                        NetworkInterface netInterface =  mCustomVarsNetworkInterfaceAdapter.spinnerVars.get(position);
                        Timber.d("networkinterface selected: " + netInterface.getDisplayName());
                        try {
                            StringBuffer n = new StringBuffer();
                            Enumeration ee = netInterface.getInetAddresses();
                            while (ee.hasMoreElements())
                            {
                                InetAddress i = (InetAddress) ee.nextElement();
                                n.append(i.getHostAddress() + " ");
                            }

                            networkInterfaceVarDetailsTextView.setText(netInterface.getDisplayName() + ": " +
                                            "isUp=" + netInterface.isUp() + "," +
                                            "addresses=" + n.toString());
                        } catch (Exception e) {
                            networkInterfaceVarDetailsTextView.setText(netInterface.getDisplayName() + ": " + e.getMessage());

                        }

                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        };
        mNetworkInterfaceSpinner.setOnItemSelectedListener(myListener);


        dialog.show();
        //positiveAction.setEnabled(true);  //default to false if there are watchers

    }


    private CheckBox tagSuperUserCheckBox;
    private CheckBox tagPinnedCheckBox;
    private TextView tvRemoveCommandDetails;

    public void showRemoveCommandView() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        if (mSpinnerCommands.getSelectedItemPosition() >= 0) {
            Command c = mCustomCmdsAdapter.getItem(mSpinnerCommands.getSelectedItemPosition());

            if (c.isPublic && !isUserAdmin()) {
                Toast.makeText(getApplicationContext(), "This is a public command, maybe you meant to select/remove a private command?", Toast.LENGTH_SHORT).show();
            }

            MaterialDialog dialog =
                    new MaterialDialog.Builder(this)
                            .title("Remove command")
                            .customView(R.layout.custom_removecommand_dialog, true)
                            .positiveText("REMOVE")
                            .negativeText(android.R.string.cancel)
                            .onPositive(
                                    (dialog1, which) ->
                                    {
                                        if (firebaseUser != null) {
                                            try {
                                                removeCommand(c);
                                            }
                                            catch (Exception e) {
                                                Timber.e( e.getMessage());
                                            }
                                            Toast.makeText(getApplicationContext(), "Command removed for " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Please Login to remove commands!", Toast.LENGTH_SHORT).show();
                                        }


                                    })
                            .build();

            positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
            tvRemoveCommandDetails = dialog.getCustomView().findViewById(R.id.tvRemoveCommandDetails);


            if (c.key != null) {
                tvRemoveCommandDetails.setText("key: " + c.key +
                        "\nuid: " + c.getUid() +
                        "\nuser: " + c.getEmail() +
                        "\nisAdmin: " + isUserAdmin() +
                        "\nisPublic: " + c.isPublic +
                        "\nisPinned: " + c.isPinned() +
                        "\nruncounts: " + c.getRuncounts() +
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


    private TextView tvAddCommandAttributes;
    public LinearLayout addCommandLinearLayoutAdmin;
    public CheckBox tagAdminIsPublicCheckBox;
    public View updateAction;
    public View createNewAction;

    public void showAddCommandView() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Command currentCommand =  mCustomCmdsAdapter.getItem(mSpinnerCommands.getSelectedItemPosition());
        String currentTextCommand = mTopCommandView.getText().toString();

        MaterialDialog dialog =
                new MaterialDialog.Builder(this)
                        .title("Command editor")
                        .customView(R.layout.custom_addcommand_dialog, true)
                        .neutralText("UPDATE")
                        .positiveText("CREATE NEW")
                        .onPositive(
                                (dialog1, which) ->
                                {

                                if (firebaseUser != null && mFullCommand != null) {
                                    Command c = new Command();
                                    List<String> tags = new ArrayList<>();
                                    if (tagSuperUserCheckBox.isChecked()) {
                                        tags.add("superuser");
                                    }
                                    if (tagPinnedCheckBox.isChecked()) {
                                        tags.add("pinned");
                                    }
                                    c.setUid(firebaseUser.getUid());
                                    c.setEmail(firebaseUser.getEmail());
                                    c.setTagList(tags);
                                    c.setDescription(dialogEditDescription.getText().toString());
                                    c.setCommand(dialogEditCommand.getText().toString());

                                    if (tagAdminIsPublicCheckBox.isChecked()) {
                                        c.isPublic = true;
                                    } else {
                                        c.isPublic = false;
                                    }

                                    try {
                                        writeCommand(c);
                                    }
                                    catch (Exception e) {
                                        Timber.e( e.getMessage());
                                    }


                                    Toast.makeText(getApplicationContext(), "New command created for " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Please Login to create commands!", Toast.LENGTH_SHORT).show();
                                }


                                })
                        .onNeutral(
                                (dialog1, which) ->
                                {

                                if (firebaseUser != null && mFullCommand != null) {
                                    List<String> tags = new ArrayList<>();
                                    if (tagSuperUserCheckBox.isChecked()) {
                                        tags.add("superuser");
                                    }
                                    if (tagPinnedCheckBox.isChecked()) {
                                        tags.add("pinned");
                                    }
                                    currentCommand.setTagList(tags);
                                    currentCommand.setDescription(dialogEditDescription.getText().toString());
                                    currentCommand.setCommand(dialogEditCommand.getText().toString());
                                    try {
                                        writeCommand(currentCommand);
                                    }
                                    catch (Exception e) {
                                        Timber.e( e.getMessage());
                                    }
                                    Toast.makeText(getApplicationContext(), "Command updated for " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Please Login to update commands!", Toast.LENGTH_SHORT).show();
                                }


                        })
                        .build();

        addCommandLinearLayoutAdmin = dialog.getCustomView().findViewById(R.id.addCommandLinearLayoutAdmin);
        tagAdminIsPublicCheckBox = dialog.getCustomView().findViewById(R.id.tagAdminIsPublicCheckBox);
        if (isUserAdmin()) {
            addCommandLinearLayoutAdmin.setVisibility(View.VISIBLE);
        }

        tvAddCommandAttributes = dialog.getCustomView().findViewById(R.id.tvAddCommandAttributes);
        tvAddCommandAttributes.setText("key: " + currentCommand.key +
                "\nuid: " + currentCommand.getUid() +
                "\nuser: " + currentCommand.getEmail() +
                "\nisAdmin:" + isUserAdmin() +
                "\nruncounts: " + currentCommand.getRuncounts() +
                "\nisPublic=" + currentCommand.isPublic);

        createNewAction = dialog.getActionButton(DialogAction.POSITIVE);
        updateAction = dialog.getActionButton(DialogAction.NEUTRAL);

        tagSuperUserCheckBox = dialog.getCustomView().findViewById(R.id.tagSuperUserCheckBox);
        tagPinnedCheckBox = dialog.getCustomView().findViewById(R.id.tagPinnedCheckBox);

        dialogEditDescription = dialog.getCustomView().findViewById(R.id.dialogEditDescription);
        dialogEditCommand = dialog.getCustomView().findViewById(R.id.dialogEditCommand);


        dialogEditCommand.setText(currentTextCommand);

        if (currentCommand.key != null) {
            dialogEditDescription.setText(currentCommand.getDescription());
            if (currentCommand.getTagList() != null) {
                for (String tag : currentCommand.getTagList()) {
                    if (tag.equals("superuser")) {
                        tagSuperUserCheckBox.setChecked(true);
                    } else if (tag.equals("pinned")) {
                        tagPinnedCheckBox.setChecked(true);
                    }
                }
            }

        }



        dialog.show();

        createNewAction.setEnabled(true);  //default to false if there are watchers
        if (currentCommand.isPublic) {
            if (isUserAdmin()) {
                updateAction.setEnabled(true);
            } else {
                updateAction.setEnabled(false);
            }
        } else {
            updateAction.setEnabled(true);
        }

    }




}
