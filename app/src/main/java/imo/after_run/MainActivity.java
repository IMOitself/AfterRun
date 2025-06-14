package imo.after_run;

import android.app.Activity;
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

        if(! hasStoragePermission()){
            requestStoragePermission();
            finish();
            return;
        }

		if(! TermuxUtilsV2.permissionIsGranted(this)){
			TermuxUtilsV2.permissionRequest(this);
			finish();
            return;
		}

		final EditText commandEdittext = findViewById(R.id.command_edittext);
		commandRunBtn = findViewById(R.id.command_run_btn);
		instruction = findViewById(R.id.instruction);
		outputTxt = findViewById(R.id.output_txt);
		outputTxt.setMovementMethod(new ScrollingMovementMethod());

		instruction.setVisibility(View.GONE);
		commandRunBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					commandRunBtn.setEnabled(false);
					if (! CommandTermux.backgroundMode) instruction.setVisibility(View.VISIBLE);
					String command = commandEdittext.getText().toString().trim();
					
					Runnable onCancel = new Runnable(){
						@Override
						public void run(){
							commandRunBtn.setEnabled(true);
						}
					};
					
					CommandTermux.run(command, onCancel, MainActivity.this);
					
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
					CommandTermux.OutputDetector.start(onLoop, onDetect, MainActivity.this);
				}
			});
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
