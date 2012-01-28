package com.mattfeury.saucillator.android.settings;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.R.layout;
import com.mattfeury.saucillator.android.R.xml;
import com.mattfeury.saucillator.android.instruments.ComplexOsc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ModifyInstrument extends PreferenceActivity {

  private ComplexOsc modifying;
  private ComplexOsc loaded;

  private boolean creating = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.modify_instrument_preferences);
    setContentView(R.layout.modify_instrument);

    SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    SharedPreferences.Editor editor = customSharedPreference.edit();

    // Create or modify?
    Bundle extras = getIntent().getExtras();
    try {
      ComplexOsc osc = (ComplexOsc)extras.get("osc");
      modifying = osc;
      //loaded = osc.clone(); //FIXME
    } catch(Exception e) {
      // This is a new instrument. Do something else
      creating = true;
    }

    // Set default values
    if (! creating) {
      editor.putString("namePref", modifying.getName());
      editor.commit();
    }

    // Bind FX, timbre handlers
    Preference timbrePref = (Preference) findPreference("timbrePref");
    timbrePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        Toast.makeText(getBaseContext(),
                        "The custom preference has been clicked",
                        Toast.LENGTH_LONG).show();
        return true;
      }
    });
    Preference fxPref = (Preference) findPreference("effectsPref");
    fxPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        Toast.makeText(getBaseContext(),
                        "The custom preference has been clicked",
                        Toast.LENGTH_LONG).show();
        return true;
      }
    });
    
  }

  private void exit() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    String name = prefs.getString("namePref", "");
    
  }

}
