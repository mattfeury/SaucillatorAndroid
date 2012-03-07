package com.mattfeury.saucillator.dev.android.settings;

import java.util.ArrayList;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.instruments.InstrumentManager;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;
import com.mattfeury.saucillator.dev.android.utilities.ViewBinders;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class TimbrePreferences extends Activity {
  private int timbrePosition;
  private boolean createNew;
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.timbre_preferences);

    // Create or modify?
    Bundle extras = getIntent().getExtras();
    timbrePosition = extras.getInt("timbrePosition");
    boolean creating = extras.getBoolean("creating");
    createNew = extras.getBoolean("createNew");
    String type = extras.getString("type");
    final int harmonic = extras.getInt("harmonic", 1);
    float amplitude = extras.getFloat("amplitude", 1.0f);
    int phase = extras.getInt("phase", 0);

    ComplexOsc osc = ModifyInstrument.modifying;

    Spinner timbreSpinner = (Spinner) findViewById(R.id.timbreSpinner);
    ArrayList<String> timbres = InstrumentManager.getAllInstrumentNames(getAssets());

    if (! creating && ! osc.isInternal()) { //don't allow recursive timbre
      int index = timbres.lastIndexOf(osc.getName());

      if (index > -1)
        timbres.remove(index);
    }

    // Timbre Type
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, timbres);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    timbreSpinner.setAdapter(adapter);
    
    if (! createNew) {
      int index = timbres.lastIndexOf(type);
      if (index > -1)
        timbreSpinner.setSelection(index);
    }

    // Sliders
    ViewBinders.bindSliderToVariable(this, R.id.harmonicSlider, R.id.harmonicValue, harmonic, 1);
    ViewBinders.bindSliderToVariable(this, R.id.amplitudeSlider, R.id.amplitudeValue, amplitude, 2f);
    ViewBinders.bindSliderToVariable(this, R.id.phaseSlider, R.id.phaseValue, phase);
  }

  public void exit(boolean save) {
    Intent intent = new Intent(TimbrePreferences.this, ModifyTimbre.class);

    if (save) {
      Spinner timbreSpinner = (Spinner) findViewById(R.id.timbreSpinner);
      String timbre = (String)timbreSpinner.getSelectedItem();

      TextView harmonicView = (TextView) findViewById(R.id.harmonicValue);
      String harmonic = (String)harmonicView.getText();

      TextView amplitudeView = (TextView) findViewById(R.id.amplitudeValue);
      String amplitude = (String)amplitudeView.getText();

      TextView phaseView = (TextView) findViewById(R.id.phaseValue);
      String phase = (String)phaseView.getText();

      intent.putExtra("createNew", createNew);
      intent.putExtra("type", timbre);
      intent.putExtra("timbrePosition", timbrePosition);
      intent.putExtra("harmonic", Integer.parseInt(harmonic));
      intent.putExtra("amplitude", Float.parseFloat(amplitude));
      intent.putExtra("phase", Integer.parseInt(phase));

      setResult(0, intent);
    } else {
      setResult(1, intent);
    }

		finish();
  }
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Exit button
    if (keyCode == KeyEvent.KEYCODE_BACK)
      showDialog(0);

    return true;
  }
  protected Dialog onCreateDialog(int id) {
    Dialog dialog = null;
    if (id == 0) {
      dialog =
        new AlertDialog.Builder(this)
              .setMessage("Save Changes?")
              .setCancelable(true)
              .setPositiveButton("Yes",
                  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                      exit(true);
                    }
              })
              .setNegativeButton("No",
                  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      exit(false);
                    }
              })
              .create();
    }
    return dialog;
  }
}
