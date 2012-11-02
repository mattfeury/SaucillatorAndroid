package com.mattfeury.saucillator.dev.android.settings;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.*;
import com.mattfeury.saucillator.dev.android.services.InstrumentService;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.utilities.Box;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyInstrument extends PreferenceActivity {

  // Static so we don't have to pass it serialized to
  // the edit timbre / fx views
  public static ComplexOsc modifying;
  //private ComplexOsc loaded;
  public static boolean creating = true;
  
  private final static int timbreActivity = 0;
  private final static int fxActivity = 1;

  private static final int deleteConfirmDialog = 0,
                           deletedInfoDialog = 1,
                           saveConfirmDialog = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.modify_instrument_preferences);

    // Create or modify?
    Bundle extras = getIntent().getExtras();
    creating = extras.getBoolean("createNew");

    // Set default values
    if (creating) {
      modifying = new ComplexOsc();
      requestName();
    } else {
      modifying = AudioEngine.getCurrentOscillator(); 
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
    
    // Maintance stuff
    Preference savePref = (Preference) findPreference("savePref");
    Preference revertPref = (Preference) findPreference("revertPref");
    Preference deletePref = (Preference) findPreference("deletePref");

    if (modifying.isInternal()) {
      savePref.setEnabled(false);
      savePref.setSummary("Aw naw! Internal instruments cannot be overwritten. Create one instead.");

      deletePref.setEnabled(false);
      deletePref.setSummary("Egads! Internal instruments cannot be deleted. U mad?");
    }

    savePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        showDialog(saveConfirmDialog);
        return true;
      }
    });

    revertPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        ComplexOsc reverted = InstrumentService.getInstrument(modifying.getName());
        if (reverted != null) {
          modifying = reverted;
          String message = "Loaded instrument: " + modifying.getName();
          Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getBaseContext(), "No saved instrument exists with this name.", Toast.LENGTH_LONG).show();          
        }
        return true;
      }
    });

    deletePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        showDialog(deleteConfirmDialog);
        return true;
      }
    });
  }
  protected Dialog onCreateDialog(int id) {
    Dialog dialog = null;
      
    switch(id) {
      case deleteConfirmDialog:
        dialog =
          new AlertDialog.Builder(this)
                .setMessage("Delete saved instrument '" + modifying.getName() + "' ? This cannot be undone.")
                .setCancelable(true)
                .setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        deleteInstrument();
                      }
                })
                .setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                      }
                })
                .create();
        break;
      case deletedInfoDialog:
        dialog =
          new AlertDialog.Builder(this)
                .setMessage("Successfully deleted '" + modifying.getName() + "' from disk. This instrument will remain in memory until another instrument is chosen. At that point it will be lost unless saved. Yathzee!")
                .setCancelable(false)
                .setNeutralButton("OK",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        exit();
                      }
                })
                .create();
        break;
      case saveConfirmDialog:
        dialog =
          new AlertDialog.Builder(this)
                .setMessage("Save '"+modifying.getName()+"' to SD card? This will overwrite any existing instrument with this name.")
                .setCancelable(true)
                .setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        Box<ComplexOsc> savedBox = InstrumentService.saveInstrument(modifying);
                        String latestName = modifying.getName(),
                                message = "";
                        if (savedBox.isDefined()) {
                          message = "Disco! Instrument saved to SD card: " + latestName;
                        } else if (savedBox.isFailure()) {
                          message = savedBox.getFailure();
                        } else {
                          message = "Instrument could not be saved to SD card";
                        }
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                      }
                })
                .setNegativeButton("Don't Save",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                      }
                })
                .create();
        break;
    }
    return dialog;
  }

  private void requestName() {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);

    alert.setTitle("Name Your Instrument");

    // Set an EditText view to get user input 
    final EditText input = new EditText(this);
    alert.setView(input);

    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        String value = input.getText().toString().trim();

        Box<Boolean> isValidName = InstrumentService.isValidInstrumentName(value);
        if (isValidName.isFailure()) {
          Toast.makeText(getBaseContext(), "Invalid name. " + isValidName.getFailure(), Toast.LENGTH_SHORT).show();
          requestName();
          return;
        }

        modifying.setName(value);
      }
    });

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        exit();
        modifying = null;
      }
    });
    
    alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
      public void onCancel(DialogInterface dialog) {
        exit();
        modifying = null;
      }
    });

    alert.show();
  }

  private void deleteInstrument() {
    Box<Boolean> savedBox = InstrumentService.deleteInstrument(modifying.getName());
 
    if (savedBox.isDefined()) {
      showDialog(deletedInfoDialog);
    } else if (savedBox.isFailure()) {
      Toast.makeText(getBaseContext(), savedBox.getFailure(), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getBaseContext(), "Unable to delete. This instrument may not be saved.", Toast.LENGTH_SHORT).show();
    }
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
    //EditTextPreference namePref = (EditTextPreference) findPreference("namePref");
    //String name = namePref.getText();
    //modifying.setName(name);

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
