package com.mattfeury.saucillator.android.settings;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.SauceEngine;
import com.mattfeury.saucillator.android.instruments.Theory;
import com.mattfeury.saucillator.android.instruments.Theory.Scale;
import com.mattfeury.saucillator.android.utilities.ViewBinders;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Settings extends Activity {

  EditText fileTextBox;
  Spinner noteSpinner, octaveSpinner, scaleSpinner;
  ToggleButton visualsToggle;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    Bundle extras = getIntent().getExtras();
    String fileName = extras.getString("file name");
    int note = extras.getInt("note");
    int octave = extras.getInt("octave");
    boolean visuals = extras.getBoolean("visuals");
    String scale = extras.getString("scale");

    try {
      visualsToggle = (ToggleButton) findViewById(R.id.visualsToggler);
      visualsToggle.setChecked(visuals);

      fileTextBox = (EditText) findViewById(R.id.fileName);
      fileTextBox.setText(fileName);

      SeekBar slider = (SeekBar) findViewById(R.id.padSizeSlider);
      slider.setMax(SauceEngine.TRACKPAD_SIZE_MAX);
      ViewBinders.bindSliderToVariable(this, R.id.padSizeSlider, R.id.padSizeValue, SauceEngine.TRACKPAD_GRID_SIZE, 1);

      noteSpinner = (Spinner) findViewById(R.id.noteChooser);
      ArrayAdapter<CharSequence> noteAdapter = ArrayAdapter.createFromResource(this, R.array.notes_array, android.R.layout.simple_spinner_item);
      noteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      noteSpinner.setAdapter(noteAdapter);
      noteSpinner.setSelection(note);

      ArrayAdapter<CharSequence> octiveAdapter = ArrayAdapter.createFromResource(this, R.array.octave_array, android.R.layout.simple_spinner_item);
      octiveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      octaveSpinner = (Spinner) findViewById(R.id.octaveChooser);
      octaveSpinner.setAdapter(octiveAdapter);
      octaveSpinner.setSelection(octave - 1);

      scaleSpinner = (Spinner) findViewById(R.id.scaleChooser);
      ArrayAdapter<CharSequence> scaleAdapter = ArrayAdapter.createFromResource(this, R.array.scale_array, android.R.layout.simple_spinner_item);
      scaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      int selectedScale = scaleAdapter.getPosition(scale);
      scaleSpinner.setAdapter(scaleAdapter);
      scaleSpinner.setSelection(selectedScale);      
		}
    catch (Exception e) {
      Log.e("settingsCreation", e.toString());
    }
  }

  private void saveChanges() {
    Intent intent = new Intent(Settings.this, SauceEngine.class);
    int octave = octaveSpinner.getSelectedItemPosition() + 1;
    int note = noteSpinner.getSelectedItemPosition();
    String fileName = fileTextBox.getText().toString();
    boolean visuals = visualsToggle.isChecked();
    String scale = (String) scaleSpinner.getSelectedItem();
    
    TextView padSizeText = (TextView) findViewById(R.id.padSizeValue);
    int padSize = Integer.parseInt((String)padSizeText.getText());
    SauceEngine.TRACKPAD_GRID_SIZE = padSize;

    intent.putExtra("octave", octave);
    intent.putExtra("note", note);
    intent.putExtra("file name", fileName);
    intent.putExtra("visuals", visuals);
    intent.putExtra("scale", scale);
    setResult(0, intent);

		Toast.makeText(this, "Changes Saved.", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Exit button
    if (keyCode == KeyEvent.KEYCODE_BACK)
      saveChanges();
    
    return true;
	}
}
