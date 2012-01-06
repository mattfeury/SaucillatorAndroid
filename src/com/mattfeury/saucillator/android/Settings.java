package com.mattfeury.saucillator.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Settings extends Activity {

  EditText fileTextBox;
  Spinner noteSpinner;
  Spinner octaveSpinner;
  ToggleButton visualsToggle;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    Bundle extras = getIntent().getExtras();
    String fileName = extras.getString("file name");
    int note = extras.getInt("note");
    int octave = extras.getInt("octave");
    boolean visuals = extras.getBoolean("visuals");

    try {
      visualsToggle = (ToggleButton) findViewById(R.id.visualsToggler);
      visualsToggle.setChecked(visuals);

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
    Intent intent = new Intent(Settings.this, SauceEngine.class);
    int octave = octaveSpinner.getSelectedItemPosition() + 1;
    int note = noteSpinner.getSelectedItemPosition();
    String fileName = fileTextBox.getText().toString();
    boolean visuals = visualsToggle.isChecked();

    intent.putExtra("octave", octave);
    intent.putExtra("note", note);
    intent.putExtra("file name", fileName);
    intent.putExtra("visuals", visuals);
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
