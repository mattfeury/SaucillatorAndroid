package com.mattfeury.saucillator.android.settings;

import java.util.ArrayList;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.android.instruments.InstrumentManager;
import com.mattfeury.saucillator.android.utilities.Utilities;
import com.mattfeury.saucillator.android.utilities.ViewBinders;

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

public class EffectsPreferences extends Activity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.effects_preferences);

    Bundle extras = getIntent().getExtras();
    final int modRate = extras.getInt("modRate", 0);
    final int modDepth = extras.getInt("modDepth", 0);
    final int delay = extras.getInt("delay", 1);
    float lag = extras.getFloat("lag", 0f);

    ComplexOsc osc = ModifyInstrument.modifying;

    // Sliders
    ViewBinders.bindSliderToVariable(this, R.id.modRateSlider, R.id.modRateValue, modRate);
    ViewBinders.bindSliderToVariable(this, R.id.modDepthSlider, R.id.modDepthValue, modDepth);

    ViewBinders.bindSliderToVariable(this, R.id.delaySlider, R.id.delayValue, delay);
    ViewBinders.bindSliderToVariable(this, R.id.lagSlider, R.id.lagValue, lag);
    
    ViewBinders.bindCheckboxToSlider(this, R.id.lfoEnabler, R.id.modRateSlider, R.id.modDepthSlider);
    ViewBinders.bindCheckboxToSlider(this, R.id.delayEnabler, R.id.delaySlider);
    ViewBinders.bindCheckboxToSlider(this, R.id.lagEnabler, R.id.lagSlider);
  }

  public void exit(boolean save) {
    Intent intent = new Intent(EffectsPreferences.this, ModifyInstrument.class);

    if (save) {
      Spinner timbreSpinner = (Spinner) findViewById(R.id.timbreSpinner);
      String timbre = (String)timbreSpinner.getSelectedItem();

      TextView harmonicView = (TextView) findViewById(R.id.harmonicValue);
      String harmonic = (String)harmonicView.getText();

      TextView amplitudeView = (TextView) findViewById(R.id.amplitudeValue);
      String amplitude = (String)amplitudeView.getText();

      TextView phaseView = (TextView) findViewById(R.id.phaseValue);
      String phase = (String)phaseView.getText();

      intent.putExtra("type", timbre);
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
