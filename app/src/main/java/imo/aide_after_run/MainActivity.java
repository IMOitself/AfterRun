package imo.aide_after_run;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.TextView;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(! hasStoragePermission()){
            requestStoragePermission();
            finish();
            return;
        }

        if(! hasTermuxPermission()){
            requestTermuxPermission();
            finish();
            return;
        }
		
        /** 
         * must have Termux:API installed
         * must run this on termux first:
        
         pkg install termux-api && echo "allow-external-apps = true" >> "$HOME/.termux/termux.properties"

        **/
        try{
            String script = "echo hello!";
            script += "\nread a"; //this ensures termux do not exit immediately
            
            Intent intent = new Intent();
            intent.setClassName("com.termux", "com.termux.app.RunCommandService");
            intent.setAction("com.termux.RUN_COMMAND");
            intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh");
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", script});
            startService(intent);
        }catch(Exception e){
            TextView textView = new TextView(this);
            textView.setText(e.getMessage());
            textView.setTextIsSelectable(true);
            setContentView(textView);
        }
    }
    
    boolean hasTermuxPermission(){
        return checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED;
    }

    void requestTermuxPermission(){
        requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"}, 69);
    }

    boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {
			requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }
}
