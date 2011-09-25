package com.mattfeury.saucillator.android;

import com.sauce.touch.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class Settings extends Activity{
	
	SeekBar delaySlider;
	SeekBar lagSlider;
	TextView delayValue;
	TextView lagValue;
	Button saveButton;
	Button cancelButton;
	EditText fileTextBox;
	
	private class DelaySliderListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			delayValue.setText(" " + progress + " ");
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
			lagValue.setText(" " + progress + " ");
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
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class CancelButtonListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		delaySlider = (SeekBar) findViewById(R.id.delaySlider);
		delaySlider.setOnSeekBarChangeListener(new DelaySliderListener());
		lagSlider = (SeekBar) findViewById(R.id.lagSlider);
		lagSlider.setOnSeekBarChangeListener(new LagSliderListener());
		delayValue = (TextView) findViewById(R.id.delayValue);
		lagValue = (TextView) findViewById(R.id.lagValue);
		saveButton = (Button) findViewById(R.id.save);
		saveButton.setOnClickListener(new SaveButtonListener());
		cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new CancelButtonListener());
		fileTextBox = (EditText) findViewById(R.id.fileName);
	}
}
