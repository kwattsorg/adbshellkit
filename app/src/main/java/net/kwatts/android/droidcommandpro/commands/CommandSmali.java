package net.kwatts.android.droidcommandpro.commands;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;

import net.kwatts.android.droidcommandpro.ApiReceiver;
import net.kwatts.android.droidcommandpro.App;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.value.BooleanEncodedValue;
import org.jf.dexlib2.iface.value.CharEncodedValue;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.IntEncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.DexFileNamer;
import lanchon.multidexlib2.MultiDexIO;
import timber.log.Timber;

//A deeper dive with https://github.com/dorneanu/smalisca...
//TODO: make command to parse PM and manifest https://developer.android.com/reference/android/content/pm/PackageManager#setApplicationEnabledSetting%28java.lang.String,%20int,%20int%29
/*
am broadcast --user 0 -n net.kwatts.android.droidcommandpro/.AdbshellkitApiReceiver \
    --es socket_input 1 --es socket_output 2 --es api_method cmd_smali --es application_name net.kwatts.android.droidcommandpro
*/

//TODO: yara!
// crypto: https://github.com/xingkong123600/findcrypt-yara

public class CommandSmali  {
    public static String cmd = "smali";
    public static String descr = "Disassembles an application, outputs a json to home directory";
    public static String args = "--es application_name <package name> --ez savetofile <true or false>";
    public static String[] permissions = {""};

    public static void onReceive(final ApiReceiver apiReceiver, final Context context, final Intent intent) {

        final String application_name = intent.getStringExtra("application_name");
        boolean saveFile = intent.getBooleanExtra("savetofile", false);
        ResultReturner.returnData(apiReceiver, intent, out -> {
            if (application_name == null) {
                out.print("");
            } else {
                JSONObject res = run(context, saveFile, application_name);
                out.print(res.toString(1));
            }
        });
    }

    public static JSONObject run(Context ctx, boolean saveFile, String appName) {
        String appNameSmali = appName.replace('.', '/');
        String packageApkFileName = getApkFileName(ctx, appName);

        JSONObject res = new JSONObject();

        if (packageApkFileName == null) {
            return res;
        }

        JSONObject dex_classes = new JSONObject();


        DexFile dex = null;
        try {
            DexFileNamer dexFileNamer = new BasicDexFileNamer();
            Opcodes opcodes = Opcodes.forApi(Build.VERSION.SDK_INT);
            File apkFile = new File(packageApkFileName);

            //dexFile = DexFileFactory.loadDexFile(packageApkFileName, Opcodes.getDefault());
            //TODO: process multiple dex files, see: https://github.com/Sable/soot/blob/develop/src/main/java/soot/dexpler/DexFileProvider.java
            dex = MultiDexIO.readDexFile(true,
                    apkFile,
                    dexFileNamer,
                    opcodes,
                    null);


        } catch (IOException ioe) {
            Timber.e(ioe, "Unable to load APK");
            return res;
        }

        if (dex != null) {
            try {
                //res.put("dex_class_count", dex.getClasses().size());
                Set<? extends ClassDef> dexClasses = dex.getClasses();
                ClassDef[] classDefs = dexClasses.toArray(new ClassDef[0]);


                //JSONObject classes = new JSONObject();
                for (ClassDef clazz : classDefs) {

                    // if "sourcefile": "R.java"
                    // and unzip apk file, get resources.arsc, then strings it or look at offset
                    // or use aapt?

                    // if (!className.startsWith("Landroid") && !className.startsWith("Ljava") && !className.startsWith("Ldalvik")) {
                    if (clazz.getType().startsWith("L" + appNameSmali)) {


                        // CLASS INSTANCE VARIABLES

                        Iterable<? extends Field> fields = clazz.getFields();

                        JSONArray class_fields = new JSONArray();
                        for (Field field : fields) {
                            StringBuilder f = new StringBuilder(field.getName());
                            EncodedValue initialValue = field.getInitialValue();

                            if (initialValue != null) {

                                // https://github.com/glasses007/smali/blob/master/baksmali/src/main/java/org/jf/baksmali/Adaptors/EncodedValue/EncodedValueAdaptor.java
                                //https://github.com/ylya/horndroid/blob/master/src/main/java/com/horndroid/util/FormatEncodedValue.java
                                switch (initialValue.getValueType()) {
                                    case ValueType.STRING:
                                        f.append("=").append(((StringEncodedValue) initialValue).getValue());
                                        break;
                                    case ValueType.INT:
                                        f.append("=").append(((IntEncodedValue) initialValue).getValue());
                                        break;
                                    case ValueType.CHAR:
                                        f.append("=").append(((CharEncodedValue) initialValue).getValue());
                                        break;
                                    case ValueType.BOOLEAN:
                                        f.append("=").append(((BooleanEncodedValue) initialValue).getValue());
                                        break;

                                    default:
                                        //f.append("=").append(ValueType.getValueTypeName(initialValue.getValueType())).append(")");

                                }

                            }
                            class_fields.put(f.toString());
                        }


                        // CLASS METHODS
                        JSONArray class_methods = new JSONArray();

                        for (Method methodDef : clazz.getMethods()) {
                            MethodImplementation methodImpl = methodDef.getImplementation();
                            if (methodImpl != null) {
                                for (Instruction instruction : methodImpl.getInstructions()) {
                                    if (instruction instanceof ReferenceInstruction) {
                                        if (((ReferenceInstruction) instruction).getReferenceType() == ReferenceType.METHOD) {
                                            MethodReference method =
                                                    (MethodReference) ((ReferenceInstruction) instruction).getReference();

                                            String name = method.getName();
                                            String definingClass = method.getDefiningClass();
                                            String returnType = method.getReturnType();

                                            List<? extends CharSequence> paramTypes = method.getParameterTypes();
                                            StringBuilder returnTypes = new StringBuilder("(");
                                            if (paramTypes != null) {
                                                for (CharSequence type : paramTypes) {
                                                    returnTypes.append(type.toString());
                                                }
                                            }
                                            returnTypes.append(")");

                                            class_methods.put(returnType + "," + name + returnTypes.toString());
                                        }
                                    }
                                }
                            }
                        }

                        JSONObject f = new JSONObject();
                        f.put("sourcefile", clazz.getSourceFile());
                        f.put("superclass", clazz.getSuperclass());
                        f.put("fields", class_fields);
                        f.put("methods", class_methods);
                        dex_classes.put(clazz.getType(), f);

                    }

                }

            } catch (Exception e) {
                Timber.e(e);
            }

        } else {
            Timber.d("dex file is null");
        }

        // Combine results
        try {
            JSONObject smali = new JSONObject();
            smali.put("app_package_name", appName);
            smali.put("app_package_apk_filename", packageApkFileName);
            smali.put("dex_classes", dex_classes);

            if (saveFile) {
                try {
                    String output_file = App.FILES_PATH + "/home/" + appName + "_smali.json";
                    File file = new File(output_file);
                    java.io.FileWriter fileWriter = new java.io.FileWriter(file);
                    fileWriter.write(smali.toString(1));
                    fileWriter.flush();
                    fileWriter.close();
                    res.put("output_file", output_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                res = smali;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return res;

    }


    // Get resources for package
    // aapt2 dump resources ./app/release/app-release.apk
    // https://stackoverflow.com/questions/27548810/android-compiled-resources-resources-arsc
    // http://vkswtips.blogspot.com/2012/02/android-ndk-how-to-load-resource-files.html
    // https://android.googlesource.com/platform/frameworks/native/+/jb-dev/libs/utils/README
    // https://www.programcreek.com/java-api-examples/index.php?source_dir=android-toolkit-master/core/src/main/java/parser/arsc/ARSCParser.java#
    // PackagemanagerService.java setEnabledSetting(final String packageName, String className, int newState, final int flags, int userId, String callingPackage)
    // Checks,
    // if the uid of the calling process is system or not
    // if it's system, continue, if not, it should have android.Manifest.permission.CHANGE_COMPONENT_ENABLED_STATE permission
            /*           {
            "android:name": "com.mcafee.systemprovider.pkginstall.AppPreinstallReceiver",
            "android:exported": "false",
            "intent-filter": {
              "action": {
                "android:name": "android.intent.action.PACKAGE_NEEDS_VERIFICATION"
              },
              "data": {
                "android:mimeType": "application/vnd.android.package-archive"
              }
            }
          },

                      PackageManager pm = this.getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName("com.xyzapp",
                            "com.xyzapp.MainActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
          */

    public static JSONObject getResources(Context ctx, String appName) {
        JSONArray resval = new JSONArray();
        JSONObject res = new JSONObject();

        try {
            // You can get PackageManager from the Context
            Resources resources = ctx.getPackageManager().getResourcesForApplication(appName); //"com.android.settings"

            java.lang.reflect.Field[] fields = resources.getClass().getFields();
            int[] allfields = {};
            for (int z = 0; z < fields.length; z++) {
                // something like this, look at https://android.googlesource.com/platform/frameworks/base/+/0e2d281/core/java/android/content/res/Resources.java
                // allfields[z] = fields[z].getInt();
            }

            int resId = resources.getIdentifier("entryvalues_font_size", "array", appName);
            if (resId != 0) {
                try {
                    resval.put(resId + ": " + Arrays.toString(resources.getStringArray(resId)));
                } catch (Exception ignored) {
                }
            }

            res.put(appName, resval);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //catch (PackageManager.NameNotFoundException e) {
        //
        //}


        return res;
    }

    // Get from package manager or 'cmd package list packages -f'
    public static String getApkFileName(Context ctx, String appName) {
        final PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(appName)) {
                return packageInfo.sourceDir;
            }
        }

        return null;
    }


}
