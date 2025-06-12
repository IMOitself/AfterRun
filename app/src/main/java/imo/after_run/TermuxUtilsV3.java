package imo.after_run;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class TermuxUtilsV3 {
	
    private static String TERMUX_EXTRA_KEY = "TERMUX_EXTRA_KEY";

    public static boolean permissionIsGranted(Activity activity){
        return activity.checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED;
    }

	public static void permissionRequest(Activity activity){
        activity.requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"}, 69);
    }
	
    public static void commandRun(String command, Activity activity){
		try{
			//this supports multi line commands
			String commandFull = "command=$(\n" + command + "\n)";
			
			commandFull += "\nam start -n ";
			commandFull += activity.getPackageName() + "/" + activity.getClass().getName();
			commandFull += " --es \""+TERMUX_EXTRA_KEY+"\" \"$command\"";
			
			Intent intent = new Intent();
			intent.setClassName("com.termux", "com.termux.app.RunCommandService");
			intent.setAction("com.termux.RUN_COMMAND");
			intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh");
			intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", commandFull});
			intent.putExtra("com.termux.RUN_COMMAND_SHELL_NAME", "After Run");
			activity.startService(intent);

		}catch(IllegalStateException e){
			//Not allowed to start service Intent...app is in background...
			commandHandleException(e, activity);
		}
	}
	
	public static String commandOutputGet(Activity activity){
		Intent intent = activity.getIntent();
		String receivedText = "";
		if (intent == null) receivedText = "intent null";
		if (! intent.hasExtra(TERMUX_EXTRA_KEY)) receivedText = "no termux key";
		if (receivedText.isEmpty()) receivedText = intent.getStringExtra(TERMUX_EXTRA_KEY);
		return receivedText;
	}

	
	private static void commandHandleException(Exception e, final Activity activity){
		final LinearLayout layout = new LinearLayout(activity);
		final TextView textView = new TextView(activity);
		final Button button = new Button(activity);

		layout.setOrientation(LinearLayout.VERTICAL);

		textView.setText(e.getMessage());
		textView.setTextIsSelectable(true);

		button.setText("Maybe Open Termux first?");
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
			intent.setComponent(new ComponentName("com.termux", "com.termux.app.TermuxActivity"));
			activity.startActivity(intent);
			Toast.makeText(activity, "Go back to the app again:D", Toast.LENGTH_LONG).show();
			activity.finishAffinity();
		} catch (Exception e) {
			return e;
		}
		return null;
	}
}
