package imo.after_run;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MainActivity extends Activity 
{
	File outputFile = new File("/storage/emulated/0/Download/.afterruntemp");
	ViewGroup instruction;
	TextView outputTxt;
	
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
         * must have Termux:API installed and
         * run this on termux first:
		   pkg install termux-api && echo "allow-external-apps = true" >> "$HOME/.termux/termux.properties"
        **/
		
		final EditText commandEdittext = findViewById(R.id.command_edittext);
		final Button commandRunBtn = findViewById(R.id.command_run_btn);
		instruction = findViewById(R.id.instruction);
		outputTxt = findViewById(R.id.output_txt);
		outputTxt.setMovementMethod(new ScrollingMovementMethod());
		
		instruction.setVisibility(View.GONE);
		commandRunBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					instruction.setVisibility(View.VISIBLE);
					try{
						String command = commandEdittext.getText().toString().trim();
						
						//this supports multi line commands
						String commandFull = "\n(\n" + command + "\n)";

						//output to a file
						commandFull += "> " + outputFile.getAbsolutePath();

						Intent intent = new Intent();
						intent.setClassName("com.termux", "com.termux.app.RunCommandService");
						intent.setAction("com.termux.RUN_COMMAND");
						intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh");
						intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", commandFull});
						startService(intent);

					}catch(IllegalStateException e){
						//Not allowed to start service Intent...app is in background...
						handleIllegalStateException(e);
					}
				}
			});
    }

	
	@Override
	protected void onResume() {
		super.onResume();
		if(! outputFile.exists()) return;
		
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
        } catch(Exception e) { handleException(e); }
		
		if(content.toString().trim().isEmpty()) return;
		
		instruction.setVisibility(View.GONE);
		outputTxt.setText(content.toString());
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
	
	void handleException(Exception e){
		final TextView textView = new TextView(this);
		textView.setText(e.getMessage());
		setContentView(textView);
	}
	
	void handleIllegalStateException(Exception e){
		final LinearLayout layout = new LinearLayout(this);
		final TextView textView = new TextView(this);
		final Button button = new Button(this);
		
		layout.setOrientation(LinearLayout.VERTICAL);
		
		textView.setText(e.getMessage());
		textView.setTextIsSelectable(true);
		
		button.setText("Maybe Open Termux:API first?");
		button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					try {
						Intent intent = new Intent();
						intent.setComponent(new ComponentName("com.termux.api", "com.termux.api.activities.TermuxAPIMainActivity"));
						startActivity(intent);
						Toast.makeText(MainActivity.this, "Go back to the app again:D", Toast.LENGTH_LONG).show();
						//finish();
					} catch (Exception e) {
						textView.setText(e.getMessage());
					}
				}
			});
		layout.addView(textView);
		layout.addView(button);
		setContentView(layout);
	}
}
