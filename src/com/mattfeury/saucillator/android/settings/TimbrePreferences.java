package com.mattfeury.saucillator.android.settings;

import java.util.ArrayList;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.android.instruments.InstrumentManager;
import com.mattfeury.saucillator.android.utilities.Utilities;

import android.app.Activity;
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

    if (! creating) { //don't allow recursive timbre
      int index = timbres.lastIndexOf(osc.getName());

      if (index > -1)
        timbres.remove(index);
    }

    // Timbre Type
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, timbres);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    timbreSpinner.setAdapter(adapter);
    
    int index = timbres.lastIndexOf(type);
    if (index > -1)
      timbreSpinner.setSelection(index);

    // Sliders
    bindSliderToVariable(R.id.harmonicSlider, R.id.harmonicValue, harmonic);
    bindSliderToVariable(R.id.amplitudeSlider, R.id.amplitudeValue, amplitude, 2f);
    bindSliderToVariable(R.id.phaseSlider, R.id.phaseValue, phase);

  }

  public void exit() {
    Intent intent = new Intent(TimbrePreferences.this, ModifyTimbre.class);

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
		finish();
  }
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Exit button
    if (keyCode == KeyEvent.KEYCODE_BACK)
      exit();

    return true;
  }

  public void bindSliderToVariable(int sliderId, int textId, int progress) {
    SeekBar slider = (SeekBar) findViewById(sliderId);
    slider.setIndeterminate(false);
    slider.setProgress(progress);
    final TextView value = (TextView) findViewById(textId);
    value.setText(""+progress);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int change, boolean fromUser) {
        value.setText(""+change);
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });    
  }
  public void bindSliderToVariable(int sliderId, int textId, float percent, final float max) {
    SeekBar slider = (SeekBar) findViewById(sliderId);
    slider.setIndeterminate(false);
    slider.setProgress((int) ((percent / max) * 100));
    final TextView value = (TextView) findViewById(textId);
    value.setText(""+percent);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        value.setText(""+(progress * max / 100f));
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });    
  }

}
