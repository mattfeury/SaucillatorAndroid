package com.mattfeury.saucillator.android;

import com.sauce.touch.R;

import android.app.Activity;
import android.app.PendingIntent.OnFinished;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class Settings extends Activity{
	
	SeekBar delaySlider;
	SeekBar lagSlider;
	TextView delayValue;
	TextView lagValue;
	Button saveButton;
	Button cancelButton;
	EditText fileTextBox;
	Spinner noteSpinner;
	Spinner octaveSpinner;
	
	private int sampleRate = UGen.SAMPLE_RATE;
    private int lag = 0;
    private String fileName = "Recording";
    private int note = 1;
    private int octave = 4;
	
	private class DelaySliderListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			sampleRate = progress;
			delayValue.setText(" " + sampleRate + " ");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class LagSliderListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			lag = progress;
			lagValue.setText(" " + lag + "% ");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class SaveButtonListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
	    	Intent intent = new Intent(Settings.this, TouchTest.class);
	    	octave = octaveSpinner.getSelectedItemPosition() + 1;
	    	note = noteSpinner.getSelectedItemPosition();
	    	fileName = fileTextBox.getText().toString();
	    	
	    	intent.putExtra("octave", octave);
	    	intent.putExtra("note", note);
	    	intent.putExtra("file name", fileName);
	    	intent.putExtra("sample rate", sampleRate);
	    	intent.putExtra("lag", lag);
			setResult(0, intent);
			finish();
		}
		
	}
	
	private class CancelButtonListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			finish();
		}

	}
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		Bundle extras = getIntent().getExtras();
		fileName = extras.getString("file name");
		note = extras.getInt("note");
		octave = extras.getInt("octave");
		sampleRate = extras.getInt("sample rate");
		lag = extras.getInt("lag");
		
		delaySlider = (SeekBar) findViewById(R.id.delaySlider);
		delaySlider.setOnSeekBarChangeListener(new DelaySliderListener());
		delaySlider.setIndeterminate(false);
		delaySlider.setMax(UGen.SAMPLE_RATE);
		

		delayValue = (TextView) findViewById(R.id.delayValue);
		delayValue.setText(" " + sampleRate);
		
		lagSlider = (SeekBar) findViewById(R.id.lagSlider);
		lagSlider.setOnSeekBarChangeListener(new LagSliderListener());
		
		lagValue = (TextView) findViewById(R.id.lagValue);
		lagValue.setText(" " + lag + "%");
		
		saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new SaveButtonListener());
		cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new CancelButtonListener());
		
		fileTextBox = (EditText) findViewById(R.id.fileName);
		fileTextBox.setText(fileName);
		
		noteSpinner = (Spinner) findViewById(R.id.noteChooser);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.notes_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    noteSpinner.setAdapter(adapter);
	    noteSpinner.setSelection(note);
	    
		adapter = ArrayAdapter.createFromResource(this, R.array.octave_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		octaveSpinner = (Spinner) findViewById(R.id.octaveChooser);
		octaveSpinner.setAdapter(adapter);
		octaveSpinner.setSelection(octave - 1);
	}
	
}
