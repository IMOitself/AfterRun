package imo.after_run;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CommandTermux {
	/** 
	 * must have Termux:API installed and
	 * run this on termux first:

	 pkg install termux-api
	 sed -i 's/# allow-external-apps = true/allow-external-apps = true/g' ~/.termux/termux.properties
	 termux-setup-storage

	 **/

	public static boolean permissionIsGranted(Activity activity){
        return activity.checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED;
    }

	public static void permissionRequest(Activity activity){
        activity.requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"}, 69);
    }

	public static void run(String command, Activity activity){
		try{
			//this supports multi line commands
			String commandFull = "\n(\n" + command + "\n)";

			//output to a file
			commandFull += "> " + OutputDetector.outputFile.getAbsolutePath();

			Intent intent = new Intent();
			intent.setClassName("com.termux", "com.termux.app.RunCommandService");
			intent.setAction("com.termux.RUN_COMMAND");
			intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh");
			intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", commandFull});
			intent.putExtra("com.termux.RUN_COMMAND_SHELL_NAME", "After Run");
			activity.startService(intent);

		}catch(IllegalStateException e){
			//Not allowed to start service Intent...app is in background...
			handleException(e, activity);
		}
	}

	private static void handleException(Exception e, final Activity activity){
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

	public static class OutputDetector {

		private static Handler handler;
		private static Runnable fileCheckRunnable;
		private static final int checkIntervalMs = 500;
		private static File outputFile = new File("/storage/emulated/0/Download/.afterruntemp");
		public static String output = "";

		public static void start(final Runnable onLoop, final Runnable onDetect, final Activity activity) {
			handler = new Handler(activity.getMainLooper());
			fileCheckRunnable = new Runnable(){
				@Override
				public void run(){
					if(! outputFile.exists()){
						restart();
						onLoop.run();
						return;
					}
					try {
						BufferedReader reader = new BufferedReader(new FileReader(outputFile));
						String line;
						while ((line = reader.readLine()) != null) {
							output += "\n" + line;
							onLoop.run();
						}
						output = output.trim();
					} catch (IOException e) {
						handleException(e, activity);
						stop();
					}

					if (! output.isEmpty()){
						outputFile.delete();
						onDetect.run();
						output = ""; //clear
						stop();
					}else{
						restart();
					}
				}
			};

			// Start the first check immediately.
			handler.post(fileCheckRunnable);
		}

		public static void start(final Runnable onDetect, final Activity activity) {
			Runnable onLoop = new Runnable(){
				@Override
				public void run(){}
			};
			start(onLoop, onDetect, activity);
		}

		private static void restart(){
			handler.postDelayed(fileCheckRunnable, checkIntervalMs);
		}

		/**
		 * Stops the periodic file checking.
		 */
		public static void stop() {
			if (handler != null && fileCheckRunnable != null) 
				handler.removeCallbacks(fileCheckRunnable);
		}
	}


}
