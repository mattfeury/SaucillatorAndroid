package com.mattfeury.saucillator.dev.android.settings;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.Theory;
import com.mattfeury.saucillator.dev.android.instruments.Theory.Scale;
import com.mattfeury.saucillator.dev.android.utilities.ViewBinders;

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

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);
  }

  private void saveChanges() {
    Intent intent = new Intent(Settings.this, SauceEngine.class);

    setResult(0, intent);

    finish();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK)
      saveChanges();
    
    return true;
  }
}
