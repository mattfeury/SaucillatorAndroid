package com.mattfeury.saucillator.android.settings;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.SauceEngine;
import com.mattfeury.saucillator.android.R.layout;
import com.mattfeury.saucillator.android.R.xml;
import com.mattfeury.saucillator.android.instruments.*;
import com.mattfeury.saucillator.android.sound.WavWriter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

public class ModifyInstrument extends PreferenceActivity {

  // Static so we don't have to pass it serialized to
  // the edit timbre / fx views
  public static ComplexOsc modifying;
  //private ComplexOsc loaded;
  public static boolean creating = true;
  
  private final static int timbreActivity = 0;
  private final static int fxActivity = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.modify_instrument_preferences);
    //setContentView(R.layout.modify_instrument);

    // Create or modify?
    Bundle extras = getIntent().getExtras();
    creating = extras.getBoolean("createNew");

    // Set default values
    if (creating) {
      modifying = new ComplexOsc();
    } else {
      modifying = SauceEngine.getCurrentOscillator();
      EditTextPreference namePref = (EditTextPreference) findPreference("namePref");
 
      namePref.setText(modifying.getName());
      namePref.setSummary(modifying.getName());
    }

    // Bind FX, timbre handlers
    Preference timbrePref = (Preference) findPreference("timbrePref");
    timbrePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(ModifyInstrument.this, ModifyTimbre.class);
        startActivityForResult(intent, timbreActivity);
        return true;
      }
    });
    Preference fxPref = (Preference) findPreference("effectsPref");
    fxPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(ModifyInstrument.this, EffectsPreferences.class);
        intent.putExtra("modRate", modifying.getModRate());
        intent.putExtra("modDepth", modifying.getModDepth());
        intent.putExtra("delayRate", modifying.getDelayRate());
        intent.putExtra("delayDecay", modifying.getDelayDecay());
        intent.putExtra("lag", modifying.getLag());
        intent.putExtra("attack", modifying.getAttack());
        intent.putExtra("release", modifying.getRelease());

        startActivityForResult(intent, fxActivity);
        return true;
      }
    }); 
  }
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // resultCode of 0 means save. TODO remove magic number
    if (requestCode == fxActivity && resultCode == 0 && data != null) {
      Bundle extras = data.getExtras();
      
      final int modRate = extras.getInt("modRate", 0);
      final int modDepth = extras.getInt("modDepth", 0);
      final int delayRate = extras.getInt("delayRate", 0);
      final float delayDecay = extras.getFloat("delayDecay", 0);
      float lag = extras.getFloat("lag", 0f);
      float attack = extras.getFloat("attack", 0f);
      float release = extras.getFloat("release", 0f);

      modifying.setModRate(modRate);
      modifying.setModDepth(modDepth);
      modifying.setDelayRate(delayRate);
      modifying.setDelayDecay(delayDecay);
      modifying.setLag(lag);
      modifying.setAttack(attack);
      modifying.setRelease(release);
    }
  }


  public void exit() {
    Intent intent = new Intent(ModifyInstrument.this, SauceEngine.class);
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
}
