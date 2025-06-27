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
	TextView outputTxt;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
        CommandTermux.checkAndRequestPermissions(this);

		commandEdittext = findViewById(R.id.command_edittext);
		commandRunBtn = findViewById(R.id.command_run_btn);
		outputTxt = findViewById(R.id.output_txt);
		outputTxt.setMovementMethod(new ScrollingMovementMethod());

		commandRunBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					onButtonClicked();
				}
			});
    }
	
	void onButtonClicked(){
		commandRunBtn.setEnabled(false);
		String command = commandEdittext.getText().toString().trim();
		
        new CommandTermuxV2(command, MainActivity.this).start();
	}
}
