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

public class CommandTermux {
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
    private Runnable onDetect;
    private Runnable onLoop;
    private Runnable onCancel;
    
    public CommandTermux(String command, Activity mActivity){
        this.command = command;
        this.mActivity = mActivity;
    }
    
    public CommandTermux setOnDetect(Runnable runnable){
        onDetect = runnable;
        return this;
    }
    
    public CommandTermux setOnLoop(Runnable runnable){
        onLoop = runnable;
        return this;
    }
    
    public CommandTermux setOnCancel(Runnable runnable){
        onCancel = runnable;
        return this;
    }
    
    public void run(){
        // starts first to be stop if necessary
        OutputDetector.start(onLoop, onDetect, mActivity);
		
        if(onCancel == null){
            onCancel = new Runnable(){
                @Override
                public void run(){}
            };
        }
        
        run(command, onCancel, mActivity);
    }
	
    
    
    
    
    
	
	public static void run(String command, Runnable onCancel, Activity activity){
		try{
			//this supports multi line commands
			String commandFull = "\n(\n" + command + "\n)";

			//output to a file
			commandFull += "> " + OutputDetector.outputFile.getAbsolutePath();
			commandFull += " 2>&1"; //include error
			
			commandFull += "\necho \"" + COMMAND_END_KEY + "\"";
			commandFull += " >> " + OutputDetector.outputFile.getAbsolutePath();
			
			
			Intent intent = new Intent();
			intent.setClassName("com.termux", "com.termux.app.RunCommandService");
			intent.setAction("com.termux.RUN_COMMAND");
			intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh");
			intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", commandFull});
			intent.putExtra("com.termux.RUN_COMMAND_SHELL_NAME", "After Run");
			if (backgroundMode) intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
			activity.startService(intent);

		}catch(IllegalStateException e){
			//Not allowed to start service Intent...app is in background...
			handleException(e, activity);
			onCancel.run();
            OutputDetector.stop();
		}
	}

	private static void handleException(Exception e, final Activity activity){
		new AlertDialog.Builder(activity)
			.setTitle("Maybe open Termux first?")
			.setMessage(e.getMessage())
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dia, int which) {
					dia.dismiss();
				}
			})
			.setNegativeButton("Open Termux", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dia, int which) {
					try {
                        dia.dismiss();
						openTermux(activity);
                        
					} catch (Exception e) {
						handleException(e, activity);
					}
				}
			})
			.create().show();
	}

	public static void openTermux(final Activity activity) throws Exception{
		String packageName = "com.termux";
		Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
		activity.startActivity(intent);
		Toast.makeText(activity, "Go back to the app again:D..", Toast.LENGTH_LONG).show();
	}

	public static class OutputDetector {

		private static Handler handler;
		private static Runnable fileCheckRunnable;
		private static final int checkIntervalMs = 250;
		private static File outputFile = new File("/storage/emulated/0/Download/.afterruntemp");
		public static String output = "";

		public static void start(final Runnable onLoop, final Runnable onDetect, final Activity activity) {
            onLoop.run();
			handler = new Handler(activity.getMainLooper());
			fileCheckRunnable = new Runnable(){
				@Override
				public void run(){
					if(! outputFile.exists()){
						onLoop.run();
						restart();
						return;
					}
					boolean outputHasEnd = false;
					try {
						BufferedReader isCompleteChecker = new BufferedReader(new FileReader(outputFile));
						String line;
						while ((line = isCompleteChecker.readLine()) != null) {
							if(line.contains((COMMAND_END_KEY))){
								outputHasEnd = true;
								break;
							}
							onLoop.run();
						}
						
						if(! outputHasEnd){
							onLoop.run();
							restart();
							return;
						}
						
						BufferedReader finalReader = new BufferedReader(new FileReader(outputFile));
						String finalLines = "";
						String finalLine;
						while ((finalLine = finalReader.readLine()) != null) {
							if(finalLine.contains(COMMAND_END_KEY)) break;
							finalLines += "\n" + finalLine;
							onLoop.run();
						}
						output = finalLines.trim();
						outputFile.delete();
						onDetect.run();
						output = ""; //clear
						stop();
						
					} catch (IOException e) {
						handleException(e, activity);
						stop();
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

		public static void stop() {
			if (handler != null && fileCheckRunnable != null) 
				handler.removeCallbacks(fileCheckRunnable);
		}
	}
}
