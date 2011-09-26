package com.mattfeury.saucillator.android;

import com.sauce.touch.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {
	
	SeekBar delaySlider;
	SeekBar lagSlider;
	TextView delayValue;
	TextView lagValue;
	Button saveButton;
	Button cancelButton;
	EditText fileTextBox;
	Spinner noteSpinner;
	Spinner octaveSpinner;

  //this is nasty. these are repeated defaults
  private int delayRate = UGen.SAMPLE_RATE;
  private int lag = 0;
  private String fileName = "Recording";
  private int note = 1;
  private int octave = 4;

	private class DelaySliderListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			delayRate = progress;
			delayValue.setText(" " + delayRate + " ");
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
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		Bundle extras = getIntent().getExtras();
		fileName = extras.getString("file name");
		note = extras.getInt("note");
		octave = extras.getInt("octave");
		delayRate = extras.getInt("delay rate");
		lag = extras.getInt("lag");
		
		try {
			delaySlider = (SeekBar) findViewById(R.id.delaySlider);
			delaySlider.setIndeterminate(false);
			delaySlider.setMax(UGen.SAMPLE_RATE);
			delaySlider.setProgress(delayRate);
			delaySlider.setOnSeekBarChangeListener(new DelaySliderListener());
	
			delayValue = (TextView) findViewById(R.id.delayValue);
			delayValue.setText(" " + delayRate);
			
			lagSlider = (SeekBar) findViewById(R.id.lagSlider);
			lagSlider.setIndeterminate(false);
			lagSlider.setMax(100);
			lagSlider.setProgress(lag);
			lagSlider.setOnSeekBarChangeListener(new LagSliderListener());
			

			
			lagValue = (TextView) findViewById(R.id.lagValue);
			lagValue.setText(" " + lag + "%");
			
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
		catch (Exception e) {
			Log.e("settingsCreation", e.toString());
		}
	}
	
	private void saveChanges() {
    	Intent intent = new Intent(Settings.this, TouchTest.class);
    	octave = octaveSpinner.getSelectedItemPosition() + 1;
    	note = noteSpinner.getSelectedItemPosition();
    	fileName = fileTextBox.getText().toString();
    	
    	intent.putExtra("octave", octave);
    	intent.putExtra("note", note);
    	intent.putExtra("file name", fileName);
    	intent.putExtra("delay rate", delayRate);
    	intent.putExtra("lag", lag);
		setResult(0, intent);
		Toast.makeText(this, "Changes Saved.", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private void cancelChanges() {
		Toast.makeText(this, "Changes Discarded.", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		if (id == 0) {
	        dialog = new AlertDialog.Builder(this).setMessage("Save Changes?").setCancelable(true)
		    	.setPositiveButton("Yes",			
				new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
		        		saveChanges();
		        		}
		        	}
		        	).setNegativeButton("No", 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cancelChanges();
						}
					}
		    ).create();
		}
        return dialog;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Just... don't look here.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	showDialog(0);
        }
        return true;
	}
}
