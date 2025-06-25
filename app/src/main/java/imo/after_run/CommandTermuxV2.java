package imo.after_run;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import android.widget.TextView;

public class CommandTermuxV2 {
    /** 
     * must have Termux:API installed and
     * run this on termux first:

     pkg install termux-api
     sed -i 's/# allow-external-apps = true/allow-external-apps = true/g' ~/.termux/termux.properties
     termux-setup-storage

     **/

    /**
     * put this on AndroidManifest.xml (above "<application "):

     <!-- Storage Permission -->
     <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
     android:maxSdkVersion="28" /> <!-- Only for Android 9 (API 28) and below -->
     <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" /> <!-- For Android 11+ -->

     <!-- Termux Permission -->
     <uses-permission android:name="com.termux.permission.RUN_COMMAND"/>

     **/
    public static boolean backgroundMode = true;
    private static final String COMMAND_END_KEY = "END HEHE";

    @Deprecated
    public static boolean permissionIsGranted(Activity activity){
        return hasTermuxPermission(activity);
    }

    @Deprecated
    public static void permissionRequest(Activity activity){
        requestTermuxPermission(activity);
    }

    public static boolean hasTermuxPermission(Activity activity){
        return activity.checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestTermuxPermission(Activity activity){
        activity.requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"}, 69);
    }

    public static boolean hasStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        } else {
            activity.requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            activity.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    public static void checkAndRequestPermissions(Activity activity){
        if(! hasStoragePermission(activity)){
            requestStoragePermission(activity);
            activity.finish();
            return;
        }

        if(! hasTermuxPermission(activity)){
            requestTermuxPermission(activity);
            activity.finish();
            return;
        }
    }


    private String command;
    private Activity mActivity;

    private Runnable onEnd;
    private Runnable onLoading;
    private Runnable onError;

    public CommandTermuxV2(String command, Activity mActivity){
        this.command = command;
        this.mActivity = mActivity;
    }

    public CommandTermuxV2 setOnEnd(Runnable runnable){
        onEnd = runnable;
        return this;
    }

    public CommandTermuxV2 setOnLoading(Runnable runnable){
        onLoading = runnable;
        return this;
    }

    public CommandTermuxV2 setOnError(Runnable runnable){
        onError = runnable;
        return this;
    }

    public CommandTermuxV2 setLoading(final TextView textview){
        return setLoading(textview, "waiting");
    }

    public CommandTermuxV2 setLoading(final TextView textview, final String loadingText){
        this.setOnLoading(new Runnable(){
                String[] waiting = {loadingText+".", loadingText+"..", loadingText+"..."};
                int waitingIndex = 0;

                @Override
                public void run(){
                    if(waitingIndex >= waiting.length) waitingIndex = 0;
                    textview.setText(waiting[waitingIndex]);
                    waitingIndex++;
                }
            });
        return this;
    }




    //TODO: implement ability to send multiple commands at once
}
