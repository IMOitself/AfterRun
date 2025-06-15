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
		
        CommandTermux.checkAndRequestPermissions(this);

		commandEdittext = findViewById(R.id.command_edittext);
		commandRunBtn = findViewById(R.id.command_run_btn);
		instruction = findViewById(R.id.instruction); // optional. its just to show user what to do
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

		// this runs if sending command to termux encounter an error
		Runnable onCancel = new Runnable(){
			@Override
			public void run(){
				CommandTermux.OutputDetector.stop(); // still waits for output and should be stopped
				commandRunBtn.setEnabled(true);
				outputTxt.setText("");
			}
		};

		// optional. this runs while detecting command output
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
		
		// this runs after command return the output
		Runnable onDetect = new Runnable(){
			@Override
			public void run(){
				commandRunBtn.setEnabled(true);
				outputTxt.setText(CommandTermux.OutputDetector.output);
				if (! CommandTermux.backgroundMode) instruction.setVisibility(View.GONE);
			}
		};
		
		// start detector first so that it can be stopped by onCancel
		CommandTermux.OutputDetector.start(onLoop, onDetect, MainActivity.this); // starts first to be stop if necessary
		CommandTermux.run(command, onCancel, MainActivity.this);
	}
}
