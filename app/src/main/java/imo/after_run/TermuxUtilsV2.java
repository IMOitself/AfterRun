package imo.after_run;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class TermuxUtilsV2 {
	/** 
	 * must have Termux:API installed and
	 * run this on termux first:

	 pkg install termux-api && echo "allow-external-apps = true" >> "$HOME/.termux/termux.properties"

	 **/
	 
	private static File outputFile = new File("/storage/emulated/0/Download/.afterruntemp");
	
    public static boolean permissionIsGranted(Activity activity){
        return activity.checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED;
    }

	public static void permissionRequest(Activity activity){
        activity.requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"}, 69);
    }
	
	public static void commandRun(String command, Activity activity){
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
			openTermuxAPI(activity);
		}
	}
	
	public static String commandOutputRead(){
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
	
	public static boolean commandOutputExists(){
		return outputFile.exists();
	}
	 
	private static void commandHandleException(Exception e, final Activity activity){
		final LinearLayout layout = new LinearLayout(activity);
		final TextView textView = new TextView(activity);
		final Button button = new Button(activity);

		layout.setOrientation(LinearLayout.VERTICAL);

		textView.setText(e.getMessage());
		textView.setTextIsSelectable(true);

		button.setText("Maybe Open Termux:API first?");
		button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					Exception e = openTermuxAPI(activity);
					if (e != null) textView.setText(e.getMessage());
				}
			});
		layout.addView(textView);
		layout.addView(button);
		activity.setContentView(layout);
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
	
}
