package net.kwatts.android.droidcommandpro.commands;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.jf.dexlib2.DexFileFactory;
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
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.util.EncodedValueUtils;
import org.jf.dexlib2.analysis.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * Created by kwatts on 8/9/18.
 */

// deeper dive with https://github.com/dorneanu/smalisca
//TODO: make command to parse PM and manifest https://developer.android.com/reference/android/content/pm/PackageManager#setApplicationEnabledSetting%28java.lang.String,%20int,%20int%29
public class CommandSmali {

    public static String cmd = "CMD_SMALI";

    public static JSONObject execute(android.content.Context ctx, List<String> args) {
        String packageApkFileName = getApkFileName(ctx,args.get(0));


        JSONObject res = new JSONObject();

        if (packageApkFileName == null) {
            return res;
        }


        org.jf.dexlib2.dexbacked.DexBackedDexFile dexFile = null ;
        try {

            dexFile = DexFileFactory.loadDexFile(packageApkFileName, Opcodes.getDefault());
            //TODO: process multiple dex files, see: https://github.com/Sable/soot/blob/develop/src/main/java/soot/dexpler/DexFileProvider.java

        } catch (IOException ioe) {
            Timber.e(ioe,"Unable to load APK");
            return res;
        }
        if (dexFile != null) {
            try {
                res.put("dex_class_count", dexFile.getClassCount());
                res.put("dex_field_count", dexFile.getFieldCount());
                res.put("dex_method_count", dexFile.getMethodCount());
                res.put("dex_string_count", dexFile.getStringCount());
                res.put("dex_type_count", dexFile.getTypeCount());

                JSONObject dex_types = new JSONObject();
                for (int i = 0; i < dexFile.getTypeCount(); i++) {
                    dex_types.put("dex_type_" + i, dexFile.getType(i));
                }
                res.put("dex_types", dex_types);

            } catch (Exception e) { }

            //res = getAppSmali(dexFile);
        } else
        {
            Timber.d("dex file is null");
        }

        return res;
    }


    public static void decompileApp(String appname) {



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

    public static JSONObject getResources(android.content.Context ctx, String appName) {
        JSONArray resval = new JSONArray();
        JSONObject res = new JSONObject();

        try {
            // You can get PackageManager from the Context
            android.content.res.Resources resources = ctx.getPackageManager().getResourcesForApplication(appName); //"com.android.settings"

            java.lang.reflect.Field[] fields = resources.getClass().getFields();
            int allfields[] = {};
            for(int z = 0; z < fields.length; z++){
                // something like this, look at https://android.googlesource.com/platform/frameworks/base/+/0e2d281/core/java/android/content/res/Resources.java
               // allfields[z] = fields[z].getInt();
            }

            int resId = resources.getIdentifier("entryvalues_font_size", "array", appName);
            if (resId != 0) {
                try {
                    resval.put(resId + ": " + resources.getStringArray(resId).toString());
                } catch (Exception e) { }
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
    public static String getApkFileName(android.content.Context ctx, String appName) {
        final PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> packages =  pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(appName)) {
                return packageInfo.sourceDir;
            }
        }

        return null;
    }



    // Helper methods
    public static JSONObject getAppSmali(DexFile dexFile) {

        JSONObject classStaticFields = new JSONObject();
        JSONObject classFields = new JSONObject();



        //SKIP_CLASS:
        for (ClassDef classDef: dexFile.getClasses()) {

            try {
                classFields.put(classDef.getSourceFile(),classDef.getType());
             /*
                for (Field f : classDef.getFields()) {
                    String containingClass1 = f.getDefiningClass();
                    if (!containingClass1.startsWith("Landroid") || containingClass1.startsWith("Lgoogle")) {
                        classFields.put(containingClass1, f.getName() );
                        EncodedValue initialValue = f.getInitialValue();
                        if (initialValue != null) {
                            if (!EncodedValueUtils.isDefaultValue(initialValue)) {
                                classFields.put(containingClass1 + "." + f.getType() + "." + f.getName(), initialValue.getValueType() + "." + initialValue);
                            }
                        }

                    }
                }
            /*

/*
                for (Field field : classDef.getStaticFields()) {
                    String containingClass = field.getDefiningClass();
                    if (!containingClass.startsWith("Landroid") || containingClass.startsWith("Lgoogle")) {
                        EncodedValue initialValue = field.getInitialValue();
                        if (!EncodedValueUtils.isDefaultValue(initialValue)) {
                            // only dump strings
                            if (initialValue.getValueType() == ValueType.STRING) {
                                //org.jf.baksmali.Adaptors.EncodedValue.EncodedValueAdaptor.writeTo(writer, initialValue, containingClass);
                                // https://github.com/glasses007/smali/blob/master/baksmali/src/main/java/org/jf/baksmali/Adaptors/EncodedValue/EncodedValueAdaptor.java
                                classStaticFields.put("[" +
                                                field.getDefiningClass() + "->" +
                                                field.getName() + "]",
                                        initialValue);

                            }
                        }

                    }

                } */

            } catch (Exception e) {
                Timber.e(e,"Unable to get fields " + e.getMessage());
                try {
                    classFields.put("exception", e.getMessage());
                } catch (Exception e2) { }
            }


/*
            for (Method methodDef: classDef.getMethods()) {
                MethodImplementation methodImpl = methodDef.getImplementation();
                if (methodImpl != null) {
                    for (Instruction instruction: methodImpl.getInstructions()) {
                        if (instruction instanceof ReferenceInstruction) {
                            if (((ReferenceInstruction)instruction).getReferenceType() == ReferenceType.METHOD) {
                                MethodReference methodReference =
                                        (MethodReference) ((ReferenceInstruction)instruction).getReference();
                                //Timber.d(String.format("found invocation of method: %s", methodReference));
                            }
                        }
                    }
                }
            } */
        }

        return classFields;
    }

}
