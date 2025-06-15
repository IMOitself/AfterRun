package imo.after_run;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity 
{
	EditText commandEdittext;
	Button commandRunBtn;
	ViewGroup instruction;
	TextView outputTxt;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
        if(! CommandTermux.hasStoragePermission(this)){
            CommandTermux.requestStoragePermission(this);
            finish();
            return;
        }

		if(! CommandTermux.permissionIsGranted(this)){
			CommandTermux.permissionRequest(this);
			finish();
            return;
		}

		commandEdittext = findViewById(R.id.command_edittext);
		commandRunBtn = findViewById(R.id.command_run_btn);
		instruction = findViewById(R.id.instruction);
		outputTxt = findViewById(R.id.output_txt);
		outputTxt.setMovementMethod(new ScrollingMovementMethod());

		instruction.setVisibility(View.GONE);
		commandRunBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					onButtonClicked();
				}
			});
    }
	
	void onButtonClicked(){
		commandRunBtn.setEnabled(false);
		if (! CommandTermux.backgroundMode) instruction.setVisibility(View.VISIBLE);
		String command = commandEdittext.getText().toString().trim();

		Runnable onCancel = new Runnable(){
			@Override
			public void run(){
				commandRunBtn.setEnabled(true);
				CommandTermux.OutputDetector.stop();
				outputTxt.setText("");
			}
		};

		Runnable onLoop = new Runnable(){
			String[] waiting = {"waiting.", "waiting..", "waiting..."};
			int waitingIndex = 0;

			@Override
			public void run(){
				if(waitingIndex >= waiting.length) waitingIndex = 0;
				outputTxt.setText(waiting[waitingIndex]);
				waitingIndex++;
			}
		};
		Runnable onDetect = new Runnable(){
			@Override
			public void run(){
				commandRunBtn.setEnabled(true);
				if (! CommandTermux.backgroundMode) instruction.setVisibility(View.GONE);
				outputTxt.setText(CommandTermux.OutputDetector.output);
			}
		};
		CommandTermux.OutputDetector.start(onLoop, onDetect, MainActivity.this); // starts first to be stop if necessary
		CommandTermux.run(command, onCancel, MainActivity.this);
	}
}
