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
		
        new CommandTermux(command, MainActivity.this)
            .setOnCancel(new Runnable(){// this runs if sending command to termux encounter an error
                @Override
                public void run(){
                    CommandTermux.stopDetector(); // still waits for output and should be stopped
                    commandRunBtn.setEnabled(true);
                    outputTxt.setText("");
                }
            })
        .setOnLoop(new Runnable(){
                String[] waiting = {"waiting.", "waiting..", "waiting..."};
                int waitingIndex = 0;

                @Override
                public void run(){
                    if(waitingIndex >= waiting.length) waitingIndex = 0;
                    outputTxt.setText(waiting[waitingIndex]);
                    waitingIndex++;
                }
            })
        .setOnDetect(new Runnable(){
                @Override
                public void run(){
                    commandRunBtn.setEnabled(true);
                    outputTxt.setText(CommandTermux.getOutput());
                    if (! CommandTermux.backgroundMode) instruction.setVisibility(View.GONE);
                }
            })
        .run();
	}
}
