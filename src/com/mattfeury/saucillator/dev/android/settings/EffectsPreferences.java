package com.mattfeury.saucillator.dev.android.settings;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.utilities.ViewBinders;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.SeekBar;
import android.widget.TextView;

public class EffectsPreferences extends Activity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.effects_preferences);

    Bundle extras = getIntent().getExtras();
    final int modRate = extras.getInt("modRate", 0);
    final int modDepth = extras.getInt("modDepth", 0);
    final int delayRate = extras.getInt("delayRate", 0);
    final float delayDecay = extras.getFloat("delayDecay", 0);
    float lag = extras.getFloat("lag", 0f);
    float attack = extras.getFloat("attack", 0f);
    float release = extras.getFloat("release", 0f);

    // Set defaults on view elements
    SeekBar seek = (SeekBar) findViewById(R.id.modRateSlider);
    seek.setMax(SauceEngine.MOD_RATE_MAX);

    seek = (SeekBar) findViewById(R.id.modDepthSlider);
    seek.setMax(SauceEngine.MOD_DEPTH_MAX);

    seek = (SeekBar) findViewById(R.id.delayRateSlider);
    seek.setMax(SauceEngine.DELAY_MAX);

    // Bind Sliders to Values
    ViewBinders.bindSliderToVariable(this, R.id.modRateSlider, R.id.modRateValue, modRate);
    ViewBinders.bindSliderToVariable(this, R.id.modDepthSlider, R.id.modDepthValue, modDepth);

    ViewBinders.bindSliderToVariable(this, R.id.delayRateSlider, R.id.delayRateValue, delayRate, 1);
    ViewBinders.bindSliderToVariable(this, R.id.delayDecaySlider, R.id.delayDecayValue, delayDecay);

    ViewBinders.bindSliderToVariable(this, R.id.lagSlider, R.id.lagValue, lag, 0.99f);

    ViewBinders.bindSliderToVariable(this, R.id.attackSlider, R.id.attackValue, attack, 0.99f);
    ViewBinders.bindSliderToVariable(this, R.id.releaseSlider, R.id.releaseValue, release, 0.99f);

    // Bind Checkboxes to Sliders
    //ViewBinders.bindCheckboxToSlider(this, R.id.lfoEnabler, R.id.modRateSlider, R.id.modDepthSlider);
    //ViewBinders.bindCheckboxToSlider(this, R.id.delayEnabler, R.id.delaySlider);
    //ViewBinders.bindCheckboxToSlider(this, R.id.lagEnabler, R.id.lagSlider);
    //ViewBinders.bindCheckboxToSlider(this, R.id.envelopeEnabler, R.id.attackSlider, R.id.releaseSlider);
  }

  public void exit(boolean save) {
    Intent intent = new Intent(EffectsPreferences.this, ModifyInstrument.class);

    if (save) {
      TextView view = (TextView) findViewById(R.id.modRateValue);
      int modRate = Integer.parseInt((String)view.getText());

      view = (TextView) findViewById(R.id.modDepthValue);
      int modDepth = Integer.parseInt((String)view.getText());

      view = (TextView) findViewById(R.id.delayRateValue);
      int delayRate = Integer.parseInt((String)view.getText());

      view = (TextView) findViewById(R.id.delayDecayValue);
      float delayDecay = Float.parseFloat((String)view.getText());

      view = (TextView) findViewById(R.id.lagValue);
      float lag = Float.parseFloat((String)view.getText());

      view = (TextView) findViewById(R.id.attackValue);
      float attack = Float.parseFloat((String)view.getText());

      view = (TextView) findViewById(R.id.releaseValue);
      float release = Float.parseFloat((String)view.getText());

      intent.putExtra("modRate", modRate);
      intent.putExtra("modDepth", modDepth);
      intent.putExtra("delayRate", delayRate);
      intent.putExtra("delayDecay", delayDecay);
      intent.putExtra("lag", lag);
      intent.putExtra("attack", attack);
      intent.putExtra("release", release);

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
              .setMessage("Save?")
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
