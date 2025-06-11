package imo.after_run;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class TermuxUtils {
	/** 
	 * must have Termux:API installed and
	 * run this on termux first:
	 
	 pkg install termux-api && echo "allow-external-apps = true" >> "$HOME/.termux/termux.properties"
	 
	 **/
	
    /**
	  * must have this on onCreate() of the activity:
	
	 if (! TermuxUtils.hasTermuxPermission(this)) {
	 	TermuxUtils.requestTermuxPermission(this);
	 }
	
	**/
    public static boolean hasTermuxPermission(Activity activity){
        return activity.checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED;
    }
    
	public static void requestTermuxPermission(Activity activity){
        activity.requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"}, 69);
    }
	/**
	 * sample usage:

	 TermuxUtils.openTermuxAPI(MainActivity.this);
	 
	 * need to open Termux:API first if runCommand() cant start service

	 **/
	public static Exception openTermuxAPI(Activity activity){
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.termux.api", "com.termux.api.activities.TermuxAPIMainActivity"));
			activity.startActivity(intent);
			Toast.makeText(activity, "Go back to the app again:D", Toast.LENGTH_LONG).show();
			
		} catch (Exception e) {
			return e;
		}
		return null;
	}
	/**
	 * sample usage:

	 IllegalStateException e = TermuxUtils.runCommand("echo hello", outputFile, MainActivity.this);
	 if (e != null) // handle exception. maybe prompt to do openTermuxAPI()

	 * must handle error if not allowed to start service
	 * outputFile will be deleted so be careful
	 
	 **/
	public static IllegalStateException runCommand(String command, File outputFile, Activity activity){
		try{
			//this supports multi line commands
			String commandFull = "\n(\n" + command + "\n)";

			//output to a file
			commandFull += "> " + outputFile.getAbsolutePath();

			Intent intent = new Intent();
			intent.setClassName("com.termux", "com.termux.app.RunCommandService");
			intent.setAction("com.termux.RUN_COMMAND");
			intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh");
			intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", commandFull});
			activity.startService(intent);

		}catch(IllegalStateException e){
			//Not allowed to start service Intent...app is in background...
			return e;
		}
		return null;
	}
	/**
	 * sample usage:

	 String content = TermuxUtils.readCommandOutput(outputFile);
	 
	 * must be the same outputFile used in runCommand()
	 * outputFile will be deleted so be careful

	 **/
	public static String readCommandOutput(File outputFile){
		// read command output from file and delete it
		StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(outputFile));
            String line;

            while((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
			outputFile.delete();
        } catch(Exception e) { return e.getMessage(); }
		return content.toString();
	}
}
