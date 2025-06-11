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
		
        /** 
         * must have Termux:API installed and
         * run this on termux first:
		   pkg install termux-api && echo "allow-external-apps = true" >> "$HOME/.termux/termux.properties"
        **/
		
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
					instruction.setVisibility(View.VISIBLE);
					String command = commandEdittext.getText().toString().trim();
					TermuxUtilsV2.commandRun(command, MainActivity.this);
				}
			});
    }

	
	@Override
	protected void onResume() {
		super.onResume();
		if(! TermuxUtilsV2.commandOutputExists()) return;
		
		String content = TermuxUtilsV2.commandOutputRead();
		
		if(content.toString().trim().isEmpty()) return;
		
		commandRunBtn.setEnabled(true);
		instruction.setVisibility(View.GONE);
		outputTxt.setText(content.toString());
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
